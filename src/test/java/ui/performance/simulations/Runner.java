package ui.performance.simulations;

import io.gatling.app.Gatling;
import io.gatling.shared.cli.GatlingCliOptions;

public class Runner {

    // ClosedModelBrowser
    // MultiUserWorkaround
    // BrowserSimulationsJava
    // BrowserSimulationsScala


    public static void main(String[] args) {
        String [] runParams = new String[]{
                GatlingCliOptions.Simulation.shortOption(),"ui.performance.simulations.ClosedModelBrowser",
                GatlingCliOptions.ResultsFolder.shortOption(), "target/gatling"
        };

        Gatling.main(runParams);
    }
}
