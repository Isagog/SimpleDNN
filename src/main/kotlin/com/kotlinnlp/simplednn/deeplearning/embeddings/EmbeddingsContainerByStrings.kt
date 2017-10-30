/* Copyright 2016-present The KotlinNLP Authors. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, you can obtain one at http://mozilla.org/MPL/2.0/.
 * ------------------------------------------------------------------*/

package com.kotlinnlp.simplednn.deeplearning.embeddings

/**
 * An extension of the [EmbeddingsContainer] which can also map Embeddings also to strings.
 *
 * @property count the number of embeddings in this [EmbeddingsContainer] (e.g. number of word in a vocabulary)
 * @property size the size of each embedding (typically a range between about 50 to a few hundreds)
 * @property pseudoRandomDropout a Boolean indicating if Embeddings must be dropped out with pseudo random probability
 */
class EmbeddingsContainerByStrings(
  count: Int,
  size: Int,
  pseudoRandomDropout: Boolean = true
) : EmbeddingsContainerBase<EmbeddingsContainerByStrings>(
  count = count,
  size = size,
  pseudoRandomDropout = pseudoRandomDropout) {

  companion object {

    /**
     * Private val used to serialize the class (needed from Serializable)
     */
    @Suppress("unused")
    private const val serialVersionUID: Long = 1L
  }

  /**
   * Map strings to ids of embeddings.
   */
  private val idsMap = mutableMapOf<String, Int>()

  /**
   * Get the embedding with the given [id] as [String].
   * If the [id] is null return the [nullEmbedding].
   * If the [id] is negative or greater than [count] return the [unknownEmbedding].
   *
   * @param id the [String] id of an embedding (can be null)
   * @param dropout the probability to get the [unknownEmbedding] (default = 0.0 = no dropout)
   *
   * @return the Embedding with the given [id] or [nullEmbedding] or [unknownEmbedding]
   */
  fun getEmbedding(id: String?, dropout: Double = 0.0): Embedding {

    return if (id == null) {

      super.getEmbedding(id = null, dropout = dropout)

    } else {

      if (!this.idsMap.containsKey(id)) {
        this.idsMap[id] = this.idsMap.size
      }

      super.getEmbedding(this.idsMap[id]!!, dropout = dropout)
    }
  }

  /**
   * @param id the [String] id of an embedding
   *
   * @return the [Int] id of to the element with the given [String] [id] or null if it is not associated to any
   *         embedding
   */
  fun getIntId(id: String): Int? = if (this.idsMap.containsKey(id)) this.idsMap[id] else null
}
