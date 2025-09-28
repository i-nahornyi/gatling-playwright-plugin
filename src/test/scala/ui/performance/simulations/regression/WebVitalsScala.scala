package ui.performance.simulations.regression

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.WaitUntilState.LOAD
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.custom.browser.Predef._
import io.gatling.custom.browser.model.PageLoadValidator

class WebVitalsScala extends Simulation {

  val browserProtocol: Protocol = gatlingBrowser
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))
    .enableUIMetrics()
    .buildProtocol()

  val validationScript: String = loadScript("scripts/home_page_load.js")

  val pageLoadValidator: PageLoadValidator = PageLoadValidator(validationScript, null, new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000))

  def mainScenario: ScenarioBuilder = scenario("test").repeat(5)(
    browserAction("HomePage").open("https://gatling.io/").withLoadValidations(pageLoadValidator),
    pause(1, 3),
    browserAction("Pricing").open("https://gatling.io/pricing"),
    pause(1, 3),
    browserAction("Customers").open("https://gatling.io/customers").withLoadValidations(),
    pause(1, 3),
    browserAction("How_it_works").open("https://gatling.io/how-it-works").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(LOAD)),
    browserCleanContext()
  )

  setUp(mainScenario.inject(atOnceUsers(1))).protocols(browserProtocol)

}
