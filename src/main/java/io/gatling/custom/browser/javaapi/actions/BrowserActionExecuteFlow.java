package io.gatling.custom.browser.javaapi.actions;


import io.gatling.custom.browser.actions.builder.BrowserActionsExecuteFlowBuilder;
import io.gatling.javaapi.core.ActionBuilder;

public class BrowserActionExecuteFlow implements ActionBuilder {

    private final BrowserActionsExecuteFlowBuilder wrapped;

    public BrowserActionExecuteFlow(BrowserActionsExecuteFlowBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return this.wrapped;
    }
}
