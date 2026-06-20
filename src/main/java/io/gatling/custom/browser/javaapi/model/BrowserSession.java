package io.gatling.custom.browser.javaapi.model;

import io.gatling.commons.stats.Status;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.internal.Expressions;
import scala.Option;

import java.util.function.Function;

public class BrowserSession {
    private final io.gatling.custom.browser.model.BrowserSession wrapped;

    public BrowserSession(io.gatling.custom.browser.model.BrowserSession wrapped) {
        this.wrapped = wrapped;
    }


    public io.gatling.custom.browser.model.BrowserSession asScala() {
        return wrapped;
    }

    public Session getGatlingSession() {
        return new Session(wrapped.getGatlingSession());
    }

    public BrowserSession updateBrowserSession(Session session) {
        return new BrowserSession(wrapped.updateBrowserSession(session.asScala()));
    }

    public Object resolveSessionValue(String value) {
        return wrapped.resolveSessionValue(value);
    }

    public Object resolveSessionValue(Function<Session, Object> function) {
        return wrapped.resolveSessionValue(Expressions.javaFunctionToExpression(function));
    }

    public void setActionStartTime() {
        wrapped.setActionStartTime();
    }

    public void setActionStartTime(long currentTime) {
        wrapped.setActionStartTime(currentTime);
    }

    public void setActionEndTime() {
        wrapped.setActionEndTime();
    }

    public void setActionEndTime(long currentTime) {
        wrapped.setActionEndTime(currentTime);
    }


    public long getActionEndTime() {
        return wrapped.getActionEndTime();
    }

    public long getActionStartTime() {
        return wrapped.getActionStartTime();
    }

    public void setStatusOK() {
        wrapped.setStatusOK();
    }

    public void setStatusKO() {
        wrapped.setStatusKO();
    }

    public void setStatusKO(String errorMessage) {
        wrapped.setStatusKO(errorMessage);
    }

    public Status getStatus() {
        return wrapped.getStatus();
    }

    public Option<String> getErrorMessage() {
        return wrapped.getErrorMessage();
    }
}
