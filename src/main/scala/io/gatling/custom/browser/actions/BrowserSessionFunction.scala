package io.gatling.custom.browser.actions

import com.microsoft.playwright.{Page, PlaywrightException}
import com.microsoft.playwright.impl.TargetClosedError
import io.gatling.commons.stats.{KO, OK}
import io.gatling.core.Predef.Status
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY
import io.gatling.custom.browser.utils.PlaywrightExceptionParser
import org.opentest4j.AssertionFailedError

import java.util.function.BiFunction

case class BrowserSessionFunction(function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with BrowserActionsBase {
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
