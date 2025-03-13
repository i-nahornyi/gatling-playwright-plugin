package ui.performance.simulations.regression;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.util.function.BiFunction;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class FailedSimulation extends Simulation {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FailedSimulation.class);
    BiFunction<Page, BrowserSession, BrowserSession> actionMarkedIsKO = (page, browserSession) -> {
        browserSession.setStatusKO("status KO with error message set by user");
        return browserSession;
    };


    BiFunction<Page, BrowserSession, BrowserSession> throwAssertionFailedErrorWithExpected = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            PlaywrightAssertions.assertThat(page.locator("//*[@id=\"__docusaurus\"]/nav")).isDisabled();
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwAssertionFailedError = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            PlaywrightAssertions.assertThat(page).hasTitle("Error expected title");
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwTimeoutError = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            page.check("#locatorThatNotExist");
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwTimeoutError2 = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            page.locator("//*[@id=\"__docusaurus\"]/nav").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwDriverException = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            page.evaluate("var e = badJsExpression");
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwDriverException2 = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.navigate("https://playwright.dev/");
            page.evaluate("return badJsExpression");
        }
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> throwClosedError = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            page.close();
            page.navigate("https://playwright.dev/");
        }
        return browserSession;
    };



    BiFunction<Page, BrowserSession, BrowserSession> crashAction = (page, browserSession) -> {

        if (!browserSession.getJavaSession().isFailed()) {
            throw new IllegalArgumentException("My text of exception");
        }
        return browserSession;
    };

    ScenarioBuilder mainScenario = scenario("test")
            .exec(
                    pause(1),
                    BrowserDsl.browserAction("#{variable_that_not_defined}").open("https://docs.gatling1.io/"),
                    exec(session -> {
                        log.warn("variable_that_not_defined");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    pause(1),
                    BrowserDsl.browserAction("throwDriverException").open("https://docs.gatling1.io/"),
                    pause(1),
                    exec(session -> {
                        log.warn("LinkFailedOpen");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),

                    BrowserDsl.browserAction("actionMarkedIsKO").executeFlow(actionMarkedIsKO),
                    exec(session -> {
                        log.warn("actionMarkedIsKO");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwAssertionFailedError").executeFlow(throwAssertionFailedError),
                    exec(session -> {
                        log.warn("throwAssertionFailedError");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwAssertionFailedError").executeFlow(throwAssertionFailedErrorWithExpected),
                    exec(session -> {
                        log.warn("throwAssertionFailedError");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwTimeoutError").executeFlow(throwTimeoutError),
                    exec(session -> {
                        log.warn("throwTimeoutError");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwTimeoutError").executeFlow(throwTimeoutError2),
                    exec(session -> {
                        log.warn("throwTimeoutError");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        /// Reset session
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwDriverException").executeFlow(throwDriverException),
                    exec(session -> {
                        log.warn("throwDriverException");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwDriverException").executeFlow(throwDriverException2),
                    exec(session -> {
                        log.warn("throwDriverException");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("throwClosedError").executeFlow(throwClosedError),
                    exec(session -> {
                        log.warn("throwClosedError");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        return session.markAsSucceeded();
                    }),
                    BrowserDsl.browserAction("crashAction").executeFlow(crashAction),
                    exec(session -> {
                        log.warn("crashAction");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        return session;
                    }),
                    exitHereIfFailed(),
                    exec(session -> {
                        log.warn("This block shouldn't execute");
                        return session;
                    })
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).assertions(
                global().successfulRequests().count().is(0L),
                global().failedRequests().count().is(9L)
        );
    }
}
