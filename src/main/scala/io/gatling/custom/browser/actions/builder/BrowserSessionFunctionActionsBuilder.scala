package io.gatling.custom.browser.actions.builder

import com.microsoft.playwright.Page
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.actions.BrowserSessionFunction
import io.gatling.custom.browser.model.BrowserSession

import java.util.function.BiFunction

object BrowserSessionFunctionActionsBuilder {
  def apply(function: BiFunction[Page, BrowserSession, BrowserSession]): BrowserSessionFunctionActionsBuilder = new BrowserSessionFunctionActionsBuilder(function)
}

final class BrowserSessionFunctionActionsBuilder(function: BiFunction[Page, BrowserSession, BrowserSession]) extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = BrowserSessionFunction(function, ctx, next)
}


