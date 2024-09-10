package io.gatling.custom.browser.model;

import io.gatling.commons.stats.Status;
import io.gatling.core.session.Session;



public class BrowserSession {

    public Session gatlingScalaSession;
    public io.gatling.javaapi.core.Session gatlingJavaSession;
    public Long actionStartTime;
    public Long actionEndTime;

    private Status status;
    private String errorMessage;

    public BrowserSession(Session session){
        this.gatlingScalaSession = session;
        this.gatlingJavaSession = new io.gatling.javaapi.core.Session(session);
        this.status = StatusWrapper.OK();
    }

    public BrowserSession(io.gatling.javaapi.core.Session session){
        this.gatlingJavaSession = session;
        this.gatlingScalaSession = session.asScala();
        this.status = StatusWrapper.OK();
    }

    public BrowserSession updateBrowserSession(Session session) {
        this.gatlingJavaSession = new io.gatling.javaapi.core.Session(session);
        this.gatlingScalaSession = session;
        return this;
    }

    public BrowserSession updateBrowserSession(io.gatling.javaapi.core.Session session) {
        this.gatlingJavaSession = session;
        this.gatlingScalaSession = session.asScala();
        return this;
    }

    public void setStatusKO(String errorMessage){
        this.status = StatusWrapper.KO();
        this.errorMessage = errorMessage;
    }
    public void setStatusKO(){
        this.status = StatusWrapper.KO();
    }

    public void setStatusOK(){
        this.status = StatusWrapper.OK();
    }

    public Status getStatus(){
        return this.status;
    }

    public String getErrorMessage(){
        return this.errorMessage;
    }


}
