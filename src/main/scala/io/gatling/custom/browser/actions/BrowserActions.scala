package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef.Status
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.model.BrowserSession
import org.opentest4j.AssertionFailedError

import java.util.function.BiFunction


case class BrowserActionOpen(requestName: String, url: String, ctx: ScenarioContext, next: Action)
  extends ChainableAction with NameGen with ActionsBase {

  var page: Page = _

  override def name: String = genName("browserActions")

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override protected def execute(session: Session): Unit = {
    if (session.contains("__browser_context")) this.page = session("__browser_context").as[Page] else this.page = browser.newContext(contextOptions).newPage()
    val clock = ctx.coreComponents.clock
    val startTime = clock.nowMillis
    page.navigate(url)
    val endTime = clock.nowMillis
    executeNext(session.set("__browser_context", page), startTime, endTime, OK, next, requestName, None, None,isCrashed = false)
  }
}

case class BrowserActionExecuteFlow(requestName: String, function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action)
  extends ChainableAction with NameGen with ActionsBase {

  var page: Page = _

  override def name: String = genName("browserActionsExecuteFlow")

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override protected def execute(session: Session): Unit = {
    if (session.contains("__browser_context")) page = session("__browser_context").as[Page] else page = browser.newContext(contextOptions).newPage()
    val clock = ctx.coreComponents.clock

    var browserSession = new BrowserSession(session)
    var status: Status = OK
    var message: Option[String] = Option.empty
    val postProcessorFunc = function.andThen(result => {
      status = result.getStatus
      message = Option.apply(result.getErrorMessage)
      result
    })
    var startTime = clock.nowMillis
    var endTime = startTime+1
    var isCrashed = false
    try {
      browserSession = postProcessorFunc.apply(page, browserSession)
    }
    catch {
      case assertionFailedError: AssertionFailedError =>
        status = KO
        message = Option.apply(assertionFailedError.getMessage)
      case exception: Exception =>
        logger.error(s"action: $requestName crashed ${exception.getMessage}")
        status = KO
        message = Option.apply("action crashed")
        isCrashed = true;
    }
    finally {
      endTime = clock.nowMillis
      if (browserSession.actionStartTime != null) startTime = browserSession.actionStartTime
      if (browserSession.actionEndTime != null) endTime = browserSession.actionEndTime
      if (status == KO && message.isEmpty) message = Option.apply("action: "+ requestName + "marked KO")
      executeNext(browserSession.gatlingScalaSession.set("__browser_context", page), startTime, endTime, status, next, requestName, None, message,isCrashed)
    }
  }
}

case class BrowserActionsClearContext(ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen {
  var page: Page = _

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("clearBrowserContext")

  override protected def execute(session: Session): Unit = {
    if (session.contains("__browser_context")) session("__browser_context").as[Page].close()
    next ! session.remove("__browser_context")
  }
}
