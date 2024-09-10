package io.gatling.custom.browser.javaapi;

import io.gatling.custom.browser.javaapi.actions.ActionsBase;
import io.gatling.custom.browser.javaapi.protocol.BrowserProtocolBuilderBase;

public final class BrowserDsl {
    private BrowserDsl() {
    }

    public static BrowserProtocolBuilderBase gatlingBrowser() {
        return new BrowserProtocolBuilderBase();
    }

    public static ActionsBase browserAction(String name) {
        return new ActionsBase(io.gatling.custom.browser.Predef.browserAction(name));
    }

    public static ActionsBase browserAction() {
        return new ActionsBase(io.gatling.custom.browser.Predef.browserAction());
    }
}
