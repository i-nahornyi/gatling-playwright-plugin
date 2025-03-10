package io.gatling.custom.browser.javaapi;

import io.gatling.custom.browser.javaapi.actions.ActionsBase;
import io.gatling.custom.browser.javaapi.actions.BrowserActionsClearContext;
import io.gatling.custom.browser.javaapi.protocol.BrowserProtocolBuilderBase;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.internal.Expressions;

import java.util.function.Function;

public final class BrowserDsl {
    private BrowserDsl() {
    }

    public static BrowserProtocolBuilderBase gatlingBrowser() {
        return new BrowserProtocolBuilderBase();
    }

    public static ActionsBase browserAction(String name) {
        return new ActionsBase(io.gatling.custom.browser.Predef.browserAction(Expressions.toStringExpression(name)));
    }

    public static ActionsBase browserAction(Function<Session, String> name) {
        return new ActionsBase(io.gatling.custom.browser.Predef.browserAction(Expressions.javaFunctionToExpression(name)));
    }

    public static BrowserActionsClearContext browserCleanContext(){
        return new BrowserActionsClearContext(io.gatling.custom.browser.Predef.browserCleanContext());
    }
}
