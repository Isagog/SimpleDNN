/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers.recurrent.cfn

import com.kotlinnlp.simplednn.core.arrays.UpdatableArray
import com.kotlinnlp.simplednn.core.layers.RecurrentParametersUnit
import com.kotlinnlp.simplednn.core.layers.LayerParameters
import com.kotlinnlp.simplednn.core.functionalities.randomgenerators.RandomGenerator

/**
 * The parameters of the layer of type CFN.
 *
 * @property inputSize input size
 * @property outputSize output size
 * @property sparseInput whether the weights connected to the input are sparse or not
 */
class CFNLayerParameters(
  inputSize: Int,
  outputSize: Int,
  private val sparseInput: Boolean = false
) : LayerParameters<CFNLayerParameters>(inputSize = inputSize, outputSize = outputSize) {

  /**
   *
   */
  val inputGate = RecurrentParametersUnit(
    inputSize = this.inputSize,
    outputSize = this.outputSize,
    sparseInput = this.sparseInput)

  /**
   *
   */
  val forgetGate = RecurrentParametersUnit(
    inputSize = this.inputSize,
    outputSize = this.outputSize,
    sparseInput = this.sparseInput)

  /**
   *
   */
  val candidateWeights: UpdatableArray<*> = this.buildUpdatableArray(
    dim1 = this.outputSize,
    dim2 = this.inputSize,
    sparseInput = this.sparseInput)

  /**
   * The list of all parameters.
   */
  override val paramsList = arrayOf(

    this.inputGate.weights,
    this.forgetGate.weights,
    this.candidateWeights,

    this.inputGate.biases,
    this.forgetGate.biases,

    this.inputGate.recurrentWeights,
    this.forgetGate.recurrentWeights
  )

  /**
   *
   */
  override fun initialize(randomGenerator: RandomGenerator, biasesInitValue: Double): CFNLayerParameters {
    require(!this.sparseInput) { "Cannot randomize sparse weights" }

    this.inputGate.weights.values.randomize(randomGenerator)
    this.forgetGate.weights.values.randomize(randomGenerator)
    this.candidateWeights.values.randomize(randomGenerator)

    this.inputGate.biases.values.assignValues(biasesInitValue)
    this.forgetGate.biases.values.assignValues(biasesInitValue)

    this.inputGate.recurrentWeights.values.randomize(randomGenerator)
    this.forgetGate.recurrentWeights.values.randomize(randomGenerator)

    return this
  }

  /**
   * @return a new [CFNLayerParameters] containing a copy of all parameters of this
   */
  override fun copy(): CFNLayerParameters {

    val clonedParams = CFNLayerParameters(
      inputSize = this.inputSize,
      outputSize = this.outputSize,
      sparseInput = this.sparseInput)

    clonedParams.assignValues(this)

    return clonedParams
  }
}
