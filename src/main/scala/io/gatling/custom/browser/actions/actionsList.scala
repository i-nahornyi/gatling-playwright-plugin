package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.model.BrowserSession

import java.util.function.BiFunction

object actionsList {

  case class BrowserBaseAction(requestName: String) {
    def open(url: String): BrowserActionsOpenBuilder = BrowserActionsOpenBuilder(requestName, url)
    def executeFlow(function: BiFunction[Page,BrowserSession,BrowserSession]): BrowserActionsExecuteFlowBuilder = BrowserActionsExecuteFlowBuilder(requestName,function)
    def browserCleanContext(): BrowserClearActionsBuilder = BrowserClearActionsBuilder()
  }
  case class BrowserActionsOpenBuilder(requestName: String, url: String) extends ActionBuilder {
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionOpen(requestName,url, ctx, next)
  }

  case class BrowserActionsExecuteFlowBuilder(requestName: String, function: BiFunction[Page,BrowserSession,BrowserSession]) extends ActionBuilder {
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionExecuteFlow(requestName, function, ctx, next)
  }

  case class BrowserClearActionsBuilder() extends ActionBuilder{
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionsClearContext(ctx,next)
  }

}
