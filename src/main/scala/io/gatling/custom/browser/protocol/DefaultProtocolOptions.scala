package io.gatling.custom.browser.protocol

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page.WaitForFunctionOptions

object DefaultProtocolOptions {

  var defaultProtocolOptions: LaunchOptions = new LaunchOptions().setHeadless(false)
  var defaultContextOptions: NewContextOptions = new NewContextOptions().setViewportSize(1920, 1080)

  //* UI default block
  var defaultWaitPageLoadOptions: WaitForFunctionOptions = new WaitForFunctionOptions().setPollingInterval(100).setTimeout(60000)
  var defaultResourceInactivityTime = 500
  var defaultWebVitalsEnable: Boolean = false
  //

}
