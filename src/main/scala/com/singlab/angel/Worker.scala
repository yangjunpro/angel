package com.singlab.angel

import akka.actor._
import akka.event.Logging
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration._

class Worker(masterActorPath: String) {

    val config = ConfigFactory.load()
    val actorSystem = ActorSystem("angelWorker", config.getConfig("angel"))

    def startActor(): Future[ActorRef] = {
        val timeout = 1.second
        actorSystem.actorSelection(masterActorPath).resolveOne(timeout).map(master => {
            actorSystem.actorOf(Props(classOf[WorkerActor], master), "angelWorkerActor")
        })
    }

    class WorkerActor(master: ActorRef) extends Actor {

        val log = Logging(context.system, this)

        override def preStart = {
            log.info(s"Register to master at ${master.path}")
            master ! WorkerRegister()
        }

        override def receive = {
            case Shutdown =>
                log.debug(s"Received Shutdown from ${sender.path}")
                context.system.shutdown()
        }

    }
}

