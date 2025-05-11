package ui.performance.simulations.regression;

import io.gatling.app.Gatling;
import io.gatling.shared.cli.GatlingCliOptions;

public class Runner {

    // BrowserSimulationsJava
    // BrowserSimulationsScala
    // regression.ClosedModelBrowser
    // regression.MultiUserWorkaround
    // regression.FailedSimulation
    // regression.ClosedPageRecover
    // regression.Smoke
    // regression.WebVitalsJava
    // regression.WebVitalsScala


    public static void main(String[] args) {
        String [] runParams = new String[]{
                GatlingCliOptions.Simulation.shortOption(),"ui.performance.simulations.regression.WebVitalsJava",
                GatlingCliOptions.RunDescription.shortOption(), "Custom descriptions for test-%d".formatted(System.currentTimeMillis()),
                GatlingCliOptions.ResultsFolder.shortOption(), "target/gatling"
        };

        Gatling.main(runParams);
    }
}
