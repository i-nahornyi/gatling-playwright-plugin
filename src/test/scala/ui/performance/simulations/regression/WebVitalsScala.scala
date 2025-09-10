package ui.performance.simulations.regression

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.custom.browser.Predef._

class WebVitalsScala extends Simulation {

  val browserProtocol: Protocol = gatlingBrowser
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))

    .enableUIMetrics()
    .buildProtocol()

  def mainScenario: ScenarioBuilder = scenario("test").repeat(5)(
    exec(browserAction("homePage").open("https://ecomm.gatling.io/", new Page.NavigateOptions().setWaitUntil(NETWORKIDLE))),
    pause(1,3),
    exec(browserCleanContext())
  )

  setUp(mainScenario.inject(atOnceUsers(1))).protocols(browserProtocol)

}
