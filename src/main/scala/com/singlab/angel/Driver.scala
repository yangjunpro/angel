package com.singlab.angel

import akka.actor._
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.concurrent.TrieMap

object Driver {

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

        train(training)

        val test = sqlContext.createDataFrame(Seq(
            (1.0, Vectors.dense(-1.0, 1.5, 1.3)),
            (0.0, Vectors.dense(3.0, 2.0, -0.1)),
            (1.0, Vectors.dense(0.0, 2.2, -1.5))
        )).toDF("label", "features")
    }

    def train(data: DataFrame) = {

        val config = ConfigFactory.load()

        val actorSystem = ActorSystem("driver", config.getConfig("angel"))
        val driverActor = actorSystem.actorOf(Props[DriverActor], "driver")
        val driverActorPath = ExternalAddress.getPath(actorSystem, driverActor)

        data.foreachPartition(partition => {

            val (parameterServerActorSystem, _) = ParameterServer.startActor(driverActorPath)
            val (workerActorSystem, _) = Worker.startActor(driverActorPath)

            parameterServerActorSystem.awaitTermination()
            workerActorSystem.awaitTermination()

            data
        })
    }

    class DriverActor extends Actor {

        val log = Logging(context.system, this)

        val partitionToPS = TrieMap[ActorPath, ActorRef]()
        val partitionToWorker = TrieMap[ActorPath, ActorRef]()

        override def receive = {
            case PSRegister() =>
                partitionToPS(sender.path) = sender
                log.debug(s"ParameterServer registered from ${sender.path}")
                sender ! Shutdown

            case WorkerRegister() =>
                partitionToWorker(sender.path) = sender
                log.debug(s"Worker registered from ${sender.path}")
                sender ! Shutdown
        }
    }
}

