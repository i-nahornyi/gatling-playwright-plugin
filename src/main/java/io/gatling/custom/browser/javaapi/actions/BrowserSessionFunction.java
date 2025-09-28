package io.gatling.custom.browser.javaapi.actions;

import io.gatling.javaapi.core.ActionBuilder;

public class BrowserSessionFunction implements ActionBuilder {

    private final io.gatling.custom.browser.actions.builder.BrowserSessionFunctionActionsBuilder wrapped;

    public BrowserSessionFunction(io.gatling.custom.browser.actions.builder.BrowserSessionFunctionActionsBuilder wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return wrapped;
    }
}
