package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import io.gatling.core.session.Expression
import io.gatling.custom.browser.actions.builder.{BrowserActionsExecuteFlowBuilder, BrowserActionsOpenBuilder}
import io.gatling.custom.browser.model.BrowserSession

import java.util.function.BiFunction

case class BrowserAction(requestName: Expression[String]) {
  def open(url: Expression[String]): BrowserActionsOpenBuilder = BrowserActionsOpenBuilder(requestName, url)
  def executeFlow(function: BiFunction[Page, BrowserSession, BrowserSession]): BrowserActionsExecuteFlowBuilder = BrowserActionsExecuteFlowBuilder(requestName, function)
}
