package io.gatling.custom.browser.javaapi.actions;

import com.microsoft.playwright.Page;
import io.gatling.custom.browser.model.PageLoadValidator;
import io.gatling.javaapi.core.ActionBuilder;

public class BrowserActionOpenBuilder implements ActionBuilder {

    private final io.gatling.custom.browser.actions.builder.BrowserActionsOpenBuilder wrapped;

    public BrowserActionOpenBuilder(io.gatling.custom.browser.actions.builder.BrowserActionsOpenBuilder wrapped){
        this.wrapped = wrapped;
    }

    public BrowserActionOpenBuilder withNavigateOptions(Page.NavigateOptions navigateOptions){
        return new BrowserActionOpenBuilder(wrapped.withNavigateOptions(navigateOptions));
    }

    public BrowserActionOpenBuilder withLoadValidations(PageLoadValidator validator){
        return new BrowserActionOpenBuilder(wrapped.withLoadValidations(validator));
    }

    public BrowserActionOpenBuilder withLoadValidations(){
        return new BrowserActionOpenBuilder(wrapped.withLoadValidations());
    }

    @Override
    public io.gatling.core.action.builder.ActionBuilder asScala() {
        return this.wrapped;
    }

}
