package io.gatling.custom.browser.actions.builder

import com.microsoft.playwright.Page
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.actions.BrowserActionExecuteFlow
import io.gatling.custom.browser.model.BrowserSession

import java.util.function.BiFunction


object BrowserActionsExecuteFlowBuilder {
  def apply(requestName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession]): BrowserActionsExecuteFlowBuilder = new BrowserActionsExecuteFlowBuilder(requestName, function)
}

final class BrowserActionsExecuteFlowBuilder(requestName: Expression[String], function: BiFunction[Page, BrowserSession, BrowserSession]) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionExecuteFlow(requestName, function, ctx, next)
}
