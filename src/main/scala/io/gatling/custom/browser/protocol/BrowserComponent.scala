package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser
import io.gatling.core.protocol.ProtocolComponents
import io.gatling.core.session.Session

case class BrowserComponent(browser: Browser, contextOptions: Browser.NewContextOptions) extends ProtocolComponents {

  override def onStart: Session => Session = Session.Identity
  override def onExit: Session => Unit = {
    if (browser.isConnected) browser.close()
    ProtocolComponents.NoopOnExit
  }
}
