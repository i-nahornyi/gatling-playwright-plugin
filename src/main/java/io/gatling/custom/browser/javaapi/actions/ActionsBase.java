package io.gatling.custom.browser.javaapi.actions;


import com.microsoft.playwright.Page;
import io.gatling.custom.browser.model.BrowserSession;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.internal.Expressions;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionsBase {

    private final io.gatling.custom.browser.actions.actionsList.BrowserBaseAction wrapped;

    public ActionsBase(io.gatling.custom.browser.actions.actionsList.BrowserBaseAction actions) {
        this.wrapped = actions;
    }

    public BrowserActionOpen open(String url) {
        return new BrowserActionOpen(wrapped.open(Expressions.toStringExpression(url)));
    }

    public BrowserActionOpen open(Function<Session, String> url) {
        return new BrowserActionOpen(wrapped.open(Expressions.javaFunctionToExpression(url)));
    }

    public BrowserActionExecuteFlow executeFlow(BiFunction<Page, BrowserSession, BrowserSession> function) {
        return new BrowserActionExecuteFlow(wrapped.executeFlow(function));
    }
}
