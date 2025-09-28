package io.gatling.custom.browser.javaapi.actions;

import io.gatling.custom.browser.actions.actionsList.BrowserActionsOpenBuilder;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;

public class BrowserActionOpen implements ActionBuilder {

    private final BrowserActionsOpenBuilder wrapped;

    public BrowserActionOpen(BrowserActionsOpenBuilder wrapped){
        this.wrapped = wrapped;
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return this.wrapped;
    }

    @Override
    public ChainBuilder toChainBuilder() {
        return ActionBuilder.super.toChainBuilder();
    }

}
