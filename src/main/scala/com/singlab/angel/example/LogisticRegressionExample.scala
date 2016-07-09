package com.singlab.angel.example

import com.singlab.angel.{Master, ParameterServer, Worker}
import org.apache.spark.mllib.linalg.{Vector, Vectors}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

object LogisticRegressionExample {

    def main(args: Array[String]): Unit = {

        val sparkConf = new SparkConf().setAppName("test").setMaster("yarn-cluster")
        val sparkContext = new SparkContext(sparkConf)
        val sqlContext = new SQLContext(sparkContext)

        val training = sqlContext.createDataFrame(Seq(
            (1.0, Vectors.dense(0.0, 1.1, 0.1)),
            (0.0, Vectors.dense(2.0, 1.0, -1.0)),
            (0.0, Vectors.dense(2.0, 1.3, 1.0)),
            (1.0, Vectors.dense(0.0, 1.2, -0.5))
        )).toDF("label", "features")

        val test = sqlContext.createDataFrame(Seq(
            (1.0, Vectors.dense(-1.0, 1.5, 1.3)),
            (0.0, Vectors.dense(3.0, 2.0, -0.1)),
            (1.0, Vectors.dense(0.0, 2.2, -1.5))
        )).toDF("label", "features")

        sparkContext.broadcast()

        val master = new Master()
        val masterPath = master.masterActorPath

        training.repartition(2).foreachPartition(data => {

            val ps = new ParameterServer(masterPath)
            val worker = new Worker(masterPath)

            ps.startActor()
            worker.startActor()

            data.foreach(row => {
                val x = row.getAs[Vector]("features")
                x.foreachActive((index, value) => {
                })
                val y = row.getAs[Double]("label")
            })
        })
    }

}
