/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.deeplearning.attentionnetwork.attentionlayer

import com.kotlinnlp.simplednn.core.arrays.UpdatableArray
import com.kotlinnlp.simplednn.core.arrays.UpdatableDenseArray
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.FixedRangeRandom
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.RandomGenerator
import com.kotlinnlp.simplednn.core.optimizer.IterableParams
import com.kotlinnlp.simplednn.simplemath.ndarray.Shape
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory

/**
 * Attention Layer parameters.
 *
 * @property attentionSize the size of each array of attention
 */
class AttentionLayerParameters(val attentionSize: Int) : IterableParams<AttentionLayerParameters>() {

  /**
   * The context vector trainable parameter.
   */
  val contextVector = UpdatableDenseArray(values = DenseNDArrayFactory.zeros(Shape(this.attentionSize)))

  /**
   * The list of all parameters.
   */
  override val paramsList: Array<UpdatableArray<*>> = arrayOf(this.contextVector)

  /**
   * Initialize the context vector values randomly.
   *
   * @param randomGenerator a generator of random values
   */
  fun initialize(randomGenerator: RandomGenerator = FixedRangeRandom(radius = 0.08, enablePseudoRandom = true)) {
    this.contextVector.values.randomize(randomGenerator)
  }

  /**
   * @return a new [AttentionLayerParameters] containing a copy of all values of this
   */
  override fun copy(): AttentionLayerParameters {

    val clonedParams = AttentionLayerParameters(this.attentionSize)

    clonedParams.contextVector.values.assignValues(this.contextVector.values)

    return clonedParams
  }
}
