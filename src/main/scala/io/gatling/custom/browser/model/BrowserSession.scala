package io.gatling.custom.browser.model

import io.gatling.commons.stats.Status
import io.gatling.core.session.{Expression, SessionAttribute, Session => GatlingScalaSession}
import io.gatling.javaapi.core.internal.Expressions

class BrowserSession(private var gatlingScalaSession: GatlingScalaSession) {

  private var actionStartTime: Option[Long] = Option.empty
  private var actionEndTime: Option[Long] = Option.empty

  private var status: Status = StatusWrapper.OK
  private var errorMessage: Option[String] = None

  def getGatlingSession: GatlingScalaSession = {
    this.gatlingScalaSession
  }

  def getGatlingSession(value: String): SessionAttribute = {
    this.gatlingScalaSession(value: String)
  }

  def getActionStartTime: Long = {
    this.actionStartTime.getOrElse(0)
  }

  def setActionStartTime(): Unit = {
    setActionStartTime(System.currentTimeMillis())
  }

  def setActionStartTime(actionStartTime: Long): Unit = {
    this.actionStartTime = Option.apply(actionStartTime + 1)
  }

  def getActionEndTime: Long = {
    this.actionEndTime.getOrElse(0)
  }

  def setActionEndTime(): Unit = {
    setActionEndTime(System.currentTimeMillis())
  }

  def setActionEndTime(actionEndTime: Long): Unit = {
    this.actionEndTime = Option.apply(actionEndTime + 1)
  }

  def updateBrowserSession(session: GatlingScalaSession): BrowserSession = {
    this.gatlingScalaSession = session
    this
  }

  def setStatusKO(errorMessage: String): Unit = {
    this.status = StatusWrapper.KO
    this.errorMessage = Some(errorMessage)
  }

  def setStatusKO(): Unit = {
    this.status = StatusWrapper.KO
  }

  def setStatusOK(): Unit = {
    this.status = StatusWrapper.OK
  }

  def getStatus: Status = this.status

  def getErrorMessage: Option[String] = this.errorMessage


  def resolveSessionValue(value: String): Any = {
    Expressions.toAnyExpression(value)(this.gatlingScalaSession).toOption.get
  }

  def resolveSessionValue(value: Expression[Any]): Any = {
    value(this.gatlingScalaSession).toOption.get
  }
}
