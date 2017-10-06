/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.deeplearning.attentionnetwork

import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.functionalities.activations.Tanh
import com.kotlinnlp.simplednn.core.layers.*
import com.kotlinnlp.simplednn.core.layers.feedforward.FeedforwardLayerParameters
import com.kotlinnlp.simplednn.core.layers.feedforward.FeedforwardLayerStructure
import com.kotlinnlp.simplednn.core.optimizer.ParamsErrorsAccumulator
import com.kotlinnlp.simplednn.deeplearning.attentionnetwork.attentionlayer.AttentionLayerParameters
import com.kotlinnlp.simplednn.deeplearning.attentionnetwork.attentionlayer.AttentionLayerStructure
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.sparse.SparseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.sparsebinary.SparseBinaryNDArray
import com.kotlinnlp.simplednn.utils.ItemsPool

/**
 * The Attention Network which classifies an input sequence using an Attention Layer and a Feedforward Layer as
 * transform layer.
 *
 * @property model the parameters of the model of the network
 * @property inputType the type of the input arrays
 * @property dropout the probability of dropout (default 0.0) when generating the attention arrays for the Attention
 *                   Layer. If applying it, the usual value is 0.5 (better 0.25 if it's the first layer).
 * @property id an identification number useful to track a specific [AttentionNetwork]
 */
