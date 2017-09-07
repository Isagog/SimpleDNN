/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers.feedforward

import com.kotlinnlp.simplednn.core.layers.BackwardHelper
import com.kotlinnlp.simplednn.core.layers.LayerParameters
import com.kotlinnlp.simplednn.simplemath.ndarray.NDArray

/**
 * The helper which executes the backward on a [layer].
 *
 * @property layer the [FeedforwardLayerStructure] in which the backward is executed
 */
class FeedforwardBackwardHelper<InputNDArrayType : NDArray<InputNDArrayType>>(
  override val layer: FeedforwardLayerStructure<InputNDArrayType>
) : BackwardHelper<InputNDArrayType> {

  /**
   * Executes the backward calculating the errors of the parameters and eventually of the input through the SGD
   * algorithm, starting from the preset errors of the output array.
   *
   * @param paramsErrors the errors of the parameters which will be filled
   * @param propagateToInput whether to propagate the errors to the input array
   */
  override fun backward(paramsErrors: LayerParameters<*>, propagateToInput: Boolean) {

    this.layer.applyOutputActivationDeriv()

    this.assignParamsGradients(paramsErrors as FeedforwardLayerParameters)

    if (propagateToInput) {
      this.assignLayerGradients()
    }
  }

  /**
   * gb = gy * 1
   * gw = gy (dot) x
   *
   * @param paramsErrors the errors of the parameters which will be filled
   */
  private fun assignParamsGradients(paramsErrors: FeedforwardLayerParameters) {

    this.layer.outputArray.assignParamsGradients(
      paramsErrors = paramsErrors.unit,
      x = this.layer.inputArray.values)
  }

  /**
   * gx = gy (dot) w
   */
  private fun assignLayerGradients() { this.layer.params as FeedforwardLayerParameters
    this.layer.inputArray.assignErrors(this.layer.outputArray.getInputErrors(parameters = this.layer.params.unit))
  }
}
