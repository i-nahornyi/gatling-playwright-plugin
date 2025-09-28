package ui.performance.simulations.regression;


import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class SyntheticMonitoring extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080).setBypassCSP(true)
            )
            .enableUIMetrics()
            .buildProtocol();




    ScenarioBuilder mainScenario = scenario("synthetic_monitoring")
            .repeat(3).on(
                    BrowserDsl.browserAction("home").open("https://github.com/i-nahornyi/gatling-playwright-plugin").withLoadValidations(),
                    pause(1),
                    BrowserDsl.browserCleanContext()
            );


    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }

}
