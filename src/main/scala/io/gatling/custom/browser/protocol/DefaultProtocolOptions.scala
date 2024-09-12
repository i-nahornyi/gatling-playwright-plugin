package io.gatling.custom.browser.protocol

import com.microsoft.playwright.{Browser, BrowserType}

object DefaultProtocolOptions {

  var defaultProtocolOptions: BrowserType.LaunchOptions = new BrowserType.LaunchOptions().setHeadless(false)
  var defaultContextOptions: Browser.NewContextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080)

}
