/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.functionalities.updatemethods.nesterovmomentum

import com.kotlinnlp.simplednn.core.functionalities.updatemethods.momentum.MomentumStructure
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import com.kotlinnlp.simplednn.simplemath.ndarray.Shape

/**
 * @param shape shape
 */
class NesterovMomentumStructure(shape: Shape) : MomentumStructure(shape) {

  /**
   * Used with Nesterov Momentum
   */
  val vPrev: DenseNDArray by lazy { DenseNDArrayFactory.zeros(shape) }
}
