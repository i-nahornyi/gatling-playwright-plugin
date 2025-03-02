package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType, Playwright}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

import scala.collection.concurrent.TrieMap

case class BrowserComponent(browserInstances: TrieMap[Long, Browser], launchOptions: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions)
  extends ProtocolComponents with StrictLogging{


  private val MAX_RETRIES = 3
  private val RETRY_DELAY_MS = 1000 // 1 second

  override def onStart: Session => Session = session => {

    val browser: Browser = launchBrowserWithRetry(session.userId, launchOptions)
    browserInstances.put(session.userId, browser)
    logger.debug(s"browser with userID - ${session.userId} added to browser pool")

    session
  }

  private def launchBrowserWithRetry(userId: Long, launchOptions: BrowserType.LaunchOptions): Browser = {
    retry(MAX_RETRIES) {
      val playwright = createPlaywrightWithRetry()
      val browser = playwright.chromium().launch(launchOptions)
      browserInstances.put(userId, browser)
      browser
    }
  }

  private def createPlaywrightWithRetry(): Playwright = {
    retry(MAX_RETRIES) {
      Playwright.create()
    }
  }

  private def retry[T](maxRetries: Int)(block: => T): T = {
    var attempt = 0
    var lastError: Throwable = null

    while (attempt < maxRetries) {
      attempt += 1
      try {
        return block
      } catch {
        case e: Throwable =>
          lastError = e
          logger.debug(s"Attempt $attempt of $maxRetries failed: ${e.getMessage}")
          if (attempt < maxRetries) {
            Thread.sleep(RETRY_DELAY_MS)
          }
      }
    }
    throw new RuntimeException(s"Failed after $maxRetries attempts", lastError)
  }

  override def onExit: Session => Unit = session => {
    val userId = session.userId
    browserInstances(userId).close()
    logger.debug(s"browser with userID - ${session.userId} closed - ${browserInstances(session.userId).isConnected}")
    browserInstances.remove(userId)
    logger.debug(s"browser with userID - ${session.userId} removed from browser pool")
    ProtocolComponents.NoopOnExit
  }
}
