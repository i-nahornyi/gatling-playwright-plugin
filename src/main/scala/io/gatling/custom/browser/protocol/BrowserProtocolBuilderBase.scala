package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType}
import io.gatling.core.protocol.Protocol

case object BrowserProtocolBuilderBase {

  def withLaunchOptions(launchOptions: BrowserType.LaunchOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withLaunchOptions(launchOptions)
  def withContextOptions(contextOptions: Browser.NewContextOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withContextOptions(contextOptions)
}


final case class BrowserProtocolBuilder(launchOptions: BrowserType.LaunchOptions = DefaultProtocolOptions.defaultProtocolOptions,
                                        contextOptions: Browser.NewContextOptions = DefaultProtocolOptions.defaultContextOptions) {

  implicit def build(): Protocol = BrowserProtocol(launchOptions, contextOptions)
  def withLaunchOptions(launchOptions: BrowserType.LaunchOptions): BrowserProtocolBuilder = copy(launchOptions = launchOptions)
  def withContextOptions(contextOptions: Browser.NewContextOptions): BrowserProtocolBuilder = copy(contextOptions = contextOptions)

}