package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.util.function.BiFunction;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class ClosedPageRecover extends Simulation {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FailedSimulation.class);

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            //// This part of setup block is optional
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080))
            ////
            .buildProtocol();

    BiFunction<Page, BrowserSession, BrowserSession> closePage = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.close();
            page.check("#locatorThatNotExist");
        }
        return browserSession;
    };

    ScenarioBuilder mainScenario = scenario("test")
            .exec(
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    pause(1),
                    exec(session -> session.set("testValue","testValue")),
                    BrowserDsl.browserAction("FailedAction").executeFlow((page, browserSession) -> {
                        page.close();
                        page.check("#main-content");
                        return browserSession;
                    }),
                    pause(1),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    BrowserDsl.browserAction("FailedAction").executeFlow((page, browserSession) -> {
                        page.context().browser().close();
                        page.check("#main-content");
                        return browserSession;
                    }),
                    BrowserDsl.browserAction("SuccessAction").open("https://gatling.io/"),
                    exec(session -> {
                        log.warn(session.getString("testValue"));
                        return session;
                    }),
                    crashLoadGeneratorIf(session -> "testValue lost", session -> !session.contains("testValue"))
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).assertions(
                global().successfulRequests().count().is(3L),
                global().failedRequests().count().is(2L)
        );
    }
}
