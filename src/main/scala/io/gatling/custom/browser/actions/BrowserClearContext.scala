package io.gatling.custom.browser.actions

import io.gatling.core.action.{Action, ChainableAction}
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen
import io.gatling.custom.browser.actor.{BrowserCommandResponse, BrowserWorkerActor}
import io.gatling.custom.browser.utils.Constants

import scala.concurrent.Await

case class BrowserClearContext(ctx: ScenarioContext, next: Action) extends ChainableAction with NameGen with BrowserActionsBase {

  override def statsEngine: StatsEngine = ctx.coreComponents.statsEngine

  override def name: String = genName("clearBrowserContext")

  override protected def execute(session: Session): Unit = {

    val userId = session.userId
    logger.trace(s"userID-$userId, execute ClearContext action")

    if(browserActorPool.existActor(userId)){

      val browserActorWorker = browserActorPool.getActorById(userId)
      val promise = browserActorWorker.replyPromise[Unit](Constants.PROMISE_TIMEOUT)

      browserActorWorker ! BrowserWorkerActor.ClosePage(promise)
      Await.result(promise.future, Constants.PROMISE_TIMEOUT)
      logger.trace(s"userID-$userId, remove BrowserContext from BrowserWorkerActor")
    }
    next ! session
  }
}
