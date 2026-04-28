package ui.performance.simulations.regression

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.Cookie
import com.microsoft.playwright.options.WaitUntilState.LOAD
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import io.gatling.custom.browser.Predef._
import io.gatling.custom.browser.model.{BrowserSession, PageLoadValidator}

import java.util.Collections

class WebVitalsScala extends Simulation {

  val browserProtocol: Protocol = gatlingBrowser
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))
    .enableUIMetrics()
    .buildProtocol()

  val customValidationScript: String = loadScript("scripts/home_page_load.js")

  val customPageLoadValidator: PageLoadValidator = PageLoadValidator(customValidationScript,
    null,
    new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000)
  )

  def setupSession(page: Page, browserSession: BrowserSession): BrowserSession = {
    page.context.addCookies(Collections.singletonList(
      new Cookie("__hs_cookie_cat_pref", "1:true_2:true_3:true").setDomain("gatling.io").setPath("/"))
    )
    browserSession
  }

  val THINK_TIME: ChainBuilder = pause(1, 3)

  def mainScenario: ScenarioBuilder = scenario("test").repeat(5)(
    browserSessionFunction(setupSession),
    browserAction("HomePage").open("https://gatling.io/").withLoadValidations(customPageLoadValidator),
    THINK_TIME,
    browserAction("Pricing").open("https://gatling.io/pricing"),
    THINK_TIME,
    browserAction("Customers").open("https://gatling.io/customers").withLoadValidations(),
    THINK_TIME,
    browserAction("How_it_works").open("https://gatling.io/how-it-works").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(LOAD)),
    browserCleanContext()
  )

  setUp(mainScenario.inject(atOnceUsers(1))).protocols(browserProtocol)

}
