package io.gatling.custom.browser.javaapi.actions;

import io.gatling.custom.browser.actions.actionsList.BrowserSessionFunctionActionsBuilder;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;

public class BrowserActionsSessionFunction implements ActionBuilder {

    private final BrowserSessionFunctionActionsBuilder wrapped;

    public BrowserActionsSessionFunction(BrowserSessionFunctionActionsBuilder wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return wrapped;
    }

    @Override
    public ChainBuilder toChainBuilder() {
        return ActionBuilder.super.toChainBuilder();
    }
}
