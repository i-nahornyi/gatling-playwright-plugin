package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import io.gatling.commons.stats.KO
import io.gatling.commons.util.Clock
import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.actor.BrowserWorkerActor.ActionStatus
import io.gatling.custom.browser.actor.{BrowserCommandResponse, BrowserWorkerActor}
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.utils.Constants

import java.util.function.BiFunction
import scala.concurrent.Await

case class BrowserSessionFunction(function: BiFunction[Page, BrowserSession, BrowserSession], ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with BrowserActionsBase {
  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("browserSessionFunction")

  override protected def execute(session: Session): Unit = {

    loadPageInstance(session)

    val browserActorWorker = browserActorPool.getActorById(session.userId)
    val promise = browserActorWorker.replyPromise[BrowserCommandResponse](Constants.PROMISE_TIMEOUT)
    var actionResult = BrowserCommandResponse(clock.nowMillis, clock.nowMillis, session, ActionStatus())

    try {
      browserActorWorker ! BrowserWorkerActor.ExecuteFlow(
        session,
        name,
        function,
        enableUIMetrics = false,
        clock,
        promise
      )
      actionResult = Await.result(promise.future, Constants.PROMISE_TIMEOUT)
    }
    catch {
      case exception: Exception =>
        logger.error(s"'$name' failed to execute: ${exception.getMessage}")
        logger.trace(s"Browser action crashed: $name ${exception.getMessage}")
        actionResult.actionStatus = ActionStatus(KO,Option.apply(s"crashed with ${exception.getMessage}"), isCrashed = true)
    }
    finally {
      if (actionResult.actionStatus.status == KO) actionResult.session = actionResult.session.markAsFailed
      next ! actionResult.session
    }
  }

  def clock: Clock = ctx.coreComponents.clock

}
