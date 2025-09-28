package io.gatling.custom.browser.actions.builder

import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.actions.BrowserClearContext

object BrowserClearActionsBuilder {
  def apply(): BrowserClearActionsBuilder = new BrowserClearActionsBuilder()
}

case class BrowserClearActionsBuilder() extends ActionBuilder {
  override def build(ctx: ScenarioContext, next: Action): Action = BrowserClearContext(ctx, next)
}


