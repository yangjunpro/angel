package com.singlab.angel

import akka.actor.{Actor, ActorPath, ActorRef, ActorSystem, Props}
import akka.event.Logging
import com.google.common.collect.HashBiMap
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConversions._

class Master {

    val config = ConfigFactory.load()
    val actorSystem = ActorSystem("master", config.getConfig("angel"))

    val masterActor = actorSystem.actorOf(Props[MasterActor], "master")
    val masterActorPath = ExternalAddress.getPath(actorSystem, masterActor)

    class MasterActor extends Actor {

        val log = Logging(context.system, this)

        val partitionToPS = HashBiMap.create[ActorPath, ActorRef]()
        val partitionToWorker = HashBiMap.create[ActorPath, ActorRef]()

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
