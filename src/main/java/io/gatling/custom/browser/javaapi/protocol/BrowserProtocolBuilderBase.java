package io.gatling.custom.browser.javaapi.protocol;

import com.microsoft.playwright.Browser.NewContextOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;
import io.gatling.core.protocol.Protocol;
import io.gatling.custom.browser.protocol.DefaultProtocolOptions;
import io.gatling.javaapi.core.ProtocolBuilder;
import scala.Option;

public class BrowserProtocolBuilderBase implements ProtocolBuilder {

    LaunchOptions launchOptions = DefaultProtocolOptions.defaultProtocolOptions() ;
    NewContextOptions contextOptions = DefaultProtocolOptions.defaultContextOptions();
    Boolean webVitalsEnable = DefaultProtocolOptions.defaultWebVitalsEnable();
    Double defaultTimeout = null;

    public BrowserProtocolBuilderBase withLaunchOptions(LaunchOptions launchOptions){
        this.launchOptions = launchOptions;
        return this;
    }

    public BrowserProtocolBuilderBase withContextOptions(NewContextOptions contextOptions){
        this.contextOptions = contextOptions;
        return this;
    }
    public BrowserProtocolBuilderBase enableUIMetrics(){
        this.webVitalsEnable = true;
        return this;
    }

    public BrowserProtocolBuilderBase withDefaultTimeout(double timeoutMillis){
        this.defaultTimeout = timeoutMillis;
        return this;
    }


    /**
     * @deprecated No longer necessary to call explicitly — this builder itself implements
     * {@link ProtocolBuilder} and can be passed directly wherever one is expected (e.g. {@code .protocols(...)}).
     */
    @Deprecated(since = "0.1.5")
    public BrowserProtocolBuilder buildProtocol(){
        return new BrowserProtocolBuilder(new io.gatling.custom.browser.protocol.BrowserProtocolBuilder(launchOptions, contextOptions, webVitalsEnable, Option.apply(defaultTimeout)));
    }

    // Lets the builder itself be passed directly to .protocols(...) without an explicit
    // .buildProtocol() call, mirroring the Scala side's implicit BrowserProtocolBuilder -> Protocol
    // conversion (Java has no implicits, so implementing ProtocolBuilder is the equivalent here).
    @Override
    @SuppressWarnings("deprecation")
    public Protocol protocol() {
        return buildProtocol().protocol();
    }

}
