package io.gatling.custom.browser.model

import com.microsoft.playwright.Page.WaitForFunctionOptions

case class PageLoadValidator(expression: String, arg: Any = null, options: WaitForFunctionOptions = null) {
  def this(expression: String) = this(expression, null, null)

  def this(expression: String, arg: Any) = this(expression, arg, null)
}
