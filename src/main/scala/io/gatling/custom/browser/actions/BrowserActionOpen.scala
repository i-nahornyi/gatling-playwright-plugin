package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page.NavigateOptions
import com.microsoft.playwright.impl.TargetClosedError
import com.microsoft.playwright.{Page, PlaywrightException}
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.Predef.Status
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.model.PageLoadValidator
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY
import io.gatling.custom.browser.utils.{PerformanceUIHelper, PlaywrightExceptionParser}
import org.opentest4j.AssertionFailedError

case class BrowserActionOpen(actionName: Expression[String], url: Expression[String], navigateOptions: NavigateOptions = null, pageLoadValidator: PageLoadValidator = null, ctx: ScenarioContext, next: Action)
  extends RequestAction with NameGen with BrowserActionsBase {

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

      logger.trace(s"userID-${currentSession.userId}, execute Open action '$resolvedRequestName' --> $resolvedUrl")

      var isCrashed = false
      var status: Status = OK
      var message: Option[String] = Option.empty

      if (enableUIMetrics) PerformanceUIHelper.injectMetricTrackingScript(page)
      val startTime = clock.nowMillis

      try {
          page.navigate(resolvedUrl, navigateOptions)
          if(pageLoadValidator != null){
            logger.trace(s"userID-${currentSession.userId} start executing pageLoadValidator script")
            PerformanceUIHelper.checkIsPageLoaded(page, pageLoadValidator)
            logger.trace(s"userID-${currentSession.userId} finished executing pageLoadValidator script")
          }
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
        if (enableUIMetrics && !page.isClosed) PerformanceUIHelper.reportUIMetrics(startTime, resolvedRequestName, page, status)
        executeNext(currentSession.set(BROWSER_CONTEXT_KEY, page), startTime, endTime, status, next, resolvedRequestName, None, message, isCrashed)
      }
    }

  override def clock: Clock = ctx.coreComponents.clock

  override def requestName: Expression[String] = actionName
}
