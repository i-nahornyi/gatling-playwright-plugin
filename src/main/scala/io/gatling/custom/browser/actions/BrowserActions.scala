package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page.NavigateOptions
import com.microsoft.playwright.{BrowserContext, Page, PlaywrightException}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Status
import io.gatling.core.action.{Action, ChainableAction, RequestAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY
import org.opentest4j.AssertionFailedError

import java.util.function.BiFunction


case class BrowserActionOpen(actionName: Expression[String], url: Expression[String], options: NavigateOptions = null, ctx: ScenarioContext, next: Action)
  extends RequestAction with NameGen with ActionsBase {

  var page: Page = _

  override def name: String = genName("browserActionOpenUrl")

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def sendRequest(session: Session): Validation[Unit] =
    for {
      resolvedRequestName <- requestName(session)
      resolvedUrl <- url(session)
    } yield {

      this.page = getBrowserContextFromSession(session)
      logger.debug(s"userID-${session.userId}, execute Open action $resolvedRequestName  --> $resolvedUrl")

      var isCrashed = false
      var status: Status = OK
      var message: Option[String] = Option.empty

      val startTime = clock.nowMillis

      try {
        if(options == null) page.navigate(resolvedUrl) else page.navigate(resolvedUrl,options)
      }
      catch {
        case assertionFailedError: AssertionFailedError =>
          logger.error(s"AssertionFailedError: $resolvedRequestName ${assertionFailedError.getMessage}")
          status = KO
          message = Option.apply(assertionFailedError.getMessage)
        case playwrightException: PlaywrightException =>
          logger.error(s"PlaywrightException: $resolvedRequestName ${playwrightException.getMessage}")
          status = KO
          message = Option.apply(playwrightException.getMessage)
        case exception: Exception =>
          logger.error(s"Browser action crashed: $resolvedRequestName ${exception.getMessage}")
          status = KO
          message = Option.apply("action crashed")
          isCrashed = true;
      }
      finally {
        val endTime = clock.nowMillis
        if (status == KO && message.isEmpty) message = Option.apply("action: " + requestName + "marked KO")
        executeNext(session.set(BROWSER_CONTEXT_KEY, page), startTime, endTime, status, next, resolvedRequestName, None, message, isCrashed)
      }
    }

  override def clock: Clock = ctx.coreComponents.clock

  override def requestName: Expression[String] = actionName
}

case class BrowserActionExecuteFlow(actionName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action)
  extends RequestAction with NameGen with ActionsBase {

  var page: Page = _

  override def name: String = genName("browserActionExecuteFlow")

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def sendRequest(session: Session): Validation[Unit] = for {
    resolvedRequestName <- requestName(session)
  } yield {

    this.page = getBrowserContextFromSession(session)
    logger.debug(s"userID-${session.userId}, execute Flow action $resolvedRequestName")

    var isCrashed = false
    var status: Status = OK
    var message: Option[String] = Option.empty

    val postProcessorFunc = function.andThen(result => {
      status = result.getStatus
      message = result.getErrorMessage
      result
    })

    var browserSession: BrowserSession = new BrowserSession(session)
    var startTime = clock.nowMillis

    try {
      browserSession = postProcessorFunc.apply(page, browserSession)
    }
    catch {
      case assertionFailedError: AssertionFailedError =>
        logger.error(s"AssertionFailedError: $resolvedRequestName ${assertionFailedError.getMessage}")
        status = KO
        message = Option.apply(assertionFailedError.getMessage)
      case playwrightException: PlaywrightException =>
        logger.error(s"PlaywrightException: $resolvedRequestName ${playwrightException.getMessage}")
        status = KO
        message = Option.apply(playwrightException.getMessage)
      case exception: Exception =>
        logger.error(s"Browser action crashed: $resolvedRequestName ${exception.getMessage}")
        status = KO
        message = Option.apply("action crashed")
        isCrashed = true;
    }
    finally {
      var endTime = clock.nowMillis
      if (browserSession.getActionStartTime != 0) startTime = browserSession.getActionStartTime
      if (browserSession.getActionEndTime != 0) endTime = browserSession.getActionEndTime
      if (status == KO && message.isEmpty) message = Option.apply("action: " + requestName + "marked KO")
      executeNext(browserSession.getScalaSession().set(BROWSER_CONTEXT_KEY, page), startTime, endTime, status, next, resolvedRequestName, None, message, isCrashed)
    }
  }

  override def clock: Clock = ctx.coreComponents.clock

  override def requestName: Expression[String] = actionName
}

case class BrowserActionsClearContext(ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with ActionsBase {
  var page: Page = _

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("clearBrowserContext")

  override protected def execute(session: Session): Unit = {

    val userId = session.userId

    logger.debug(s"userID-$userId, execute ClearContext action")

    if (session.contains(BROWSER_CONTEXT_KEY)){

      session(BROWSER_CONTEXT_KEY).as[Page].context().close(new BrowserContext.CloseOptions().setReason("Closing due to the BrowserActionsClearContext action"))
      browserContextsPool.remove(userId)

      logger.debug(s"userID-$userId, remove BrowserContext from BrowserContextPool")
    }

    logger.debug(s"userID-$userId, remove BrowserContext from session")
    next ! session.remove(BROWSER_CONTEXT_KEY)
  }
}
