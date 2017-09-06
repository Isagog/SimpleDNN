/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.deeplearning.mergelayers.affine

import com.kotlinnlp.simplednn.core.layers.BackwardHelper
import com.kotlinnlp.simplednn.core.layers.LayerParameters
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray

/**
 * The helper which executes the backward on an affine [layer].
 *
 * @property layer the [AffineLayerStructure] in which the backward is executed
 */
class AffineBackwardHelper<InputNDArrayType : NDArray<InputNDArrayType>>(
  override val layer: AffineLayerStructure<InputNDArrayType>
) : BackwardHelper<InputNDArrayType> {

  /**
   * A support variable to manage the errors on the parameters during the backward.
   */
  lateinit private var paramsErrors: AffineLayerParameters

  /**
   * Executes the backward calculating the errors of the parameters and eventually of the input through the SGD
   * algorithm, starting from the preset errors of the output array.
   *
   * @param paramsErrors the errors of the parameters which will be filled
   * @param propagateToInput whether to propagate the errors to the input array
   */
  override fun backward(paramsErrors: LayerParameters, propagateToInput: Boolean) {

    this.paramsErrors = paramsErrors as AffineLayerParameters

    this.layer.applyOutputActivationDeriv()

    this.assignParamsGradients()

    if (propagateToInput) {
      this.assignLayerGradients()
    }
  }

  /**
   */
  private fun assignParamsGradients() {

    val x1: InputNDArrayType = this.layer.inputArray.values
    val x2: InputNDArrayType = this.layer.inputArray2.values

    val gy: DenseNDArray = this.layer.outputArray.errors
    val gw1: NDArray<*> = this.paramsErrors.w1.values
    val gw2: NDArray<*> = this.paramsErrors.w2.values
    val gb: NDArray<*> = this.paramsErrors.b.values

    gw1.assignDot(gy, x1.T)
    gw2.assignDot(gy, x2.T)
    gb.assignValues(gy)
  }

  /**
   */
  private fun assignLayerGradients() {

    val w1: DenseNDArray = this.layer.params.w1.values as DenseNDArray
    val w2: DenseNDArray = this.layer.params.w2.values as DenseNDArray

    val gy: DenseNDArray = this.layer.outputArray.errors
    val gyT: DenseNDArray = gy.T

    this.layer.inputArray1.assignErrorsByDotT(gyT, w1)
    this.layer.inputArray2.assignErrorsByDotT(gyT, w2)
  }
}