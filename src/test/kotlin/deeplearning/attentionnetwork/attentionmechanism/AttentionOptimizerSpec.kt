/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package deeplearning.attentionnetwork.attentionmechanism

import com.kotlinnlp.simplednn.core.functionalities.updatemethods.learningrate.LearningRateMethod
import com.kotlinnlp.simplednn.deeplearning.attentionnetwork.attentionmechanism.AttentionOptimizer
import com.kotlinnlp.simplednn.deeplearning.attentionnetwork.attentionmechanism.AttentionParameters
import com.kotlinnlp.simplednn.simplemath.ndarray.dense.DenseNDArrayFactory
import deeplearning.attentionnetwork.utils.AttentionLayerUtils
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import kotlin.test.assertTrue

/**
 *
 */
class AttentionOptimizerSpec : Spek({

  describe("an AttentionOptimizer") {

    on("accumulation") {

      val params = AttentionLayerUtils.buildAttentionParams()
      val optimizer = AttentionOptimizer(params = params, updateMethod = LearningRateMethod(learningRate = 0.1))
      val errors = AttentionParameters(attentionSize = 2)

      errors.contextVector.values.assignValues(DenseNDArrayFactory.arrayOf(doubleArrayOf(0.2, -0.5)))
      optimizer.accumulateErrors(errors)

      errors.contextVector.values.assignValues(DenseNDArrayFactory.arrayOf(doubleArrayOf(0.4, -0.3)))
      optimizer.accumulateErrors(errors)

      optimizer.update()

      it("should update the context vector correctly") {
        assertTrue {
          params.contextVector.values.equals(
            DenseNDArrayFactory.arrayOf(doubleArrayOf(-0.33, -0.46)),
            tolerance = 1.0e-06
          )
        }
      }
    }
  }
})
