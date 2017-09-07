/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.functionalities.updatemethods.rmsprop

import com.kotlinnlp.simplednn.core.functionalities.updatemethods.UpdateMethod
import com.kotlinnlp.simplednn.core.arrays.UpdatableDenseArray
import com.kotlinnlp.simplednn.core.functionalities.regularization.WeightsRegularization
import com.kotlinnlp.simplednn.simplemath.ndarray.*
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.sparse.SparseNDArray
import kotlin.reflect.KClass

/**
 * The RMSProp method is a variant of [com.kotlinnlp.simplednn.core.functionalities.updatemethods.adagrad.AdaGradMethod]
 * where the squared sum of previous gradients is replaced with a moving average.
 *
 * @property learningRate Double >= 0. Initial learning rate
 * @property epsilon Double >= 0. Bias parameter
 * @property decay Learning rate decay parameter
 *
 * References
 * [rmsprop: Divide the gradient by a running average of its recent magnitude](http://www.cs.toronto.edu/~tijmen/csc321/slides/lecture_slides_lec6.pdf)
 *
 */
class RMSPropMethod(
  val learningRate: Double = 0.001,
  val epsilon: Double = 1e-08,
  val decay: Double = 0.95,
  regularization: WeightsRegularization? = null
) : UpdateMethod<RMSPropStructure>(regularization) {

  /**
   * The Kotlin Class of the support structure of this updater.
   */
  override val structureClass: KClass<RMSPropStructure> = RMSPropStructure::class

  /**
   * Optimize sparse errors.
   *
   * @param errors the [SparseNDArray] errors to optimize
   * @param array an [UpdatableDenseArray]
   *
   * @return optimized sparse errors
   */
  override fun optimizeSparseErrors(errors: SparseNDArray, array: UpdatableDenseArray): SparseNDArray {

    val helperStructure: RMSPropStructure = this.getSupportStructure(array)
    val m = helperStructure.secondOrderMoments

    val mask: NDArrayMask = errors.mask
    val mUpdate: SparseNDArray =
      m.prod(this.decay, mask = mask).assignSum(errors.prod(errors).assignProd(1.0 - this.decay))

    m.assignValues(mUpdate)

    return errors.div(m.sqrt(mask = mask).assignSum(this.epsilon)).assignProd(this.learningRate)
  }

  /**
   * Optimize dense errors.
   *
   * @param errors the [DenseNDArray] errors to optimize
   * @param array an [UpdatableDenseArray]
   *
   * @return optimized dense errors
   */
  override fun optimizeDenseErrors(errors: DenseNDArray, array: UpdatableDenseArray): DenseNDArray {

    val helperStructure: RMSPropStructure = this.getSupportStructure(array)
    val m = helperStructure.secondOrderMoments

    m.assignSum(m.prod(this.decay), errors.prod(errors).assignProd(1.0 - this.decay))

    return errors.div(m.sqrt().assignSum(this.epsilon)).assignProd(this.learningRate)
  }
}
