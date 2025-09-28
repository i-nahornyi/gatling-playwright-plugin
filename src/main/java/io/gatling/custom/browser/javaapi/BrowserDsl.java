package io.gatling.custom.browser.javaapi;

import com.microsoft.playwright.Page;
import io.gatling.custom.browser.javaapi.actions.ActionsBase;
import io.gatling.custom.browser.javaapi.actions.BrowserClearContext;
import io.gatling.custom.browser.javaapi.actions.BrowserSessionFunction;
import io.gatling.custom.browser.javaapi.protocol.BrowserProtocolBuilderBase;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.internal.Expressions;

import java.util.function.BiFunction;
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

    public static BrowserClearContext browserCleanContext(){
        return new BrowserClearContext(io.gatling.custom.browser.Predef.browserCleanContext());
    }

    public static BrowserSessionFunction browserSessionFunction(BiFunction<Page, BrowserSession, BrowserSession> function) {
        return new BrowserSessionFunction(io.gatling.custom.browser.Predef.browserSessionFunction(function));
    }

    public static String loadScript(String filePath){
        return io.gatling.custom.browser.Predef.loadScript(filePath);
    }
}
