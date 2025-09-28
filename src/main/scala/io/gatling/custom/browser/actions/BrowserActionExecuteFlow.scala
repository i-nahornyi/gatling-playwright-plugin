package io.gatling.custom.browser.actions

import com.microsoft.playwright.{Page, PlaywrightException}
import com.microsoft.playwright.impl.TargetClosedError
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Status
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY
import io.gatling.custom.browser.utils.PlaywrightExceptionParser
import org.opentest4j.AssertionFailedError

import java.util.function.BiFunction

case class BrowserActionExecuteFlow(actionName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action)
  extends RequestAction with NameGen with BrowserActionsBase {

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
