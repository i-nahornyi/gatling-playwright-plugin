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

trait BrowserActionsBase extends StrictLogging {

  val ctx: ScenarioContext
  private val browserComponent = ctx.protocolComponentsRegistry.components(BrowserProtocol.browserProtocolKey)
  private val browserInstance: Browser = browserComponent.browserInstance
  val browserContextsPool: mutable.Map[Long, BrowserContext] = browserComponent.browserContextsPool
  val contextOptions: Browser.NewContextOptions = browserComponent.contextOptions
  val enableUIMetrics: Boolean = browserComponent.enableUIMetrics

  protected def getBrowserContextFromSession(session: Session): (Page, Session) = {

    val userID = session.userId

    if (browserContextsPool.contains(userID)) {

      var browserContext = browserContextsPool(session.userId)

      // Check browser is browser open
      if (!checkIsBrowserOpen(browserContext)) {
        logger.trace(s"browserContextPool contains context for userID-$userID, but browser was closed, create new instance")
        browserComponent.recreateBrowserInstance()
      }
      // Check page is closed
      if (!checkIsContextHaveActivePage(browserContext)) {
        logger.trace(s"browserContextPool contains context for userID-$userID, but page was closed, create new instance")
        browserContext = browserInstance.newContext(contextOptions)
        browserContextsPool.put(userID, browserContext)
        val page = browserContext.newPage()
        return (page, session.set(BROWSER_CONTEXT_KEY, page))
      }

      logger.trace(s"browserContextPool contains context for userID-$userID")
      (session(BROWSER_CONTEXT_KEY).as[Page], session)

    } else {
      logger.trace(s"browserContextPool doesn't contains context for userID-$userID, create new")
      val browserContext = browserInstance.newContext(contextOptions)
      browserContextsPool.put(userID, browserContext)
      val page = browserContext.newPage()
      (page, session.set(BROWSER_CONTEXT_KEY, page))
    }
  }

  private def checkIsBrowserOpen(browserContext: BrowserContext): Boolean = {
    browserContext.browser().isConnected
  }

  private def checkIsContextHaveActivePage(browserContext: BrowserContext): Boolean = {
    browserContext.pages().size() > 0
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
