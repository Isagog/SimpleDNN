/* Copyright 2020-present Simone Cangialosi. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.deeplearning.transformers

import com.kotlinnlp.simplednn.core.arrays.AugmentedArray
import com.kotlinnlp.simplednn.core.embeddings.EmbeddingsMap
import com.kotlinnlp.simplednn.core.functionalities.activations.Softmax
import com.kotlinnlp.simplednn.core.functionalities.updatemethods.UpdateMethod
import com.kotlinnlp.simplednn.core.layers.LayerType
import com.kotlinnlp.simplednn.core.layers.models.feedforward.simple.FeedforwardLayer
import com.kotlinnlp.simplednn.core.layers.models.feedforward.simple.FeedforwardLayerParameters
import com.kotlinnlp.simplednn.core.optimizer.ParamsOptimizer
import com.kotlinnlp.simplednn.helpers.Trainer
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArray
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import com.kotlinnlp.utils.DictionarySet
import com.kotlinnlp.utils.Shuffler
import java.io.File
import java.io.FileOutputStream

/**
 * The trainer of a [BERT] model.
 *
 * @param model the parameters of the model of the network
 * @param modelFilename the name of the file in which to save the serialized model
 * @param embeddingsMap pre-trained word embeddings
 * @param dictionary a dictionary set with all the forms in the examples
 * @param termsDropout the probability to dropout an input token
 * @param updateMethod the update method helper (Learning Rate, ADAM, AdaGrad, ...)
 * @param examples the training examples
 * @param epochs the number of training epochs
 * @param evaluator the helper for the evaluation (default null)
 * @param shuffler used to shuffle the examples before each epoch (with pseudo random by default)
 * @param verbose whether to print info about the training progress and timing (default = true)
 */
class BERTTrainer(
  private val model: BERTParameters,
  modelFilename: String,
  private val embeddingsMap: EmbeddingsMap<String>,
  private val dictionary: DictionarySet<String>,
  private val termsDropout: Double = 0.15,
  updateMethod: UpdateMethod<*>,
  examples: List<List<String>>,
  epochs: Int,
  evaluator: BERTEvaluator? = null,
  shuffler: Shuffler = Shuffler(),
  verbose: Boolean = true
) : Trainer<List<String>>(
  modelFilename = modelFilename,
  optimizers = listOf(ParamsOptimizer(updateMethod)),
  examples = examples,
  epochs = epochs,
  batchSize = 1,
  evaluator = evaluator,
  shuffler = shuffler,
  verbose = verbose
) {

  /**
   * A Bidirectional Encoder Representations from Transformers.
   */
  private val bert = BERT(this.model)

  /**
   * A feed-forward layer trained to reproduce the input vectors.
   * It is used only during the training phase.
   */
  private val classificationLayer = FeedforwardLayer<DenseNDArray>(
    inputArray = AugmentedArray(size = this.model.inputSize),
    outputArray = AugmentedArray(size = this.dictionary.size + 1), // dictionary size + unknown
    params = FeedforwardLayerParameters(inputSize = this.model.inputSize, outputSize = this.dictionary.size + 1),
    inputType = LayerType.Input.Dense,
    activationFunction = Softmax())

  /**
   * Learn from an example (forward + backward).
   *
   * @param example an example to train the model with
   */
  override fun learnFromExample(example: List<String>) {

    val encodings: List<DenseNDArray> = this.encodeExample(example)

    this.bert.forward(encodings)

    val encodingErrors: List<DenseNDArray> = encodings.zip(example).map { (vector, form) ->
      this.classifyVector(vector = vector, goldIndex = this.getId(form))
    }

    this.bert.backward(encodingErrors)
  }

  /**
   * Accumulate the errors of the model resulting after the call of [learnFromExample].
   */
  override fun accumulateErrors() {
    this.optimizers.single().accumulate(this.bert.getParamsErrors(copy = false))
  }

  /**
   * Dump the model to file.
   */
  override fun dumpModel() {
    this.model.dump(FileOutputStream(File(modelFilename)))
  }

  /**
   * @param forms the forms that compose an example
   *
   * @return the encodings of the given forms
   */
  private fun encodeExample(forms: List<String>): List<DenseNDArray> = forms.map {
    this.embeddingsMap.get(key = it, dropout = this.termsDropout).values
  }

  /**
   * Classify a vector (representing a term) comparing the result with the expected term index.
   *
   * @param vector the vector to classify
   * @param goldIndex the index of the classifying term
   *
   * @return the vector errors respect to the classification made
   */
  private fun classifyVector(vector: DenseNDArray, goldIndex: Int): DenseNDArray {

    this.classificationLayer.setInput(vector)
    this.classificationLayer.forward()

    val goldOutput: DenseNDArray = DenseNDArrayFactory.oneHotEncoder(
      length = this.classificationLayer.outputArray.size,
      oneAt = goldIndex)
    val errors: DenseNDArray = this.classificationLayer.outputArray.values.sub(goldOutput)

    this.classificationLayer.setErrors(errors)
    this.classificationLayer.backward(propagateToInput = true)

    this.optimizers.single().accumulate(this.classificationLayer.getParamsErrorsCollector().getAll())

    return this.classificationLayer.inputArray.errors
  }

  /**
   * @param form a form found in the examples
   *
   * @return a unique ID of the given form, considering all the unknown terms equal to each other
   */
  private fun getId(form: String): Int =
    this.dictionary.getId(form) ?: this.dictionary.size + 1
}
