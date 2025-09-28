package io.gatling.custom.browser.actions

import com.microsoft.playwright.{BrowserContext, Page}
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY

case class BrowserClearContext(ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with BrowserActionsBase {
  var page: Page = _

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("clearBrowserContext")

  override protected def execute(session: Session): Unit = {

    val userId = session.userId

    logger.trace(s"userID-$userId, execute ClearContext action")

    if (session.contains(BROWSER_CONTEXT_KEY)) {

      session(BROWSER_CONTEXT_KEY).as[Page].context().close(new BrowserContext.CloseOptions().setReason("Closing due to the BrowserActionsClearContext action"))
      logger.trace(s"userID-$userId, remove BrowserContext from BrowserContextPool")
      browserContextsPool.remove(userId)

      logger.trace(s"userID-$userId, remove BrowserContext from session")
      next ! session.remove(BROWSER_CONTEXT_KEY)
    }
    else {
      next ! session
    }
  }
}
