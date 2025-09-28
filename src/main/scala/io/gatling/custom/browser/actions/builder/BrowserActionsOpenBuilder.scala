package io.gatling.custom.browser.actions.builder

import com.microsoft.playwright.Page.NavigateOptions
import io.gatling.core.action.Action
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.session.Expression
import io.gatling.core.structure.ScenarioContext
import io.gatling.custom.browser.actions.BrowserActionOpen
import io.gatling.custom.browser.model.PageLoadValidator
import io.gatling.custom.browser.utils.PerformanceUIHelper


object BrowserActionsOpenBuilder {
  def apply(requestName: Expression[String], url: Expression[String]): BrowserActionsOpenBuilder = new BrowserActionsOpenBuilder(requestName, url, null, null)
}

final class BrowserActionsOpenBuilder(requestName: Expression[String],
                                      url: Expression[String],
                                      navigateOptions: NavigateOptions,
                                      loadValidations: PageLoadValidator
                                     ) extends ActionBuilder {

  def withNavigateOptions(navigateOptions: NavigateOptions) = new BrowserActionsOpenBuilder(requestName, url, navigateOptions, null)

  def withLoadValidations(validator: PageLoadValidator) = new BrowserActionsOpenBuilder(requestName, url, null, validator)
  def withLoadValidations() = new BrowserActionsOpenBuilder(requestName, url, null, PerformanceUIHelper.defaultPageLoadValidator)

  override def build(ctx: ScenarioContext, next: Action): Action = BrowserActionOpen(requestName, url, navigateOptions, loadValidations, ctx, next)
}
