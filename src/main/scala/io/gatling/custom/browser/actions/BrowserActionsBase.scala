package io.gatling.custom.browser.actions

import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.Predef.Status
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.actor.BrowserActorPool
import io.gatling.custom.browser.protocol.BrowserProtocol

trait BrowserActionsBase extends StrictLogging {

  val ctx: ScenarioContext
  private val browserComponent = ctx.protocolComponentsRegistry.components(BrowserProtocol.browserProtocolKey)
  val enableUIMetrics: Boolean = browserComponent.enableUIMetrics
  val browserActorPool: BrowserActorPool = browserComponent.actorPool

  protected[actions] def loadPageInstance(session: Session): Unit = {
    browserActorPool.loadPageInstance(session)
  }

  protected[actions] def executeNext(
                             session: Session,
                             sent: Long,
                             received: Long,
                             status: Status,
                             next: Action,
                             requestName: String,
                             responseCode: Option[String],
                             message: Option[String],
                             isCrashed: Boolean
                           ): Unit = {

    if (!isCrashed) {
      ctx.coreComponents.statsEngine.logResponse(session.scenario, session.groups, requestName, sent, received, status, responseCode, message)
    }
    else {
      ctx.coreComponents.statsEngine.logRequestCrash(session.scenario, session.groups, requestName, message.get)
    }
    next ! session.logGroupRequestTimings(sent, received)
  }
}
