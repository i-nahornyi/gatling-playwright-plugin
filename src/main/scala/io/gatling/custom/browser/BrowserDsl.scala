package io.gatling.custom.browser

import io.gatling.core.session.Expression
import io.gatling.custom.browser.actions.actionsList
import io.gatling.custom.browser.protocol.BrowserProtocolBuilderBase

trait BrowserDsl {

  def gatlingBrowser: BrowserProtocolBuilderBase.type = BrowserProtocolBuilderBase

  def browserAction(requestName: Expression[String]): actionsList.BrowserBaseAction = actionsList.BrowserBaseAction(requestName)

  def browserCleanContext(): actionsList.BrowserClearActionsBuilder = actionsList.BrowserClearActionsBuilder()
}
