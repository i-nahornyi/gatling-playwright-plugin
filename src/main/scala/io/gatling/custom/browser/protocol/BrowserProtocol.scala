package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.CoreComponents
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol.{Protocol, ProtocolKey}
import io.gatling.custom.browser.actor.BrowserActorPool
import io.gatling.custom.browser.stats.UIMetricFileWriter
import io.gatling.custom.browser.utils.Constants

case class BrowserProtocol(options: BrowserType.LaunchOptions, contextOptions: Browser.NewContextOptions, enableUIMetrics: Boolean, defaultTimeout: Option[Double] = None) extends Protocol {
  type Component = BrowserComponent
}

object BrowserProtocol extends StrictLogging {
  val browserProtocolKey: ProtocolKey[BrowserProtocol, BrowserComponent] = new ProtocolKey[BrowserProtocol, BrowserComponent] {
    override def protocolClass: Class[Protocol] = classOf[BrowserProtocol].asInstanceOf[Class[Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): BrowserProtocol = {
      BrowserProtocol(DefaultProtocolOptions.defaultProtocolOptions, DefaultProtocolOptions.defaultContextOptions, DefaultProtocolOptions.defaultWebVitalsEnable, DefaultProtocolOptions.defaultTimeout)
    }

    override def newComponents(coreComponents: CoreComponents): BrowserProtocol => BrowserComponent = browserProtocol => {
      val browserActorPool = new BrowserActorPool(coreComponents.actorSystem, Constants.BROWSER_ACTOR_POOL_NAME, browserProtocol.options, browserProtocol.contextOptions, browserProtocol.defaultTimeout)
      val browserComponent = BrowserComponent(browserProtocol.enableUIMetrics, browserActorPool)

      coreComponents.actorSystem.registerOnTermination({
        logger.trace("Received termination signal; closing BrowserWorkerActor instances.")
        browserActorPool.terminatePool("Received termination signal from Gatling.")
        UIMetricFileWriter.stop()
      })
      browserComponent
    }
  }
}
