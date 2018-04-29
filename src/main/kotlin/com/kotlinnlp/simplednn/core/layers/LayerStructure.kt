/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers

import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.arrays.Norm1Array
import com.kotlinnlp.simplednn.core.functionalities.activations.ActivationFunction
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray

/**
 * The Layer Structure.
 *
 * @property inputArray the input array of the layer
 * @property outputArray the output array of the layer
 * @property params the parameters which connect the input to the output
 * @property activationFunction the activation function of the layer
 * @property dropout the probability of dropout (default 0.0).
 *                   If applying it, the usual value is 0.5 (better 0.25 if it's the first layer).
 */
abstract class LayerStructure<InputNDArrayType : NDArray<InputNDArrayType>>(
  val inputArray: AugmentedArray<InputNDArrayType>,
  open val outputArray: AugmentedArray<DenseNDArray>,
  open val params: LayerParameters<*>,
  val activationFunction: ActivationFunction? = null,
  val dropout: Double = 0.0) {

  /**
   * The probability to keep an output value (no dropout on it)
   */
  private val p = 1.0 - this.dropout

  /**
   * The helper which execute the forward
   */
  protected abstract val forwardHelper: ForwardHelper<InputNDArrayType>

  /**
   * The helper which execute the backward
   */
  protected abstract val backwardHelper: BackwardHelper<InputNDArrayType>

  /**
   * The helper which calculates the relevance
   */
  protected abstract val relevanceHelper: RelevanceHelper<InputNDArrayType>

  /**
   * Set the values of the inputArray
   *
   * @param values the values to set into the inputArray
   */
  fun setInput(values: InputNDArrayType) = this.inputArray.assignValues(values)

  /**
   * Set the errors of the outputArray
   *
   * @param errors the errors to set into the outputArray
   */
  fun setErrors(errors: DenseNDArray) = this.outputArray.assignErrors(errors)

  /**
   * Set the relevance of the outputArray
   *
   * @param relevance the relevance to set into the outputArray
   */
  fun setOutputRelevance(relevance: Norm1Array<*>) = this.outputArray.assignRelevance(relevance.values)

  /**
   * Forward the input to the output combining it with the parameters.
   * If [useDropout] is true apply the dropout to the input before.
   *
   * @param useDropout whether to apply the dropout
   */
  fun forward(useDropout: Boolean = false) {

    if (useDropout) {
      this.applyDropout()
    }

    this.forwardHelper.forward()
  }

  /**
   * Forward the input to the output combining it with the parameters, calculating its relevance respect of the output.
   * If [useDropout] is true apply the dropout to the input before.
   *
   * @param layerContributions the [LayerParameters] in which to save the contributions during calculations
   * @param useDropout whether to apply the dropout
   */
  fun forward(layerContributions: LayerParameters<*>, useDropout: Boolean = false) {

    if (useDropout) {
      this.applyDropout()
    }

    this.forwardHelper.forward(layerContributions = layerContributions)
  }

  /**
   * Calculate the relevance of the input respect of the output and assign it to the input array.
   *
   * @param layerContributions the contributions saved during the last forward
   */
  fun setInputRelevance(layerContributions: LayerParameters<*>) {
    this.relevanceHelper.setInputRelevance(layerContributions = layerContributions)
  }

  /**
   * Calculate the relevance of the input respect of the output and add it to the relevance of the input array
   * previously set.
   *
   * @param layerContributions the contributions saved during the last forward
   */
  fun addInputRelevance(layerContributions: LayerParameters<*>) {
    this.relevanceHelper.addInputRelevance(layerContributions = layerContributions)
  }

  /**
   * @param paramsErrors the structure in which to save the errors of the parameters
   * @param propagateToInput whether to propagate the errors to the input
   * @param mePropK the k factor of the 'meProp' algorithm to propagate from the k (in percentage) output nodes with
   *                the top errors (ignored if null)
   */
  fun backward(paramsErrors: LayerParameters<*>, propagateToInput: Boolean, mePropK: Double?) {
    require(mePropK == null || mePropK in 0.0 .. 1.0) { "The mePropK must be a percentage in range [0.0, 1.0]" }

    this.backwardHelper.backward(
      paramsErrors = paramsErrors,
      propagateToInput = propagateToInput,
      mePropK = mePropK)
  }

  /**
   * Perform the multiplication of the output array by the derivative of its activated values.
   */
  fun applyOutputActivationDeriv() {

    if (this.outputArray.hasActivation) {
      this.outputArray.errors.assignProd(this.outputArray.calculateActivationDeriv())
    }
  }

  /**
   *
   */
  private fun applyDropout() {

    if (this.dropout > 0.0) {
      val inputShape = this.inputArray.values.shape
      val mask = this.inputArray.values.factory // mask of zeros and ones
        .random(inputShape)
        .roundInt(threshold = this.dropout)

      mask.assignDiv(this.p) // mask of zeros and [1.0 / this.p]

      this.inputArray.values.assignProd(mask)
    }
  }
}
