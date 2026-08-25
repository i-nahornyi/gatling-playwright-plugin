package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import io.gatling.core.protocol.Protocol

case object BrowserProtocolBuilderBase {

  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withLaunchOptions(launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withContextOptions(contextOptions)
  def withDefaultTimeout(timeoutMillis: Double): BrowserProtocolBuilder = BrowserProtocolBuilder().withDefaultTimeout(timeoutMillis)
}


final case class BrowserProtocolBuilder(launchOptions: LaunchOptions = DefaultProtocolOptions.defaultProtocolOptions,
                                        contextOptions: NewContextOptions = DefaultProtocolOptions.defaultContextOptions,
                                        webVitalsEnable: Boolean = DefaultProtocolOptions.defaultWebVitalsEnable,
                                        defaultTimeout: Option[Double] = DefaultProtocolOptions.defaultTimeout) {

  implicit def buildProtocol(): Protocol = BrowserProtocol(launchOptions, contextOptions, webVitalsEnable, defaultTimeout)
  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = copy(launchOptions = launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = copy(contextOptions = contextOptions)
  def enableUIMetrics(): BrowserProtocolBuilder = copy(webVitalsEnable = true)
  def withDefaultTimeout(timeoutMillis: Double): BrowserProtocolBuilder = copy(defaultTimeout = Some(timeoutMillis))
}