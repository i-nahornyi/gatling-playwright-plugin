package io.gatling.custom.browser.actor

import com.microsoft.playwright.{Browser, BrowserContext, BrowserType, Page}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.actor.{ActorRef, ActorSystem}
import io.gatling.core.session.Session
import io.gatling.custom.browser.utils.Constants

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class BrowserActorPool(system: ActorSystem, baseName: String, launchOptions: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions, defaultTimeout: Option[Double] = None) extends StrictLogging {


  private val actorMap = new ConcurrentHashMap[Long, ActorRef[BrowserWorkerActor.BrowserWorkerCommand]]().asScala
  private val timeout = Constants.PROMISE_TIMEOUT
  private val terminateTimeout = 20.seconds


  def createActor(actorId: Long): ActorRef[BrowserWorkerActor.BrowserWorkerCommand] = actorMap.getOrElseUpdate(actorId, {
    val actorName = s"$baseName-$actorId"
    val actor = new BrowserWorkerActor(actorName, launchOptions: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions, defaultTimeout)
    actor.init()
    val actorRef = system.actorOf(actor)
    val promise = actorRef.replyPromise[Unit](timeout)
    actorRef ! BrowserWorkerActor.InitBrowserWorker(promise)
    Await.result(promise.future, timeout)
    actorRef
  })

  def terminatePool(reason: String): Unit = {
    val promisesList = actorMap.map {
      actor =>
        val promise = actor._2.replyPromise[Unit](terminateTimeout)
        actor._2 ! BrowserWorkerActor.StopBrowserWorker(reason, promise)
        promise.future
    }
    implicit val executionContext: ExecutionContext = system.executionContext
    Future.sequence(promisesList).onComplete(_ => logger.trace("All browser actor stopped"))
  }

  def stopActorById(actorId: Long, reason: String): Unit = {
    actorMap.remove(actorId).foreach(actor => {
      val promise = actor.replyPromise[Unit](terminateTimeout)
      actor ! BrowserWorkerActor.StopBrowserWorker(reason, promise)
      Await.result(promise.future, terminateTimeout)
    })
  }

  def loadPageInstance(session: Session) : Unit = {

    val userID = session.userId

    if (existActor(userID)) {

      val browserContext = getBrowserContext(userID)

      /// Check is browser open
      if (!browserContext.browser().isConnected) {
        logger.trace(s"browserActorPool contains BrowserContext for userID-$userID, but browser was closed, create new instance")
        recreateBrowser(userID)
      }
      /// Check is context have active page
      if (!(browserContext.pages().size() > 0)) {
        logger.trace(s"browserActorPool contains BrowserContext for userID-$userID, but page was closed, create new instance")
        createNewPage(userID)
        return
      }

      logger.trace(s"browserActorPool contains BrowserContext for userID-$userID")
    }
    else {
      logger.trace(s"browserActorPool doesn't contains BrowserContext for userID-$userID, create new")
      createActor(userID)
      createNewPage(userID)
    }



  }

  private def getBrowserContext(actorId: Long): BrowserContext = {
    val actor = getActorById(actorId)
    val promise = actor.replyPromise[BrowserContext](timeout)
    actor ! BrowserWorkerActor.GetBrowserContext(promise)
    Await.result(promise.future, timeout)
  }

  private def recreateBrowser(actorId: Long): Unit = {
    val actor = getActorById(actorId)
    val promise = actor.replyPromise[Browser](timeout)
    actor ! BrowserWorkerActor.RecreateBrowser(promise)
    Await.result(promise.future, timeout)
  }

  private def createNewPage(actorId: Long): Page = {
    val actor = getActorById(actorId)
    val promise = actor.replyPromise[Page](timeout)
    actor ! BrowserWorkerActor.CreateNewPage(promise)
    Await.result(promise.future, timeout)
  }


  def getActorById(actorId: Long): ActorRef[BrowserWorkerActor.BrowserWorkerCommand] = {
    actorMap(actorId)
  }

  def existActor(actorId: Long): Boolean = {
    actorMap.contains(actorId)
  }
}
