package io.gatling.custom.browser.javaapi.protocol;

import io.gatling.core.protocol.Protocol;
import io.gatling.javaapi.core.ProtocolBuilder;

public class BrowserProtocolBuilder implements ProtocolBuilder {

    private final io.gatling.custom.browser.protocol.BrowserProtocolBuilder wrapped;

    public BrowserProtocolBuilder(io.gatling.custom.browser.protocol.BrowserProtocolBuilder browserProtocolBuilder){
        this.wrapped = browserProtocolBuilder;
    }

    @Override
    public Protocol protocol() {
        return wrapped.buildProtocol();
    }
}
