package ui.performance.simulations.regression;

import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.javaapi.model.BrowserSession;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;

import java.util.function.BiFunction;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unused")
public class Smoke extends Simulation {

    FeederBuilder<String> pageFeeder = csv("feeders/page.csv").circular();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Smoke.class);


    BiFunction<Page, BrowserSession, BrowserSession> scriptedAction = (page, browserSession) -> {

        log.info("executed -> scriptedAction");

        Boolean valueFromSession = (Boolean) browserSession.resolveSessionValue("#{link.exists()}");
        assertEquals("Value from session not resolved",true, valueFromSession);

        Session session = browserSession.getGatlingSession().set("user_defined_args", "user_defined_value");

        long currentTime = System.currentTimeMillis();

        browserSession.setActionStartTime(currentTime);
        browserSession.setActionEndTime(currentTime + 200);

        assertEquals(200, browserSession.getActionEndTime() - browserSession.getActionStartTime());

        String errorMessage = "User defined error message";
        browserSession.setStatusKO(errorMessage);
        assertEquals("User defined error message not set", browserSession.getErrorMessage().get(), errorMessage);

        browserSession = browserSession.updateBrowserSession(session);
        return browserSession;
    };

    BiFunction<Page, BrowserSession, BrowserSession> exampleBrowserSessionFunction = (page, browserSession) -> {
        page.reload();
        Session session = browserSession.getGatlingSession().set("your_args", "Changed_args").set("pageTitle", page.title());
        return browserSession.updateBrowserSession(session);
    };

    ScenarioBuilder mainScenario = scenario("test")
            .feed(pageFeeder)
            .exec(
                    pause(3),
                    BrowserDsl.browserAction("#{name}_1").open("#{link}"),
                    BrowserDsl.browserAction("#{name}_2").open("#{link2}").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(NETWORKIDLE)),
                    BrowserDsl.browserSessionFunction(exampleBrowserSessionFunction),
                    BrowserDsl.browserAction("#{name}_3").executeFlow(scriptedAction),
                    exec(session -> {
                        String actualValue = session.getString("user_defined_args");
                        assertEquals("User defined message doesn't set","user_defined_value", actualValue);
                        assertTrue("Session should fail",session.isFailed());
                        return session;
                    }),
                    BrowserDsl.browserCleanContext(),
                    exitHereIfFailed(),
                    exec(session -> {
                        throw new IllegalStateException();
                    })
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1)));
    }


}
