package ui.performance.simulations;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.WaitUntilState;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.*;
import org.opentest4j.AssertionFailedError;

import java.util.Map;
import java.util.function.BiFunction;

import static io.gatling.javaapi.core.CoreDsl.*;

public class BrowserSimulationsJava extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            //// This part of setup block is optional
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080).setIsMobile(true))
            ////
            .buildProtocol();


    /// [Example#1] How to get and put your variable to gatling session
    BiFunction<Page, BrowserSession, BrowserSession> exampleFlow = (page, browserSession) -> {
        /// You can resolve EL value from session for available options see => https://docs.gatling.io/reference/script/core/session/el/
        Boolean valueFromSession = (Boolean) browserSession.resolveSessionValue("#{url.exists()}");
        System.out.println(valueFromSession);
        /// Another one example
        String anotherValueFromSession = browserSession.resolveSessionExpression(s -> s.getString("url")+"updated").toString();
        System.out.println(anotherValueFromSession);

        page.navigate("https://playwright.dev/java/docs/debug");
        Session session = browserSession.getJavaSession().set("your_args", page.title());
        return browserSession.updateBrowserSession(session);
    };


    ///// [Example#2] How to set execution timing of action based on your logic
    BiFunction<Page, BrowserSession, BrowserSession> exampleFlow2 = (page, browserSession) -> {
        page.navigate("https://playwright.dev/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));

        /// How to evaluate JS in playwrights see ===> https://playwright.dev/java/docs/evaluating
        Map<String, Double> timing = (Map<String, Double>) page.evaluate("performance.timing");

        browserSession.setActionStartTime(timing.get("navigationStart").longValue());
        browserSession.setActionEndTime(timing.get("loadEventEnd").longValue());

        return browserSession;
    };

    ///// [Example#3] How to handle errors and set status of action
    BiFunction<Page, BrowserSession, BrowserSession> exampleFlow3 = (page, browserSession) -> {
        try {
            PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText");
        } catch (AssertionFailedError e) {
            browserSession.setStatusKO("Your custom textAssertion");
        }
        return browserSession;
    };

    //// [Example#4] How to handle errors and set status of action
    BiFunction<Page, BrowserSession, BrowserSession> exampleFlow4 = (page, browserSession) -> {

        PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText");
        //// If assertion fired, next code not executed
        //// and session will be lost
        //// solution look in [Example#3]
        Session session = browserSession.getJavaSession().set("your_args", "Changed_args");

        return browserSession.updateBrowserSession(session);
    };


    ScenarioBuilder mainScenario = scenario("test").repeat(1).on(
            group("flow-a").on(
                    exec(session -> session.set("actionName", "test-action-1").set("url", "https://demo.playwright.dev/todomvc/#/")),
                    /*
                     *  You can use EL syntax for action name and url
                     */
                    exec(BrowserDsl.browserAction("#{actionName}").open("#{url}")),
                    exec(BrowserDsl.browserAction(session -> session.getString("actionName")).open(session -> session.getString("url"))),
                    pause(1, 5),
                    exec(BrowserDsl.browserAction("test-action-2").executeFlow(exampleFlow)),
                    /*
                     *  Print variable that you store in [Example#1]
                     */
                    exec(session -> {
                        System.out.println(session.getString("your_args"));
                        return session;
                    }),
                    exec(BrowserDsl.browserAction("test-action-3").executeFlow(exampleFlow2)),
                    exec(BrowserDsl.browserCleanContext()),
                    exec(BrowserDsl.browserAction("test-action-4").executeFlow(exampleFlow3)),
                    exec(BrowserDsl.browserAction("test-action-5").executeFlow(exampleFlow4)),
                    /*
                     *  Print variable that you store in [Example#4]
                     */
                    exec(session -> {
                        System.out.println(session.getString("your_args"));
                        return session;
                    }),
                    /*
                     *  Clear browserContext on the end of loop
                     */
                    exec(BrowserDsl.browserCleanContext())
            )
    );


    {
        setUp(mainScenario.injectOpen(OpenInjectionStep.atOnceUsers(1)).protocols(browserProtocol));
    }


}
