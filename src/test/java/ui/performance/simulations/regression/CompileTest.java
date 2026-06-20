package ui.performance.simulations.regression;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.custom.browser.javaapi.protocol.BrowserProtocolBuilder;
import io.gatling.custom.browser.model.PageLoadValidator;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static io.gatling.javaapi.core.CoreDsl.*;

@SuppressWarnings("unused")
public class CompileTest extends Simulation {

    BrowserProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080).setIsMobile(false))
            .enableUIMetrics()
            .buildProtocol();

    String customValidationScript = BrowserDsl.loadScript("scripts/home_page_load.js");
    PageLoadValidator customPageLoadValidator = new PageLoadValidator(
            customValidationScript,
            null,
            new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000)
    );

    ScenarioBuilder mainScenario = scenario("test").exec(
            exec(session -> session
                    .set("testUrl", "https://gatling.io/")
                    .set("actionName", "open")),
            BrowserDsl.browserAction("open").open("https://gatling.io/").withLoadValidations(),
            BrowserDsl.browserAction("#{actionName}").open("#{testUrl}").withLoadValidations(customPageLoadValidator),
            BrowserDsl.browserAction("open").open("https://gatling.io/").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(NETWORKIDLE)),
            BrowserDsl.browserAction("open").executeFlow((page, browserSession) -> {
                page.reload();
                return browserSession;
            }),
            BrowserDsl.browserSessionFunction((page, browserSession) -> {

                browserSession.setStatusOK();
                browserSession.setStatusKO();
                browserSession.setStatusKO("my error text");

                browserSession.getStatus();
                browserSession.getErrorMessage();

                Session gatlingSession = browserSession.getGatlingSession();
                browserSession = browserSession.updateBrowserSession(gatlingSession);
                browserSession.resolveSessionValue("#{actionName}");
                browserSession.resolveSessionValue(session -> session.getString("actionName"));

                browserSession.setActionStartTime();
                browserSession.setActionEndTime();
                browserSession.setActionStartTime(System.currentTimeMillis());
                browserSession.setActionEndTime(System.currentTimeMillis());

                browserSession.getActionStartTime();
                browserSession.getActionEndTime();

                return browserSession;
            }),
            BrowserDsl.browserCleanContext()
    );

    {
        setUp(mainScenario.injectOpen(atOnceUsers(1))).protocols(browserProtocol);
    }
}
