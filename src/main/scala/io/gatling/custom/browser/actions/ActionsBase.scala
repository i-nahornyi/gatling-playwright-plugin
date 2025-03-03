package io.gatling.custom.browser.actions

import com.microsoft.playwright.{Browser, BrowserContext, Page}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.Predef.Status
import io.gatling.core.action.Action
import io.gatling.core.session.Session
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.protocol.BrowserProtocol
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY

import scala.collection.mutable

trait ActionsBase extends StrictLogging {

  val ctx: ScenarioContext
  private val browserComponent = ctx.protocolComponentsRegistry.components(BrowserProtocol.browserProtocolKey)
  private val browserInstances: Browser = browserComponent.browser
  val browserContextsPool: mutable.Map[Long, BrowserContext] = browserComponent.browserContextsPool
  val contextOptions: Browser.NewContextOptions = browserComponent.contextOptions

  protected def getBrowserContextFromSession(session: Session): Page = {

    val userID = session.userId

    if (browserContextsPool.contains(userID)) {
      logger.debug(s"browserContextPool contains context for userID-$userID")
      session(BROWSER_CONTEXT_KEY).as[Page]
    } else {
      logger.debug(s"browserContextPool doesn't contains context for userID-$userID, create new")
      val browserContext: BrowserContext = browserInstances.newContext(contextOptions)
      browserContextsPool.put(userID, browserContext)
      val page = browserContext.newPage()
      session.set(BROWSER_CONTEXT_KEY, page)
      page
    }
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
