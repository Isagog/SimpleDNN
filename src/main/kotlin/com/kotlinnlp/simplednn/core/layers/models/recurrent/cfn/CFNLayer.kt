/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers.models.recurrent.cfn

import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.functionalities.activations.ActivationFunction
import com.kotlinnlp.simplednn.core.functionalities.activations.Sigmoid
import com.kotlinnlp.simplednn.core.layers.LayerType
import com.kotlinnlp.simplednn.core.layers.models.recurrent.*
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.Shape

/**
 * The CFN Layer Structure.
 *
 * @property inputArray the input array of the layer
 * @property inputType the input array type (default Dense)
 * @property outputArray the output array of the layer
 * @property params the parameters which connect the input to the output
 * @property layersWindow the context window used for the forward and the backward
 * @property activationFunction the activation function of the layer
 * @property dropout the probability of dropout
 */
internal class CFNLayer<InputNDArrayType : NDArray<InputNDArrayType>>(
  inputArray: AugmentedArray<InputNDArrayType>,
  inputType: LayerType.Input,
  outputArray: AugmentedArray<DenseNDArray>,
  override val params: CFNLayerParameters,
  layersWindow: LayersWindow,
  activationFunction: ActivationFunction? = null,
  dropout: Double
) : GatedRecurrentLayer<InputNDArrayType>(
  inputArray = inputArray,
  inputType = inputType,
  outputArray = outputArray,
  params = params,
  layersWindow = layersWindow,
  activationFunction = activationFunction,
  dropout = dropout
) {

  /**
   *
   */
  val candidate = AugmentedArray(values = DenseNDArrayFactory.emptyArray(Shape(outputArray.size)))

  /**
   *
   */
  val inputGate = RecurrentLayerUnit<InputNDArrayType>(outputArray.size)

  /**
   *
   */
  val forgetGate = RecurrentLayerUnit<InputNDArrayType>(outputArray.size)

  /**
   *
   */
  var activatedPrevOutput: DenseNDArray? = null

  /**
   * The helper which executes the forward
   */
  override val forwardHelper = CFNForwardHelper(layer = this)

  /**
   * The helper which executes the backward
   */
  override val backwardHelper = CFNBackwardHelper(layer = this)

  /**
   * The helper which calculates the relevance
   */
  override val relevanceHelper: GatedRecurrentRelevanceHelper? = null

  /**
   * Initialization: set the activation function of the gates
   */
  init {

    this.inputGate.setActivation(Sigmoid)
    this.forgetGate.setActivation(Sigmoid)

    if (this.activationFunction != null) {
      this.candidate.setActivation(this.activationFunction)
    }
  }

  /**
   * Set the initial hidden array.
   * This method should be used when this layer is used as initial hidden state in a recurrent neural network.
   *
   * @param array the initial hidden array
   */
  override fun setInitHidden(array: DenseNDArray) {
    TODO("not implemented")
  }

  /**
   * Get the errors of the initial hidden array.
   * This method should be used only if this layer is used as initial hidden state in a recurrent neural network.
   *
   * @return the errors of the initial hidden array
   */
  override fun getInitHiddenErrors(): DenseNDArray {
    TODO("not implemented")
  }
}
