package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserContext, BrowserType, Playwright}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session
import io.gatling.custom.browser.utils.Constants.BROWSER_CONTEXT_KEY

import scala.collection.concurrent.TrieMap

case class BrowserComponent(playwright: Playwright, browser: Browser, launchOptions: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions)
  extends ProtocolComponents with StrictLogging {


  var browserContextsPool: TrieMap[Long, BrowserContext] = TrieMap.empty[Long, BrowserContext]

  override def onStart: Session => Session = session => {

    val browserContext = browser.newContext(contextOptions)
    browserContextsPool.put(session.userId, browserContext)
    session.set(BROWSER_CONTEXT_KEY, browserContext.newPage())
  }

  override def onExit: Session => Unit = session => {
    val userId = session.userId
    if (browserContextsPool.contains(userId)) {
      browserContextsPool(userId).close(new BrowserContext.CloseOptions().setReason("Closing due to onExit hook"))
      browserContextsPool.remove(userId)
    }
  }
}
