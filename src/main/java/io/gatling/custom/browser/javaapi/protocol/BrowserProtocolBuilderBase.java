package io.gatling.custom.browser.javaapi.protocol;

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import io.gatling.custom.browser.protocol.DefaultProtocolOptions;

public class BrowserProtocolBuilderBase {

    BrowserProtocolBuilder wrapped;

    LaunchOptions launchOptions = DefaultProtocolOptions.defaultProtocolOptions() ;
    NewContextOptions contextOptions = DefaultProtocolOptions.defaultContextOptions();
    Boolean webVitalsEnable = DefaultProtocolOptions.defaultWebVitalsEnable();

    public BrowserProtocolBuilderBase() {
        this.wrapped = buildProtocol();
    }

    public BrowserProtocolBuilderBase withLaunchOptions(LaunchOptions launchOptions){
        this.launchOptions = launchOptions;
        return this;
    }

    public BrowserProtocolBuilderBase withContextOptions(NewContextOptions contextOptions){
        this.contextOptions = contextOptions;
        return this;
    }
    public BrowserProtocolBuilderBase enableUIMetrics(Boolean webVitalsEnable){
        this.webVitalsEnable = webVitalsEnable;
        return this;
    }

    public BrowserProtocolBuilder buildProtocol(){
        return new BrowserProtocolBuilder(new io.gatling.custom.browser.protocol.BrowserProtocolBuilder(launchOptions,contextOptions, webVitalsEnable));
    }

}
