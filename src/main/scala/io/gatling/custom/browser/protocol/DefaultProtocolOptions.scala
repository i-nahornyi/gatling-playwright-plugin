package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions

object DefaultProtocolOptions {

  var defaultProtocolOptions: LaunchOptions = new LaunchOptions().setHeadless(false)
  var defaultContextOptions: NewContextOptions = new NewContextOptions().setViewportSize(1920, 1080)

}
