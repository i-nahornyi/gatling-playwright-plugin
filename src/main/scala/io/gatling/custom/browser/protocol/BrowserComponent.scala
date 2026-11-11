package io.gatling.custom.browser.protocol

import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session
import io.gatling.custom.browser.actor.BrowserActorPool

case class BrowserComponent(enableUIMetrics: Boolean, actorPool: BrowserActorPool)
  extends ProtocolComponents with StrictLogging {


  override def onStart: Session => Session = session => {
    actorPool.createActor(session.userId)
    session
  }

  override def onExit: Session => Unit = session => {
    val userId = session.userId
    actorPool.stopActorById(userId, "Closing due to onExit hook")
  }

}
