package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static io.gatling.javaapi.core.CoreDsl.constantConcurrentUsers;

@SuppressWarnings("unused")
public class NewMultiThreading extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            //// This part of setup block is optional
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080))
            .enableUIMetrics()
            ////
            .buildProtocol();



    ScenarioBuilder scenarioBuilder = CoreDsl.scenario("multi").repeat(2).on(
                    CoreDsl.pause(2,5),
                    BrowserDsl.browserAction("home").open("https://ecomm.gatling.io/").withLoadValidations(),
                    CoreDsl.pause(2,5),
                    BrowserDsl.browserCleanContext()
    );


    {
        setUp(scenarioBuilder.injectClosed(constantConcurrentUsers(3).during(60))).protocols(browserProtocol);
    }


}
