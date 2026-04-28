package ui.performance.simulations.regression;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.custom.browser.model.PageLoadValidator;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

import java.util.Collections;
import java.util.function.BiFunction;

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


    String customValidationScript = BrowserDsl.loadScript("scripts/home_page_load.js");
    PageLoadValidator customPageLoadValidator = new PageLoadValidator(
            customValidationScript,
            null,
            new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000)
    );

    BiFunction<Page, BrowserSession, BrowserSession> setupSession = (page, browserSession) -> {
        page.context().addCookies(Collections.singletonList(
                new Cookie("__hs_cookie_cat_pref", "1:true_2:true_3:true").setDomain("gatling.io").setPath("/"))
        );
        return browserSession;
    };

    ChainBuilder THINK_TIME = pause(1,3);


    ScenarioBuilder mainScenario = scenario("test").repeat(5).on(
            BrowserDsl.browserSessionFunction(setupSession),
            BrowserDsl.browserAction("HomePage").open("https://gatling.io/").withLoadValidations(customPageLoadValidator),
            THINK_TIME,
            BrowserDsl.browserAction("Pricing").open("https://gatling.io/pricing"),
            THINK_TIME,
            BrowserDsl.browserAction("Customers").open("https://gatling.io/customers").withLoadValidations(),
            THINK_TIME,
            BrowserDsl.browserAction("How_it_works").open("https://gatling.io/how-it-works").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(LOAD)),
            THINK_TIME,
            BrowserDsl.browserCleanContext()
    );

    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }
}
