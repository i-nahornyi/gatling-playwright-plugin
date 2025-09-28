package io.gatling.custom.browser

import com.microsoft.playwright.Page
import io.gatling.core.session.Expression
import io.gatling.custom.browser.actions.BrowserAction
import io.gatling.custom.browser.actions.builder.{BrowserClearActionsBuilder, BrowserSessionFunctionActionsBuilder}
import io.gatling.custom.browser.model.BrowserSession
import io.gatling.custom.browser.protocol.BrowserProtocolBuilderBase
import io.gatling.custom.browser.utils.Utils

import java.util.function.BiFunction

trait BrowserDsl {

  def gatlingBrowser: BrowserProtocolBuilderBase.type = BrowserProtocolBuilderBase

  def browserAction(requestName: Expression[String]): BrowserAction = BrowserAction(requestName)
  def browserSessionFunction(function: BiFunction[Page, BrowserSession, BrowserSession]): BrowserSessionFunctionActionsBuilder =
    BrowserSessionFunctionActionsBuilder(function)
  def browserCleanContext(): BrowserClearActionsBuilder = BrowserClearActionsBuilder()

  def loadScript(filePath: String): String = Utils.readFileFromResources(filePath)

}
