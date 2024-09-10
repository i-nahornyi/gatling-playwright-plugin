package io.gatling.custom.browser

import io.gatling.custom.browser.actions.actionsList
import io.gatling.custom.browser.actions.actionsList.BrowserBaseAction
import io.gatling.custom.browser.protocol.BrowserProtocolBuilderBase

trait BrowserDsl {

  def gatlingBrowser: BrowserProtocolBuilderBase.type = BrowserProtocolBuilderBase

  def browserAction(name: String): BrowserBaseAction = actionsList.BrowserBaseAction(name)

  def browserAction(): BrowserBaseAction = actionsList.BrowserBaseAction("userBrowserAction")

  def browserCleanContext(): actionsList.BrowserClearActionsBuilder = actionsList.BrowserClearActionsBuilder()
}
