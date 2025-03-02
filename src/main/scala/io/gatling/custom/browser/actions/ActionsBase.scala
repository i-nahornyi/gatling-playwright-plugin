package io.gatling.custom.browser.actions

import com.microsoft.playwright.{Browser, Page}
import io.gatling.core.Predef.Status
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.protocol.BrowserProtocol

import scala.collection.concurrent.TrieMap

trait ActionsBase {

  val ctx: ScenarioContext
  private val browserComponent = ctx.protocolComponentsRegistry.components(BrowserProtocol.browserProtocolKey)
  val contextOptions: Browser.NewContextOptions = browserComponent.contextOptions
  val browserInstances: TrieMap[Long, Browser] = browserComponent.browserInstances
  val BROWSER_CONTEXT_KEY = "gatling.browserContext"

  protected def getBrowserContextFromSession(session: Session): Page = {

    if (session.contains(BROWSER_CONTEXT_KEY))
      session(BROWSER_CONTEXT_KEY).as[Page]
    else
      browserInstances(session.userId).newContext(contextOptions).newPage()
  }

  protected def executeNext(
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
