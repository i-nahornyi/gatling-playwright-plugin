package io.gatling.custom.browser.javaapi.utils;

import com.microsoft.playwright.Page;
import io.gatling.custom.browser.model.BrowserSession;

import java.util.function.BiFunction;

public final class Converter {
    public static BiFunction<Page, BrowserSession, BrowserSession> sessionFunctionToScala(BiFunction<Page, io.gatling.custom.browser.javaapi.model.BrowserSession, io.gatling.custom.browser.javaapi.model.BrowserSession> function){
        return (page, browserSession) -> {
            io.gatling.custom.browser.javaapi.model.BrowserSession javaSession = new io.gatling.custom.browser.javaapi.model.BrowserSession(browserSession);
            io.gatling.custom.browser.javaapi.model.BrowserSession result = function.apply(page,javaSession);
            return result.asScala();
        };
    }
}
