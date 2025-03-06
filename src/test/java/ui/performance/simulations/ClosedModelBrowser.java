package ui.performance.simulations;

import io.gatling.custom.browser.javaapi.BrowserDsl;
import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;

@SuppressWarnings("unused")
public class ClosedModelBrowser extends Simulation {

    FeederBuilder<String> pageFeeder = CoreDsl.csv("feeders/page.csv").circular();

    ScenarioBuilder mainScenario = CoreDsl.scenario("test")
            .feed(pageFeeder)
            .exec(
                    CoreDsl.pause(3),
                    BrowserDsl.browserAction("#{name}_1").open("#{link}")
            )
            .randomSwitch().on(
                    CoreDsl.percent(50).then(
                            BrowserDsl.browserAction("#{name}_2").open("#{link2}"),
                            BrowserDsl.browserCleanContext()

                    ),
                    CoreDsl.percent(50).then(
                            CoreDsl.exitHere()
                    )
            );


    {
        setUp(mainScenario.injectClosed(CoreDsl.constantConcurrentUsers(1).during(60)));
    }


}
