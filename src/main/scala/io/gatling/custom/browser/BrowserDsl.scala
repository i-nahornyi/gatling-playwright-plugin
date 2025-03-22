package io.gatling.custom.browser

import com.microsoft.playwright.Page
import io.gatling.core.session.Expression
import io.gatling.custom.browser.actions.actionsList
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.protocol.BrowserProtocolBuilderBase

import java.util.function.BiFunction

trait BrowserDsl {

  def gatlingBrowser: BrowserProtocolBuilderBase.type = BrowserProtocolBuilderBase

  def browserAction(requestName: Expression[String]): actionsList.BrowserBaseAction = actionsList.BrowserBaseAction(requestName)

  def browserSessionFunction(function: BiFunction[Page, BrowserSession, BrowserSession]): actionsList.BrowserSessionFunctionActionsBuilder =
    actionsList.BrowserSessionFunctionActionsBuilder(function)

  def browserCleanContext(): actionsList.BrowserClearActionsBuilder = actionsList.BrowserClearActionsBuilder()
}
