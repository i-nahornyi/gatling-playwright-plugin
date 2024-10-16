package ui.performance.simulations

import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType.LaunchOptions
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions
import com.microsoft.playwright.options.WaitUntilState
import io.gatling.core.Predef._
import io.gatling.core.protocol.Protocol
import io.gatling.core.session.Session
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.custom.browser.Predef._
import io.gatling.custom.browser.model.BrowserSession
import org.opentest4j.AssertionFailedError

import scala.jdk.CollectionConverters.MapHasAsScala

class BrowserSimulationsScala extends Simulation {

  val browserProtocol: Protocol = gatlingBrowser
    //// This part of setup block is optional
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))
    ////
    .build()

  /// [Example#1] How to get and put your variable to gatling session
  def exampleFlow(page: Page, browserSession: BrowserSession): BrowserSession = {

    /// You can resolve EL value from session for available options see => https://docs.gatling.io/reference/script/core/session/el/
    val valueFromSession = browserSession.resolveSessionValue("#{url.exists()}")
    println(valueFromSession)
    /// Another one example
    val modifiedValueFromSession = browserSession.resolveSessionValue(session => s"${session("url").as[String]}updated")
    println(modifiedValueFromSession)

    page.navigate("https://playwright.dev/java/docs/debug")

    val session: Session = browserSession.getScalaSession().set("your_args", page.title())
    browserSession.updateBrowserSession(session)
  }

  ///// [Example#2] How to set execution timing of action based on your logic
  def exampleFlow2(page: Page, browserSession: BrowserSession): BrowserSession = {
    page.navigate("https://playwright.dev/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))

    /// How to evaluate JS in playwrights see ===> https://playwright.dev/java/docs/evaluating
    def timing: Map[String, Double] = page.evaluate("performance.timing").asInstanceOf[java.util.Map[String, Double]].asScala.toMap

    browserSession.setActionStartTime(timing("navigationStart").asInstanceOf[Long])
    browserSession.setActionEndTime(timing("loadEventEnd").asInstanceOf[Long])

    browserSession
  }

  ///// [Example#3] How to handle errors and set status of action
  def exampleFlow3(page: Page, browserSession: BrowserSession): BrowserSession = {
    try {
      PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText")
    }
    catch {
      case _: AssertionFailedError => browserSession.setStatusKO("Your custom textAssertion")
    }
    browserSession
  }

  //// [Example#4] How to handle errors and set status of action
  def exampleFlow4(page: Page, browserSession: BrowserSession): BrowserSession = {

    PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText")
    //// If assertion fired, next code not executed
    //// and session will be lost
    //// solution look in [Example#3]
    val session: Session = browserSession.getScalaSession().set("your_args", "Changed_args")

    browserSession.updateBrowserSession(session)
  }

  def mainScenario: ScenarioBuilder = scenario("test").repeat(1) {
    group("flow-a")(
      exec(session => session.set("actionName", "test-action-1")
        .set("url", "https://demo.playwright.dev/todomvc/#/")
        .set("test_boolean",true)),
      /*
       *  You can use EL syntax for action name and url
      */
      exec(browserAction("#{actionName}").open("#{url}")),
      pause(1, 5),
      exec(browserAction(session => session("actionName").as[String]).open(session => session("url").as[String])),
      pause(1, 5),
      exec(browserAction("test-action-2").executeFlow(exampleFlow)),
      /*
      *  Print variable that you store in [Example#1]
      */
      exec(session => {
        println(session("your_args").as[String])
        session
      }),
      exec(browserAction("test-action-3").executeFlow(exampleFlow2)),
      exec(browserAction("test-action-4").executeFlow(exampleFlow3)),
      exec(browserAction("test-action-5").executeFlow(exampleFlow4)),
        /*
        *  Print variable that you store in [Example#4]
        */
      exec(session => {
        println(session("your_args").as[String])
        session
      }),
      /*
      *  Clear browserContext on the end of loop
      */
      exec(browserCleanContext())
    )
  }

  setUp(mainScenario.inject(atOnceUsers(1))).protocols(browserProtocol)

}
