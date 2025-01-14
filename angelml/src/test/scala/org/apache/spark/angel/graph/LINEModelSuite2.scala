/*
 * Tencent is pleased to support the open source community by making Angel available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * https://opensource.org/licenses/Apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package org.apache.spark.angel.graph

import org.apache.hadoop.fs.FileSystem
import org.apache.spark.{SparkConf, SparkContext, SparkFunSuite}
import org.apache.spark.angel.graph.embedding.Param
import org.apache.spark.angel.graph.embedding.line2.LINEModel
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

class LINEModelSuite2 extends SparkFunSuite {
  private val LOCAL_FS = FileSystem.DEFAULT_FS
  private val TMP_PATH = System.getProperty("java.io.tmpdir", "/tmp")

  private var spark: SparkSession = _
  private var sparkConf: SparkConf = _
  private var sc: SparkContext = _
  val input = "../data/angel/bc/edge"
  val oldOutput = null
  val output = LOCAL_FS + TMP_PATH + "/linemodel_v2"
  val numPartition = 1
  val lr = 0.025f
  val dim = 32
  val batchSize = 1024
  val numPSPart = 1
  val numEpoch = 5
  val negative = 5
  val storageLevel: StorageLevel = StorageLevel.MEMORY_ONLY
  var param: Param = _
  var data: RDD[(Int, Int)] = _

  override def beforeAll(): Unit = {
    super.beforeAll()
    spark = SparkSession.builder()
      .master(s"local[$numPartition]")
      .appName("AngelGraph")
      .getOrCreate()

    sc = spark.sparkContext
    sparkConf = spark.sparkContext.getConf

    data = sc.textFile(input).mapPartitions { iter =>
      val r = new Random()
      iter.map { line =>
        val arr = line.split(" ")
        val src = arr(0).toInt
        val dst = arr(1).toInt
        (r.nextInt, (src, dst))
      }
    }.repartition(numPartition).values.persist(storageLevel)
    val numEdge = data.count()
    val maxNodeId = data.map { case (src, dst) => math.max(src, dst) }.max().toLong + 1
    println(s"numEdge=$numEdge maxNodeId=$maxNodeId")

    param = new Param()
    param.setLearningRate(lr)
    param.setEmbeddingDim(dim)
    param.setBatchSize(batchSize)
    param.setNumPSPart(Some(numPSPart))
    param.setNumEpoch(numEpoch)
    param.setNegSample(negative)
    param.setMaxIndex(maxNodeId)
    param.setModelCPInterval(1000)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    spark.close()
  }

  test("first order") {
    param.setOrder(1)
    val model = new LINEModel(param) {
      val messages = new ArrayBuffer[String]()

      // mock logs
      override def logTime(msg: String): Unit = {
        if (null != messages) {
          messages.append(msg)
        }
        println(msg)
      }
    }

    if(oldOutput == null) {
      model.randomInitialize(param.seed)
    } else {
      model.load(oldOutput)
    }

    model.train(data, param, "")
    model.save(output, 0)
    model.destroy()

    // extract loss
    val loss = model.messages.filter(_.contains("loss=")).map { line =>
      line.split(" ")(1).split("=").last.toFloat
    }

    for (i <- 0 until numEpoch - 1) {
      assert(loss(i + 1) < loss(i), s"loss increase: ${loss.mkString("->")}")
    }
  }

  test("second order") {
    param.setOrder(2)
    val model = new LINEModel(param) {
      val messages = new ArrayBuffer[String]()

      // mock logs
      override def logTime(msg: String): Unit = {
        if (null != messages) {
          messages.append(msg)
        }
        println(msg)
      }
    }

    if(oldOutput == null) {
      model.randomInitialize(param.seed)
    } else {
      model.load(oldOutput)
    }

    model.train(data, param, "")
    model.save(output, 0)
    model.destroy()

    // extract loss
    val loss = model.messages.filter(_.contains("loss=")).map { line =>
      line.split(" ")(1).split("=").last.toFloat
    }

    for (i <- 0 until numEpoch - 1) {
      assert(loss(i + 1) < loss(i), s"loss increase: ${loss.mkString("->")}")
    }
  }
}
