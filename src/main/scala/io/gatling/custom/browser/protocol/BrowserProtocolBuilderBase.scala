package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import io.gatling.core.protocol.Protocol

case object BrowserProtocolBuilderBase {

  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withLaunchOptions(launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = BrowserProtocolBuilder().withContextOptions(contextOptions)
}


final case class BrowserProtocolBuilder(launchOptions: LaunchOptions = DefaultProtocolOptions.defaultProtocolOptions,
                                        contextOptions: NewContextOptions = DefaultProtocolOptions.defaultContextOptions) {

  implicit def build(): Protocol = BrowserProtocol(launchOptions, contextOptions)
  def withLaunchOptions(launchOptions: LaunchOptions): BrowserProtocolBuilder = copy(launchOptions = launchOptions)
  def withContextOptions(contextOptions: NewContextOptions): BrowserProtocolBuilder = copy(contextOptions = contextOptions)

}