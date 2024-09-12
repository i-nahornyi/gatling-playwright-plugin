package io.gatling.custom.browser.javaapi.protocol;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import io.gatling.custom.browser.protocol.DefaultProtocolOptions;

public class BrowserProtocolBuilderBase {

    BrowserProtocolBuilder wrapped;

    BrowserType.LaunchOptions launchOptions = DefaultProtocolOptions.defaultProtocolOptions() ;
    Browser.NewContextOptions contextOptions = DefaultProtocolOptions.defaultContextOptions();

    public BrowserProtocolBuilderBase() {
        this.wrapped = buildProtocol();
    }

    public BrowserProtocolBuilderBase withLaunchOptions(BrowserType.LaunchOptions launchOptions){
        this.launchOptions = launchOptions;
        return this;
    }

    public BrowserProtocolBuilderBase withContextOptions(Browser.NewContextOptions contextOptions){
        this.contextOptions = contextOptions;
        return this;
    }

    public BrowserProtocolBuilder buildProtocol(){
        return new BrowserProtocolBuilder(new io.gatling.custom.browser.protocol.BrowserProtocolBuilder(launchOptions,contextOptions));
    }

}
