package io.gatling.custom.browser.model

import io.gatling.commons.stats.Status

object StatusWrapper {
  def OK: Status = io.gatling.commons.stats.OK
  def KO: Status = io.gatling.commons.stats.KO
}
