package ui.performance.simulations.regression;

import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.*;

import java.util.function.BiFunction;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class Smoke extends Simulation {

    FeederBuilder<String> pageFeeder = csv("feeders/page.csv").circular();
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Smoke.class);


    BiFunction<Page, BrowserSession, BrowserSession> scriptedAction = (page, browserSession) -> {
        Boolean valueFromSession = (Boolean) browserSession.resolveSessionValue("#{link.exists()}");

        log.warn("valueFromSession");
        log.warn("ActualValue => {} | Expected => {}",valueFromSession,true);

        Session session = browserSession.getJavaSession().set("user_defined_args", "user_defined_value");

        long currentTime = System.currentTimeMillis();

        browserSession.setActionStartTime(currentTime);
        browserSession.setActionEndTime(currentTime + 200);

        log.warn("setActionDuration");
        log.warn("ActualValue => {} | Expected => {}",200,(browserSession.getActionEndTime() - browserSession.getActionStartTime()));


        String errorMessage = "User defined error message";
        browserSession.setStatusKO(errorMessage);

        log.warn("setStatusKoWithMessage");
        log.warn("ActualValue => {} | Expected => {}",browserSession.getErrorMessage().get(), errorMessage);


        return browserSession.updateBrowserSession(session);
    };

    ScenarioBuilder mainScenario = scenario("test")
            .feed(pageFeeder)
            .exec(
                    pause(3),
                    BrowserDsl.browserAction("#{name}_1").open("#{link}"),
                    BrowserDsl.browserAction("#{name}_2").open("#{link2}", new Page.NavigateOptions().setWaitUntil(NETWORKIDLE)),
                    BrowserDsl.browserAction("#{name}_3").executeFlow(scriptedAction),
                    exec(session -> {
                        String actualValue = session.getString("user_defined_args");
                        log.warn("scriptedAction");
                        log.warn("ActualValue => {} | Expected => {}",actualValue,"user_defined_value");
                        log.warn("sessionShouldBeFailed");
                        log.warn("ActualValue => {} | ExpectedValue => {}", session.isFailed(), true);
                        return session;
                    }),
                    BrowserDsl.browserCleanContext(),
                    exitHereIfFailed(),
                    exec(session -> {
                        log.error("This block shouldn't execute");
                        return session;
                    })
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1)));
    }


}
