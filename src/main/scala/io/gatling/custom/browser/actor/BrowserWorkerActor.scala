package io.gatling.custom.browser.actor

import com.microsoft.playwright._
import io.gatling.commons.stats.OK
import io.gatling.commons.util.Clock
import io.gatling.core.Predef.Status
import io.gatling.core.actor.{Actor, Behavior}
import io.gatling.core.session.Session
import io.gatling.custom.browser.actor.BrowserWorkerActor._
import io.gatling.custom.browser.model.{BrowserSession, PageLoadValidator}
import io.gatling.custom.browser.utils.{PerformanceUIHelper, PlaywrightExceptionHandler}

import java.util.function.BiFunction
import scala.concurrent.Promise
import scala.util.Try

object BrowserWorkerActor {


  sealed trait BrowserWorkerCommand

  protected[browser] case class InitBrowserWorker(promise: Promise[Unit]) extends BrowserWorkerCommand

  protected[browser] case class StopBrowserWorker(reason: String, promise: Promise[Unit]) extends BrowserWorkerCommand

  protected[browser] case class CreateNewPage(promise: Promise[Page]) extends BrowserWorkerCommand

  protected[browser] case class ClosePage(promise: Promise[Unit]) extends BrowserWorkerCommand

  protected[browser] case class GetBrowserContext(promise: Promise[BrowserContext]) extends BrowserWorkerCommand

  protected[browser] case class RecreateBrowser(promise: Promise[Browser]) extends BrowserWorkerCommand

  protected[browser] case class Navigate(session: Session, resolvedRequestName: String, resolvedUrl: String, navigateOptions: Page.NavigateOptions, pageLoadValidator: PageLoadValidator, enableUIMetrics: Boolean, clock: Clock, promise: Promise[BrowserCommandResponse]) extends BrowserWorkerCommand

  protected[browser] case class ExecuteFlow(session: Session, resolvedRequestName: String, function: BiFunction[Page, BrowserSession, BrowserSession], enableUIMetrics: Boolean, clock: Clock, promise: Promise[BrowserCommandResponse]) extends BrowserWorkerCommand

  protected[browser] case class ActionStatus(var status: Status = OK, var message: Option[String] = Option.empty, var isCrashed: Boolean = false)

}

case class BrowserCommandResponse(var startTime: Long, var endTime: Long, var session: Session, var actionStatus: ActionStatus)

class BrowserWorkerActor(actorName: String, launchOptions: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions, defaultTimeout: Option[Double] = None) extends Actor[BrowserWorkerCommand](actorName) {

  private var playwright: Playwright = _
  private var browser: Browser = _
  private var browserContext: BrowserContext = _
  private var page: Page = _

  private def applyDefaultTimeout(context: BrowserContext): Unit = {
    defaultTimeout.foreach { timeoutMillis =>
      context.setDefaultTimeout(timeoutMillis)
      context.setDefaultNavigationTimeout(timeoutMillis)
    }
  }

  override def init(): Behavior[BrowserWorkerCommand] = {

    case commandContext: InitBrowserWorker =>
      logger.trace(s"${Thread.currentThread().getName} ===> Create BrowserWorkerActor name=$actorName")
      playwright = Playwright.create()
      browser = playwright.chromium().launch(launchOptions)
      browserContext = browser.newContext(contextOptions)
      applyDefaultTimeout(browserContext)
      page = browserContext.newPage()
      commandContext.promise.trySuccess(())
      stay

    case commandContext: GetBrowserContext =>
      commandContext.promise.complete(Try(browserContext))
      stay

    case commandContext: RecreateBrowser =>
      browser = playwright.chromium().launch(launchOptions)
      commandContext.promise.complete(Try(browser))
      stay

    case commandContext: CreateNewPage =>
      browserContext = browser.newContext(contextOptions)
      applyDefaultTimeout(browserContext)
      page = browserContext.newPage()
      commandContext.promise.complete(Try(page))
      stay

    case commandContext: ClosePage =>
      page.context().close(new BrowserContext.CloseOptions().setReason("Closing due to the BrowserActionsClearContext action"))
      commandContext.promise.trySuccess(())
      stay

    case commandContext: StopBrowserWorker =>
      browser.close(new Browser.CloseOptions().setReason(commandContext.reason))
      playwright.close()
      commandContext.promise.trySuccess(())
      die


    case commandContext: Navigate =>

      var actionStatus: ActionStatus = ActionStatus()
      val currentSession = commandContext.session
      val startTime = commandContext.clock.nowMillis

      try {
        page.navigate(commandContext.resolvedUrl, commandContext.navigateOptions)
        if (commandContext.pageLoadValidator != null) {
          logger.trace(s"userID-${currentSession.userId} start executing pageLoadValidator script")
          PerformanceUIHelper.checkIsPageLoaded(page, commandContext.pageLoadValidator)
          logger.trace(s"userID-${currentSession.userId} finished executing pageLoadValidator script")
        }
      }
      catch {
        case exception: Throwable =>
          actionStatus = PlaywrightExceptionHandler.handleException(exception, commandContext.resolvedRequestName)
      }
      finally {
        val endTime = commandContext.clock.nowMillis
        if (commandContext.enableUIMetrics) PerformanceUIHelper.reportUIMetrics(startTime, commandContext.resolvedRequestName, page, actionStatus.status, currentSession.userId)
        commandContext.promise.trySuccess(BrowserCommandResponse(startTime, endTime, currentSession, actionStatus))
      }
      stay

    case commandContext: ExecuteFlow =>

      var actionStatus: ActionStatus = ActionStatus()
      var currentSession = commandContext.session

      val postProcessorFunc = commandContext.function.andThen(result => {
        actionStatus.status = result.getStatus
        actionStatus.message = result.getErrorMessage
        result
      })

      var browserSession = new BrowserSession(currentSession)

      var startTime = commandContext.clock.nowMillis
      try {
        browserSession = postProcessorFunc.apply(page, browserSession)
        currentSession = browserSession.getGatlingSession
      }
      catch {
        case exception: Throwable =>
          actionStatus = PlaywrightExceptionHandler.handleException(exception, commandContext.resolvedRequestName)
      }
      finally {
        var endTime = commandContext.clock.nowMillis
        if (browserSession.getActionStartTime != 0) startTime = browserSession.getActionStartTime
        if (browserSession.getActionEndTime != 0) endTime = browserSession.getActionEndTime
        //        if (executeFlow.enableUIMetrics) PerformanceUIHelper.reportUIMetrics(startTime, executeFlow.resolvedRequestName, page, status, currentSession.userId)
        commandContext.promise.trySuccess(BrowserCommandResponse(startTime, endTime, currentSession, actionStatus))
      }
      stay
  }


}

