package ui.performance.simulations;

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


    BiFunction<Page, BrowserSession, BrowserSession> scriptedAction = (page, browserSession) -> {
        Boolean valueFromSession = (Boolean) browserSession.resolveSessionValue("#{link.exists()}");

        System.out.println("ActualValue => " + valueFromSession + " | ExpectedValue => true");

        Session session = browserSession.getJavaSession().set("user_defined_args", "user_defined_value");

        long currentTime = System.currentTimeMillis();

        browserSession.setActionStartTime(currentTime);
        browserSession.setActionEndTime(currentTime + 200);

        System.out.println("ActualValue => " + 200 + " | ExpectedValue => " + (browserSession.getActionEndTime() - browserSession.getActionStartTime()) );


        browserSession.setStatusKO("your error message");

        System.out.println("ActualValue => " + browserSession.getErrorMessage().get() + " | ExpectedValue => your error message");

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
                        System.out.println("ActualValue => " + actualValue + " | Expected => user_defined_value");
                        return session;
                    }),
                    BrowserDsl.browserCleanContext(),
                    exitHere()
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1)));
    }


}
