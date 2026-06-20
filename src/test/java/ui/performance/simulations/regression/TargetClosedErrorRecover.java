package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class TargetClosedErrorRecover extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            //// This part of setup block is optional
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080))
            ////
            .buildProtocol();

    ScenarioBuilder mainScenario = scenario("test")
            .exec(
                    BrowserDsl.browserSessionFunction((page, browserSession) -> browserSession.updateBrowserSession(browserSession.getGatlingSession().set("testValue", "testValue"))),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    pause(1),
                    BrowserDsl.browserAction("FailedAction").executeFlow((page, browserSession) -> {
                        page.context().browser().close();
                        page.check("#main-content");
                        return browserSession;
                    }),
                    pause(1),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    BrowserDsl.browserAction("FailedAction").executeFlow((page, browserSession) -> {
                        page.context().close();
                        page.check("#main-content");
                        return browserSession;
                    }),
                    pause(1),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    BrowserDsl.browserAction("FailedAction").executeFlow((page, browserSession) -> {
                        page.close();
                        page.check("#main-content");
                        return browserSession;
                    }),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    crashLoadGeneratorIf(session -> "testValue lost", session -> !session.contains("testValue"))
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).assertions(
                global().successfulRequests().count().is(4L),
                global().failedRequests().count().is(3L)
        );
    }
}
