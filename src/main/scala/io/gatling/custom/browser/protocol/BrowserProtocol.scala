package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType, Playwright}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}

case class BrowserProtocol(options: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions, enableUIMetrics: Boolean) extends Protocol {
  type Component = BrowserComponent
}

object BrowserProtocol extends StrictLogging {
  val browserProtocolKey: ProtocolKey[BrowserProtocol, BrowserComponent] = new ProtocolKey[BrowserProtocol, BrowserComponent] {
    override def protocolClass: Class[Protocol] = classOf[BrowserProtocol].asInstanceOf[Class[Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): BrowserProtocol = {
      BrowserProtocol(DefaultProtocolOptions.defaultProtocolOptions, DefaultProtocolOptions.defaultContextOptions, DefaultProtocolOptions.defaultWebVitalsEnable)
    }

    override def newComponents(coreComponents: CoreComponents): BrowserProtocol => BrowserComponent = browserProtocol => {
      val playwright: Playwright = Playwright.create()
      val browserComponent = BrowserComponent(playwright, browserProtocol.options, browserProtocol.contextOptions, browserProtocol.enableUIMetrics)
      coreComponents.actorSystem.registerOnTermination({
        logger.trace("Received termination signal; closing browser and Playwright instances.")
        browserComponent.browserInstance.close(new Browser.CloseOptions().setReason("Received termination signal from Gatling."))
        playwright.close()
      })

      browserComponent
    }
  }
}
