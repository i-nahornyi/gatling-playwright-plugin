package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import io.gatling.core.protocol.Protocol

import scala.annotation.nowarn

case object BrowserProtocolBuilderBase {

  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withLaunchOptions(launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withContextOptions(contextOptions)
  def withDefaultTimeout(timeoutMillis: Double): BrowserProtocolBuilder = BrowserProtocolBuilder().withDefaultTimeout(timeoutMillis)
}


final case class BrowserProtocolBuilder(launchOptions: LaunchOptions = DefaultProtocolOptions.defaultProtocolOptions,
                                        contextOptions: NewContextOptions = DefaultProtocolOptions.defaultContextOptions,
                                        webVitalsEnable: Boolean = DefaultProtocolOptions.defaultWebVitalsEnable,
                                        defaultTimeout: Option[Double] = DefaultProtocolOptions.defaultTimeout) {

  @deprecated("No longer necessary to call explicitly — a BrowserProtocolBuilder now implicitly converts to a Protocol wherever one is expected (e.g. .protocols(...))", "0.1.5")
  def buildProtocol(): Protocol = BrowserProtocol(launchOptions, contextOptions, webVitalsEnable, defaultTimeout)
  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = copy(launchOptions = launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = copy(contextOptions = contextOptions)
  def enableUIMetrics(): BrowserProtocolBuilder = copy(webVitalsEnable = true)
  def withDefaultTimeout(timeoutMillis: Double): BrowserProtocolBuilder = copy(defaultTimeout = Some(timeoutMillis))
}

object BrowserProtocolBuilder {
  // Lets a bare BrowserProtocolBuilder be passed anywhere a Protocol is expected (e.g. .protocols(...))
  // without requiring an explicit .buildProtocol() call, without conflating the conversion with the
  // public buildProtocol() method itself.
  @nowarn("cat=deprecation")
  implicit def toProtocol(builder: BrowserProtocolBuilder): Protocol = builder.buildProtocol()
}