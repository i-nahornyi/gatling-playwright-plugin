package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class WebVitalsJava extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(true))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080).setIsMobile(true))
            .enableUIMetrics()
            .buildProtocol();

    ScenarioBuilder mainScenario = scenario("test").repeat(5).on(
            exec(BrowserDsl.browserAction("homePage").open("https://ecomm.gatling.io/", new Page.NavigateOptions().setWaitUntil(NETWORKIDLE))),
            pause(1),
            exec(BrowserDsl.browserCleanContext())
    );

    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }
}
