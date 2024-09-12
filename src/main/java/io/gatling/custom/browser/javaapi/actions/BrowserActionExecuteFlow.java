package io.gatling.custom.browser.javaapi.actions;


import io.gatling.custom.browser.actions.actionsList.BrowserActionsExecuteFlowBuilder;
import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ChainBuilder;

public class BrowserActionExecuteFlow implements ActionBuilder {

    private final BrowserActionsExecuteFlowBuilder wrapped;

    public BrowserActionExecuteFlow(BrowserActionsExecuteFlowBuilder wrapped) {
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
