/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.simplemath.ndarray

import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.RandomGenerator
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.sparse.SparseNDArray
import java.io.Serializable

/**
 *
 */
interface NDArray<SelfType : NDArray<SelfType>> : Serializable {

  /**
   *
   */
  val factory: NDArrayFactory<SelfType>

  /**
   *
   */
  val isVector: Boolean

  /**
   * Whether the array is a bi-dimensional matrix
   */
  val isMatrix: Boolean get() = !this.isVector

  /**
   *
   */
  val isOneHotEncoder: Boolean

  /**
   *
   */
  val rows: Int

  /**
   *
   */
  val columns: Int

  /**
   *
   */
  val length: Int

  /**
   *
   */
  val shape: Shape

  /**
   * Transpose
   */
  val T: SelfType

  /**
   *
   */
  operator fun get(i: Int): Number

  /**
   *
   */
  operator fun get(i: Int, j: Int): Number


  /**
   *
   */
  operator fun set(i: Int, value: Number)

  /**
   *
   */
  operator fun set(i: Int, j: Int, value: Number)

  /**
   * Get the i-th row
   *
   * @param i the index of the row to be returned
   *
   * @return the selected row as a new NDArray
   */
  fun getRow(i: Int): SelfType

  /**
   * Get the i-th column
   *
   * @param i the index of the column to be returned
   *
   * @return the selected column as a new NDArray
   */
  fun getColumn(i: Int): SelfType

  /**
   * Get a one-dimensional NDArray sub-vector of a vertical vector.
   *
   * @param a the start index of the range (inclusive)
   * @param b the end index of the range (exclusive)
   *
   * @return the sub-array
   */
  fun getRange(a: Int, b: Int): SelfType

  /**
   *
   */
  fun zeros(): SelfType

  /**
   *
   */
  fun zerosLike(): SelfType

  /**
   *
   */
  fun copy(): SelfType

  /**
   *
   */
  fun assignValues(n: Double): SelfType

  /**
   *
   */
  fun assignValues(a: NDArray<*>): SelfType

  /**
   *
   */
  fun assignValues(a: NDArray<*>, mask: NDArrayMask): SelfType

  /**
   *
   */
  fun sum(): Double

  /**
   *
   */
  fun sum(n: Double): SelfType

  /**
   *
   */
  fun sum(a: SelfType): SelfType

  /**
   *
   */
  fun assignSum(n: Double): SelfType

  /**
   *
   */
  fun assignSum(a: NDArray<*>): SelfType

  /**
   *
   */
  fun assignSum(a: SelfType, n: Double): SelfType

  /**
   *
   */
  fun assignSum(a: SelfType, b: SelfType): SelfType
  /**
   *
   */
  fun sub(n: Double): SelfType

  /**
   *
   */
  fun sub(a: SelfType): SelfType

  /**
   * In-place subtraction by number
   */
  fun assignSub(n: Double): SelfType

  /**
   *
   */
  fun assignSub(a: NDArray<*>): SelfType

  /**
   *
   */
  fun reverseSub(n: Double): SelfType

  /**
   *
   */
  fun dot(a: NDArray<*>): DenseNDArray

  /**
   *
   */
  fun assignDot(a: SelfType, b: SelfType): SelfType

  /**
   *
   */
  fun assignDot(a: DenseNDArray, b: NDArray<*>): SelfType {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  /**
   *
   */
  fun prod(n: Double): SelfType

  /**
   *
   */
  fun prod(a: NDArray<*>): SelfType

  /**
   *
   */
  fun prod(n: Double, mask: NDArrayMask): SparseNDArray

  /**
   *
   */
  fun assignProd(n: Double): SelfType

  /**
   *
   */
  fun assignProd(n: Double, mask: NDArrayMask): SelfType

  /**
   *
   */
  fun assignProd(a: SelfType): SelfType

  /**
   *
   */
  fun assignProd(a: SelfType, n: Double): SelfType

  /**
   *
   */
  fun assignProd(a: SelfType, b: SelfType): SelfType

  /**
   *
   */
  fun div(n: Double): SelfType

  /**
   *
   */
  fun div(a: NDArray<*>): SelfType

  /**
   *
   */
  fun div(a: SparseNDArray): SparseNDArray

  /**
   *
   */
  fun assignDiv(n: Double): SelfType

  /**
   *
   */
  fun assignDiv(a: SelfType): SelfType

  /**
   *
   */
  fun avg(): Double

  /**
   * Sign function
   *
   * @return a new NDArray containing the results of the function sign() applied element-wise
   */
  fun sign(): SelfType

  /**
   *
   */
  fun sqrt(): SelfType

  /**
   * Square root of this [NDArray] masked by [mask]
   *
   * @param mask the mask to apply
   *
   * @return a [SparseNDArray]
   */
  fun sqrt(mask: NDArrayMask): SparseNDArray

  /**
   * Power
   *
   * @param power the exponent
   *
   * @return a new [NDArray] containing the values of this to the power of [power]
   */
  fun pow(power: Double): SelfType

  /**
   * In-place power
   *
   * @param power the exponent
   *
   * @return this [NDArray] to the power of [power]
   */
  fun assignPow(power: Double): SelfType

  /**
   * Euclidean norm of this NDArray
   *
   * @return the euclidean norm
   */
  fun norm2(): Double

  /**
   * @return the index of the maximum value (-1 if empty)
   **/
  fun argMaxIndex(): Int

  /**
   * Round values to Int
   *
   * @param threshold a value is rounded to the next Int if is >= [threshold], to the previous otherwise
   *
   * @return a new NDArray with the values of the current one rounded to Int
   */
  fun roundInt(threshold: Double = 0.5): SelfType

  /**
   * Round values to Int in-place
   *
   * @param threshold a value is rounded to the next Int if is >= [threshold], to the previous otherwise
   *
   * @return this NDArray
   */
  fun assignRoundInt(threshold: Double = 0.5): SelfType

  /**
   *
   */
  fun randomize(randomGenerator: RandomGenerator): SelfType

  /**
   *
   */
  fun concatH(a: SelfType): SelfType

  /**
   *
   */
  fun concatV(a: SelfType): SelfType

  /**
   * Split this NDArray into multiple NDArray.
   *
   * If the number of arguments is one, split this NDArray into multiple NDArray each with length [splittingLength].
   * If there are multiple arguments, split this NDArray according to the length of each [splittingLength] element.
   *
   * @param splittingLength the length(s) for sub-array division
   *
   * @return an Array containing the split values
   */
  fun splitV(vararg splittingLength: Int): Array<SelfType>

  /**
   *
   */
  fun equals(a: SelfType, tolerance: Double = 10e-4): Boolean

  /**
   *
   */
  fun equals(a: Any, tolerance: Double = 10e-4): Boolean {
    @Suppress("UNCHECKED_CAST")
    return a::class.isInstance(this) && this.equals(a as SelfType, tolerance)
  }

  /**
   *
   */
  override fun toString(): String

  /**
   *
   */
  override fun equals(other: Any?): Boolean

  /**
   *
   */
  override fun hashCode(): Int
}
