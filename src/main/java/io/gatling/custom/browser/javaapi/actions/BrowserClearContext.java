package io.gatling.custom.browser.javaapi.actions;

import io.gatling.custom.browser.actions.builder.BrowserClearActionsBuilder;
import io.gatling.javaapi.core.ActionBuilder;

public class BrowserClearContext implements ActionBuilder {

    private final BrowserClearActionsBuilder wrapped;

    public BrowserClearContext(BrowserClearActionsBuilder wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return wrapped;
    }
}
