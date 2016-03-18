package com.singlab.angel

import akka.actor._

object ExternalAddress extends ExtensionKey[ExternalAddressExt] {
    def getPath(actorSystem: ActorSystem, actor: ActorRef) = {
        actor.path.toStringWithAddress(ExternalAddress(actorSystem).addressForAkka)
    }
}

class ExternalAddressExt(system: ExtendedActorSystem) extends Extension {
    def addressForAkka: Address = system.provider.getDefaultAddress
}
