package io.gatling.custom.browser.actions

import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.NavigateOptions
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.model.BrowserSession

import java.util.function.BiFunction

object actionsList {

  case class BrowserBaseAction(requestName: Expression[String]) {
    def open(url: Expression[String], navigateOptions: NavigateOptions): BrowserActionsOpenBuilder = BrowserActionsOpenBuilder(requestName, url, navigateOptions)

    def open(url: Expression[String]): BrowserActionsOpenBuilder = BrowserActionsOpenBuilder(requestName, url, null)

    def executeFlow(function: BiFunction[Page, BrowserSession, BrowserSession]): BrowserActionsExecuteFlowBuilder = BrowserActionsExecuteFlowBuilder(requestName, function)

    def browserCleanContext(): BrowserClearActionsBuilder = BrowserClearActionsBuilder()
  }

  case class BrowserActionsOpenBuilder(requestName: Expression[String], url: Expression[String], navigateOptions: NavigateOptions) extends ActionBuilder {
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionOpen(requestName, url, navigateOptions, ctx, next)
  }

  case class BrowserActionsExecuteFlowBuilder(requestName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession]) extends ActionBuilder {
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionExecuteFlow(requestName, function, ctx, next)
  }

  case class BrowserClearActionsBuilder() extends ActionBuilder {
    override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionsClearContext(ctx, next)
  }

}
