package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.PageLoadValidator;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import static com.microsoft.playwright.options.WaitUntilState.LOAD;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class WebVitalsJava extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080))
            .enableUIMetrics()
            .buildProtocol();


    String validationScript = BrowserDsl.loadScript("scripts/home_page_load.js");
    PageLoadValidator pageLoadValidator = new PageLoadValidator(
            validationScript,
            null,
            new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000)
    );

    ScenarioBuilder mainScenario = scenario("test").repeat(5).on(
            BrowserDsl.browserAction("HomePage").open("https://gatling.io/").withLoadValidations(pageLoadValidator),
            pause(1,3),
            BrowserDsl.browserAction("Pricing").open("https://gatling.io/pricing"),
            pause(1,3),
            BrowserDsl.browserAction("Customers").open("https://gatling.io/customers").withLoadValidations(),
            pause(1,3),
            BrowserDsl.browserAction("How_it_works").open("https://gatling.io/how-it-works").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(LOAD)),
            pause(1,3),
            BrowserDsl.browserCleanContext()
    );

    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }
}
