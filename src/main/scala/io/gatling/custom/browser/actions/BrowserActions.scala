package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page.NavigateOptions
import com.microsoft.playwright.impl.TargetClosedError
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
import io.gatling.custom.browser.utils.PlaywrightExceptionParser
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

      val getBrowserAndSession = getBrowserContextFromSession(session)
      // Use become original session unmodified
      var currentSession = getBrowserAndSession._2
      this.page = getBrowserAndSession._1

      logger.trace(s"userID-${currentSession.userId}, execute Open action $resolvedRequestName  --> $resolvedUrl")

      var isCrashed = false
      var status: Status = OK
      var message: Option[String] = Option.empty

      val startTime = clock.nowMillis

      try {
        if (options == null) page.navigate(resolvedUrl) else page.navigate(resolvedUrl, options)
      }
      catch {
        case assertionFailedError: AssertionFailedError =>
          logger.debug(s"AssertionFailedError: $resolvedRequestName ${assertionFailedError.getMessage}")
          status = KO
          message = PlaywrightExceptionParser.parseAssertionErrorMessage(assertionFailedError.getMessage)
        case targetClosedError: TargetClosedError =>
          logger.debug(s"TargetClosedError: $resolvedRequestName ${targetClosedError.getMessage}")
          status = KO
          message = Option.apply("Target page, context or browser has been closed")
        case playwrightException: PlaywrightException =>
          logger.debug(s"PlaywrightException: $resolvedRequestName ${playwrightException.getMessage}")
          status = KO
          message = PlaywrightExceptionParser.parseErrorMessage(playwrightException.getMessage, playwrightException.getClass.getSimpleName)
        case exception: Exception =>
          logger.debug(s"Browser action crashed: $resolvedRequestName ${exception.getMessage}")
          status = KO
          message = Option.apply(s"crashed with ${exception.getMessage}")
          isCrashed = true;
      }
      finally {
        val endTime = clock.nowMillis
        if (status == KO) currentSession = currentSession.markAsFailed
        if (status == KO && message.isEmpty) message = Option.apply(s"action: $resolvedRequestName marked as KO")
        executeNext(currentSession.set(BROWSER_CONTEXT_KEY, page), startTime, endTime, status, next, resolvedRequestName, None, message, isCrashed)
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

    val getBrowserAndSession = getBrowserContextFromSession(session)
    // Use because original session unmodified
    var currentSession = getBrowserAndSession._2
    this.page = getBrowserAndSession._1
    logger.trace(s"userID-${session.userId}, execute Flow action $resolvedRequestName")

    var isCrashed = false
    var status: Status = OK
    var message: Option[String] = Option.empty

    val postProcessorFunc = function.andThen(result => {
      status = result.getStatus
      message = result.getErrorMessage
      result
    })

    var browserSession: BrowserSession = new BrowserSession(currentSession)
    var startTime = clock.nowMillis

    try {
      browserSession = postProcessorFunc.apply(page, browserSession)
      currentSession = browserSession.getScalaSession()
    }
    catch {
      case assertionFailedError: AssertionFailedError =>
        logger.debug(s"AssertionFailedError: $resolvedRequestName ${assertionFailedError.getMessage}")
        status = KO
        message = PlaywrightExceptionParser.parseAssertionErrorMessage(assertionFailedError.getMessage)
      case targetClosedError: TargetClosedError =>
        logger.debug(s"TargetClosedError: $resolvedRequestName ${targetClosedError.getMessage}")
        status = KO
        message = Option.apply("Target page, context or browser has been closed")
      case playwrightException: PlaywrightException =>
        logger.debug(s"PlaywrightException: $resolvedRequestName ${playwrightException.getMessage}")
        status = KO
        message = PlaywrightExceptionParser.parseErrorMessage(playwrightException.getMessage, playwrightException.getClass.getSimpleName)
      case exception: Exception =>
        logger.debug(s"Browser action crashed: $resolvedRequestName ${exception.getMessage}")
        status = KO
        message = Option.apply(s"crashed with ${exception.getMessage}")
        isCrashed = true;
    }
    finally {
      var endTime = clock.nowMillis
      if (browserSession.getActionStartTime != 0) startTime = browserSession.getActionStartTime
      if (browserSession.getActionEndTime != 0) endTime = browserSession.getActionEndTime
      if (status == KO) currentSession = currentSession.markAsFailed
      if (status == KO && message.isEmpty) message = Option.apply(s"action: $resolvedRequestName marked as KO")
      executeNext(currentSession.set(BROWSER_CONTEXT_KEY, page), startTime, endTime, status, next, resolvedRequestName, None, message, isCrashed)
    }
  }

  override def clock: Clock = ctx.coreComponents.clock

  override def requestName: Expression[String] = actionName
}

case class BrowserActionsSessionFunction(function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with ActionsBase {
  var page: Page = _

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("browserSessionFunction")

  override protected def execute(session: Session): Unit = {

    val getBrowserAndSession = getBrowserContextFromSession(session)
    // Use because original session unmodified
    var currentSession = getBrowserAndSession._2
    this.page = getBrowserAndSession._1
    var status: Status = OK
    var message: Option[String] = Option.empty

    var browserSession = new BrowserSession(currentSession)
    try {
      browserSession = function.apply(page, browserSession)
      currentSession = browserSession.getScalaSession()
      status = browserSession.getStatus
    }
    catch {
      case assertionFailedError: AssertionFailedError =>
        message = PlaywrightExceptionParser.parseAssertionErrorMessage(assertionFailedError.getMessage)
        logger.error(s"'$name' failed to execute: $message")
        logger.trace(s"AssertionFailedError: $name ${assertionFailedError.getMessage}")
        status = KO
      case targetClosedError: TargetClosedError =>
        message = Option.apply("Target page, context or browser has been closed")
        logger.error(s"'$name' failed to execute: $message")
        logger.trace(s"TargetClosedError: $name ${targetClosedError.getMessage}")
        status = KO
      case playwrightException: PlaywrightException =>
        message = PlaywrightExceptionParser.parseErrorMessage(playwrightException.getMessage, playwrightException.getClass.getSimpleName)
        logger.error(s"'$name' failed to execute: $message")
        logger.trace(s"PlaywrightException: $name ${playwrightException.getMessage}")
        status = KO
      case exception: Exception =>
        logger.error(s"'$name' failed to execute: ${exception.getMessage}")
        logger.trace(s"Browser action crashed: $name ${exception.getMessage}")
        status = KO
    }
    finally {
      if (status == KO) currentSession = currentSession.markAsFailed
      next ! currentSession.set(BROWSER_CONTEXT_KEY, page)
    }
  }

}

case class BrowserActionsClearContext(ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with ActionsBase {
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
