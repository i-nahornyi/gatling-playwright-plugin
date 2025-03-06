package ui.performance.simulations;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.*;

import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;


@SuppressWarnings("unused")
public class MultiUserWorkaround extends Simulation {

    ProtocolBuilder browserProtocol = BrowserDsl
            .gatlingBrowser()
            //// This part of setup block is optional
            .withLaunchOptions(new BrowserType.LaunchOptions().setHeadless(false))
            .withContextOptions(new Browser.NewContextOptions().setViewportSize(1920, 1080))
            ////
            .buildProtocol();

    FeederBuilder<String> pageFeeder = CoreDsl.csv("feeders/page.csv").circular();

    ChainBuilder flow = ChainBuilder.EMPTY.exec(
            repeat(2).on(
                    CoreDsl.pause(1,5),
                    feed(pageFeeder),
                    BrowserDsl.browserAction("#{name}").open("#{link}"),
                    CoreDsl.pause(1),
                    BrowserDsl.browserCleanContext()
            )
    );

    {
        List<PopulationBuilder> simmulationList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            simmulationList.add(scenario("scenario_"+i).exec(flow).injectOpen(OpenInjectionStep.atOnceUsers(1)));
        }
        setUp(simmulationList).protocols(browserProtocol);
    }
}