class AttentionNetwork<InputNDArrayType: NDArray<InputNDArrayType>>(
  val model: AttentionNetworkParameters,
  val inputType: LayerType.Input,
  val dropout: Double = 0.0,
  override val id: Int = 0
) : ItemsPool.IDItem {

  /**
   * The accumulator of errors of the transform layer parameters.
   */
  private val transformParamsErrorsAccumulator = ParamsErrorsAccumulator<FeedforwardLayerParameters>()

  /**
   * The attention transform layer which creates an attention array from each array of an input sequence.
   */
  lateinit private var transformLayers: Array<FeedforwardLayerStructure<InputNDArrayType>>

  /**
   * The Attention Layer of input.
   */
  lateinit private var attentionLayer: AttentionLayerStructure<InputNDArrayType>

  /**
   * Forward an input sequence.
   *
   * @param inputSequence the list of arrays of input
   * @param useDropout whether to apply the dropout to generate the attention arrays
   *
   * @return the output [DenseNDArray]
   */
  fun forward(inputSequence: ArrayList<AugmentedArray<InputNDArrayType>>,
              useDropout: Boolean = false): DenseNDArray {

    this.setInputSequence(inputSequence = inputSequence, useDropout = useDropout)
    this.attentionLayer.forward()

    return this.attentionLayer.outputArray.values
  }

  /**
   * Propagate the output errors using the gradient descent algorithm.
   *
   * @param outputErrors the errors to propagate from the output
   * @param paramsErrors the structure in which to save the errors of the parameters
   * @param propagateToInput whether to propagate the errors to the input
   */
  fun backward(outputErrors: DenseNDArray,
               paramsErrors: AttentionNetworkParameters,
               propagateToInput: Boolean = false) {

    this.backwardAttentionLayer(
      outputErrors = outputErrors,
      paramsErrors = paramsErrors.attentionParams,
      propagateToInput = propagateToInput)

    // WARNING: call it after the backward of the attention layer
    this.backwardTransformLayers(
      paramsErrors = paramsErrors.transformParams,
      propagateToInput = propagateToInput)

    if (propagateToInput) {
      this.addTransformErrorsToInput()
    }
  }

  /**
   * @return the errors of the arrays of input.
   */
  fun getInputErrors(): Array<DenseNDArray> {

    return Array(
      size = this.attentionLayer.inputSequence.size,
      init = { i -> this.attentionLayer.inputSequence[i].errors }
    )
  }

  /**
   * @param copy a Boolean indicating whether the returned importance score must be a copy or a reference
   *
   * @return the importance score of each array of input in a [DenseNDArray]
   */
  fun getImportanceScore(copy: Boolean = true): DenseNDArray =
    if (copy)
      this.attentionLayer.importanceScore.copy()
    else
      this.attentionLayer.importanceScore

  /**
   * The factory of the transform layer.
   *
   * @return a new FeedforwardLayerStructure
   */
  private fun transformLayerFactory(): FeedforwardLayerStructure<InputNDArrayType> {

    @Suppress("UNCHECKED_CAST")
    val inputArray = when (this.inputType) {
      LayerType.Input.Dense -> AugmentedArray<DenseNDArray>(size = this.model.inputSize)
      LayerType.Input.Sparse -> AugmentedArray<SparseNDArray>(size = this.model.inputSize)
      LayerType.Input.SparseBinary -> AugmentedArray<SparseBinaryNDArray>(size = this.model.inputSize)
    } as AugmentedArray<InputNDArrayType>

    return LayerStructureFactory(
      inputArray = inputArray,
      outputSize = this.model.attentionSize,
      params = this.model.transformParams,
      activationFunction = Tanh(),
      connectionType = LayerType.Connection.Feedforward,
      dropout = this.dropout
    ) as FeedforwardLayerStructure
  }

  /**
   * Set the input sequence.
   *
   * @param inputSequence the list of arrays of input
   * @param useDropout whether to apply the dropout to generate the attention arrays
   */
  private fun setInputSequence(inputSequence: ArrayList<AugmentedArray<InputNDArrayType>>,
                               useDropout: Boolean = false) {

    this.attentionLayer = AttentionLayerStructure(
      inputSequence = inputSequence,
      attentionSequence = this.buildAttentionSequence(inputSequence = inputSequence, useDropout = useDropout),
      params = this.model.attentionParams
    )
  }

  /**
   * @param inputSequence the list of arrays of input
   * @param useDropout whether to apply the dropout
   *
   * @return the list of attention arrays associated to each array of the [inputSequence]
   */
  private fun buildAttentionSequence(
    inputSequence: ArrayList<AugmentedArray<InputNDArrayType>>,
    useDropout: Boolean
  ): ArrayList<DenseNDArray> {

    val attentionSequence = ArrayList<DenseNDArray>()

    this.transformLayers = Array(size = inputSequence.size, init = { this.transformLayerFactory() })

    inputSequence.forEachIndexed { i, inputArray ->

      val layer = this.transformLayers[i]

      layer.setInput(inputArray.values)
      layer.forward(useDropout = useDropout)

      attentionSequence.add(layer.outputArray.values)
    }

    return attentionSequence
  }

  /**
   * Attention Layer backward.
   *
   * @param outputErrors the errors to propagate from the output
   * @param paramsErrors the structure in which to save the errors of the parameters
   * @param propagateToInput whether to propagate the errors to the input
   */
  private fun backwardAttentionLayer(outputErrors: DenseNDArray,
                                     paramsErrors: AttentionLayerParameters,
                                     propagateToInput: Boolean = false) {

    this.attentionLayer.setErrors(outputErrors)
    this.attentionLayer.backward(paramsErrors = paramsErrors, propagateToInput = propagateToInput)
  }

  /**
   * Transform Layers backward.
   *
   * @param paramsErrors the structure in which to save the errors of the parameters
   * @param propagateToInput whether to propagate the errors to the input
   */
  private fun backwardTransformLayers(paramsErrors: FeedforwardLayerParameters,
                                      propagateToInput: Boolean = false) {

    val attentionErrors: Array<DenseNDArray> = this.attentionLayer.getAttentionErrors()

    // Accumulate errors into the accumulator
    this.transformLayers.forEachIndexed { i, layer ->
      layer.setErrors(attentionErrors[i])
      layer.backward(paramsErrors = paramsErrors, propagateToInput = propagateToInput)
      this.transformParamsErrorsAccumulator.accumulate(paramsErrors)
    }

    this.transformParamsErrorsAccumulator.averageErrors()

    val accumulatedErrors: FeedforwardLayerParameters = this.transformParamsErrorsAccumulator.getParamsErrors()
    paramsErrors.zip(accumulatedErrors).forEach { (a, b) -> a.values.assignValues(b.values) }

    this.transformParamsErrorsAccumulator.reset()
  }

  /**
   * Add the input errors of the transform layer to each input array.
   */
  private fun addTransformErrorsToInput() {

    this.attentionLayer.inputSequence.forEachIndexed { i, inputArray ->
      inputArray.errors.assignSum(this.transformLayers[i].inputArray.errors)
    }
  }
}
