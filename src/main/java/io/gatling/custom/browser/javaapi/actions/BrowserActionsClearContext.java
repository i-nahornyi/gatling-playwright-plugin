package io.gatling.custom.browser.javaapi.actions;

import io.gatling.custom.browser.actions.actionsList.BrowserClearActionsBuilder;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;

public class BrowserActionsClearContext implements ActionBuilder {

    private final BrowserClearActionsBuilder wrapped;

    public BrowserActionsClearContext(BrowserClearActionsBuilder wrapped) {
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
