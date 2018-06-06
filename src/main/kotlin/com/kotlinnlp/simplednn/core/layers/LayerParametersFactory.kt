/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.core.layers

import com.kotlinnlp.simplednn.core.functionalities.initializers.Initializer
import com.kotlinnlp.simplednn.core.layers.feedforward.FeedforwardLayerParameters
import com.kotlinnlp.simplednn.core.layers.feedforward.highway.HighwayLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.cfn.CFNLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.deltarnn.DeltaRNNLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.gru.GRULayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.indrnn.IndRNNLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.lstm.LSTMLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.ran.RANLayerParameters
import com.kotlinnlp.simplednn.core.layers.recurrent.simple.SimpleRecurrentLayerParameters
import com.kotlinnlp.simplednn.core.mergelayers.affine.AffineLayerParameters
import com.kotlinnlp.simplednn.core.mergelayers.biaffine.BiaffineLayerParameters
import com.kotlinnlp.simplednn.core.mergelayers.concat.ConcatLayerParameters
import com.kotlinnlp.simplednn.core.mergelayers.product.ProductLayerParameters
import com.kotlinnlp.simplednn.core.mergelayers.sum.SumLayerParameters

/**
 * Helper that builds generic [LayerParameters].
 */
object LayerParametersFactory {

  /**
   * Build new generic [LayerParameters].
   *
   * @param inputsSize the list of input sizes (more then one only for Merge layers)
   * @param outputSize the size of the output
   * @param connectionType the type of connection from the input to the output
   * @param weightsInitializer the initializer of the weights (zeros if null, default: Glorot)
   * @param biasesInitializer the initializer of the biases (zeros if null, default: Glorot)
   * @param sparseInput whether the weights connected to the input are sparse or not
   * @param meProp whether to use the 'meProp' errors propagation algorithm (params are sparse)
   *
   * @return new layer parameters
   */
  operator fun invoke(inputsSize: List<Int>,
                      outputSize: Int,
                      connectionType: LayerType.Connection,
                      weightsInitializer: Initializer?,
                      biasesInitializer: Initializer?,
                      sparseInput: Boolean = false,
                      meProp: Boolean = false): LayerParameters<*> = when (connectionType) {

    LayerType.Connection.Feedforward -> FeedforwardLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer,
      meProp = meProp)

    LayerType.Connection.Highway -> HighwayLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer,
      meProp = meProp)

    LayerType.Connection.Affine -> AffineLayerParameters(
      inputsSize = inputsSize,
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.Biaffine -> BiaffineLayerParameters(
      inputSize1 = inputsSize[0],
      inputSize2 = inputsSize[1],
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.Concat -> ConcatLayerParameters(inputsSize = inputsSize)

    LayerType.Connection.Sum -> SumLayerParameters(inputSize = inputsSize.first(), nInputs = inputsSize.size)

    LayerType.Connection.Product -> ProductLayerParameters(inputSize = inputsSize.first(), nInputs = inputsSize.size)

    LayerType.Connection.SimpleRecurrent -> SimpleRecurrentLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.GRU -> GRULayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.LSTM -> LSTMLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.CFN -> CFNLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.RAN -> RANLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer,
      meProp = meProp)

    LayerType.Connection.DeltaRNN -> DeltaRNNLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param

    LayerType.Connection.IndRNN -> IndRNNLayerParameters(
      inputSize = inputsSize.first(),
      outputSize = outputSize,
      sparseInput = sparseInput,
      weightsInitializer = weightsInitializer,
      biasesInitializer = biasesInitializer) // TODO: set 'meProp' param
  }
}
