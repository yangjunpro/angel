package com.singlab.angel

import akka.actor._
import akka.event.Logging
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

object Worker {

    def startActor(driverActorPath: String) = {

        val config = ConfigFactory.load()

        val actorSystem = ActorSystem("angelWorker", config.getConfig("angel"))
        val timeout = 1.second
        val remoteDriverActor = Await.result(actorSystem.actorSelection(driverActorPath).resolveOne(timeout), timeout)
        val actor = actorSystem.actorOf(Props(classOf[WorkerActor], remoteDriverActor), "angelWorkerActor")

        (actorSystem, actor)
    }

    class WorkerActor(driver: ActorRef) extends Actor {

        val log = Logging(context.system, this)

        override def preStart = {
            log.info(s"Register to driver at ${driver.path}")
            driver ! WorkerRegister()
        }

        override def receive = {
            case Shutdown =>
                log.debug(s"Received Shutdown from ${sender.path}")
                context.system.shutdown()
        }

    }
}

