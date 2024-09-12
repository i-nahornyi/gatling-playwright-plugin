package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType, Playwright}
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

case class BrowserProtocol(options: BrowserType.LaunchOptions,contextOptions: Browser.NewContextOptions) extends Protocol {
  type Component = BrowserComponent
}

object BrowserProtocol {
  val browserProtocolKey: ProtocolKey[BrowserProtocol,BrowserComponent] = new ProtocolKey[BrowserProtocol,BrowserComponent] {
    override def protocolClass: Class[Protocol] = classOf[BrowserProtocol].asInstanceOf[Class[Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): BrowserProtocol = {
      BrowserProtocol(DefaultProtocolOptions.defaultProtocolOptions,DefaultProtocolOptions.defaultContextOptions)
    }

    override def newComponents(coreComponents: CoreComponents): BrowserProtocol => BrowserComponent = browserProtocol => {
      val playwright = Playwright.create()
      val browser = playwright.chromium.launch(browserProtocol.options)

      BrowserComponent(browser,browserProtocol.contextOptions)
    }
  }
}
