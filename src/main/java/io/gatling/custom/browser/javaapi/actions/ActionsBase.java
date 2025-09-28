package io.gatling.custom.browser.javaapi.actions;


import com.microsoft.playwright.Page;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.internal.Expressions;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionsBase {

    private final io.gatling.custom.browser.actions.BrowserAction wrapped;

    public ActionsBase(io.gatling.custom.browser.actions.BrowserAction actions) {
        this.wrapped = actions;
    }

    public BrowserActionOpenBuilder open(String url) {
        return new BrowserActionOpenBuilder(wrapped.open(Expressions.toStringExpression(url)));
    }

    public BrowserActionOpenBuilder open(Function<Session, String> url) {
        return new BrowserActionOpenBuilder(wrapped.open(Expressions.javaFunctionToExpression(url)));
    }

    public BrowserActionExecuteFlow executeFlow(BiFunction<Page, BrowserSession, BrowserSession> function) {
        return new BrowserActionExecuteFlow(wrapped.executeFlow(function));
    }
}
