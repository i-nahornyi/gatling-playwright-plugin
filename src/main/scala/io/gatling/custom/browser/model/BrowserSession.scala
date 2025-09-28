package io.gatling.custom.browser.model

import io.gatling.commons.stats.Status
import io.gatling.core.session.{Expression, SessionAttribute, Session => GatlingScalaSession}
import io.gatling.javaapi.core.internal.{Expressions, JavaExpression}
import io.gatling.javaapi.core.{Session => GatlingJavaSession}

class BrowserSession(private var gatlingScalaSession: GatlingScalaSession) {

  private var gatlingJavaSession: GatlingJavaSession = new GatlingJavaSession(gatlingScalaSession)
  private var actionStartTime: Option[Long] = Option.empty
  private var actionEndTime: Option[Long] = Option.empty

  private var status: Status = StatusWrapper.OK
  private var errorMessage: Option[String] = None

  def this(gatlingJavaSession: GatlingJavaSession) = {
    this(gatlingJavaSession.asScala) // Invoke the primary constructor
    this.gatlingJavaSession = gatlingJavaSession
  }

  // Expect bracket for syntax
  def getJavaSession: GatlingJavaSession = {
    this.gatlingJavaSession
  }

  def getScalaSession(): GatlingScalaSession = {
    this.gatlingScalaSession
  }

  // Expect bracket for syntax
  def getScalaSession(value: String): SessionAttribute = {
    this.gatlingScalaSession(value: String)
  }

  def getActionStartTime: Long = {
    this.actionStartTime.getOrElse(0)
  }

  def setActionStartTime(actionStartTime: Long): Unit = {
    this.actionStartTime = Option.apply(actionStartTime + 1)
  }

  def getActionEndTime: Long = {
    this.actionEndTime.getOrElse(0)
  }

  def setActionEndTime(actionEndTime: Long): Unit = {
    this.actionEndTime = Option.apply(actionEndTime + 1)
  }

  def updateBrowserSession(session: GatlingScalaSession): BrowserSession = {
    this.gatlingScalaSession = session
    this.gatlingJavaSession = new GatlingJavaSession(session)
    this
  }

  def updateBrowserSession(session: GatlingJavaSession): BrowserSession = {
    this.gatlingJavaSession = session
    this.gatlingScalaSession = session.asScala
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

  //// Used for JavaAPI only
  def resolveSessionExpression[T](any: JavaExpression[T]): Any = {
    Expressions.javaFunctionToExpression(any)(this.gatlingScalaSession).toOption.get
  }

  def resolveSessionValue(value: Expression[Any]): Any = {
    value(this.gatlingScalaSession).toOption.get
  }
}
