package io.gatling.custom.browser.javaapi.actions;


import com.microsoft.playwright.Page;
import io.gatling.custom.browser.model.BrowserSession;

import java.util.function.BiFunction;

public class ActionsBase {

    private final io.gatling.custom.browser.actions.actionsList.BrowserBaseAction wrapped;

    public ActionsBase(io.gatling.custom.browser.actions.actionsList.BrowserBaseAction actions) {
        this.wrapped = actions;
    }

    public BrowserActionOpen open(String url){
        return new BrowserActionOpen(wrapped.open(url));
    }

    public BrowserActionExecuteFlow executeFlow(BiFunction<Page, BrowserSession, BrowserSession> function){
        return new BrowserActionExecuteFlow(wrapped.executeFlow(function));
    }

    public BrowserActionsClearContext browserCleanContext(){
        return new BrowserActionsClearContext(wrapped.browserCleanContext());
    }


}
