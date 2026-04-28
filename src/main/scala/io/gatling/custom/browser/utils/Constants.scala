package io.gatling.custom.browser.utils

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Constants {
  val BROWSER_ACTOR_POOL_NAME = "playwrightBrowser"
  val PROMISE_TIMEOUT: FiniteDuration = Int.MaxValue.seconds
}
