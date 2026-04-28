package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.Clock
import io.gatling.commons.validation.Validation
import io.gatling.core.action.{Action, RequestAction}
import io.gatling.core.session.{Expression, Session}
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.actor.BrowserWorkerActor.ActionStatus
import io.gatling.custom.browser.actor.{BrowserCommandResponse, BrowserWorkerActor}
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.utils.Constants

import java.util.function.BiFunction
import scala.concurrent.Await

case class BrowserActionExecuteFlow(actionName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action)
  extends RequestAction with NameGen with BrowserActionsBase {

  override def name: String = genName("browserActionExecuteFlow")

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def sendRequest(session: Session): Validation[Unit] = for {
    resolvedRequestName <- requestName(session)
  } yield {

    loadPageInstance(session)
    logger.trace(s"userID-${session.userId}, execute Flow action $resolvedRequestName")

    var actionResult = BrowserCommandResponse(clock.nowMillis, clock.nowMillis, session, ActionStatus())

    val browserActorWorker = browserActorPool.getActorById(session.userId)
    val promise = browserActorWorker.replyPromise[BrowserCommandResponse](Constants.PROMISE_TIMEOUT)

    try {
      browserActorWorker ! BrowserWorkerActor.ExecuteFlow(
        session,
        resolvedRequestName,
        function,
        enableUIMetrics,
        clock,
        promise
      )
      actionResult = Await.result(promise.future, Constants.PROMISE_TIMEOUT)
    }
    catch {
      case exception: Exception =>
        logger.debug(s"Browser action crashed: $resolvedRequestName ${exception.getMessage}")
        actionResult.actionStatus = ActionStatus(KO,Option.apply(s"crashed with ${exception.getMessage}"), isCrashed = true)
        actionResult.endTime = clock.nowMillis
    }
    finally {
      if (actionResult.actionStatus.status == KO) actionResult.session = actionResult.session.markAsFailed
      if (actionResult.actionStatus.status == KO && actionResult.actionStatus.message.isEmpty) actionResult.actionStatus.message = Option.apply(s"action: $resolvedRequestName marked as KO")
      executeNext(actionResult.session, actionResult.startTime, actionResult.endTime, actionResult.actionStatus.status, next, resolvedRequestName, None, actionResult.actionStatus.message, actionResult.actionStatus.isCrashed)
    }
  }

  override def clock: Clock = ctx.coreComponents.clock

  override def requestName: Expression[String] = actionName
}
