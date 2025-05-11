package io.gatling.custom.browser.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.playwright.Page
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.validation.Success
import io.gatling.core.util.ResourceCache

import java.nio.charset.Charset
import java.util

object PerformanceUIHelper extends ResourceCache with StrictLogging {

  private final val objectMapper = new ObjectMapper()
  private final val SEPARATOR = ","
  private val UI_OBSERVER_JS = cachedResource("scripts/ui_observer.js") match {
    case Success(resource) => resource.string(Charset.defaultCharset())
  }

  def injectUIPolyfill(page: Page): Unit = {
    page.addInitScript(UI_OBSERVER_JS)
  }

  /// TODO: refactor in future
  def reportUIMetrics(timestamp: Long, requestName: String, page: Page): Unit = {

    /*
          "FCP" -> {Integer@6833} 233
          "LCP" -> {Double@6835} 119.90000009536743
          "CLS" -> {Double@6837} 0.35199003522702943
          "TTFB" -> {Integer@6839} 53
          "DomLoad" -> {Integer@6841} 334
          "PageLoad" -> {Integer@6843} 429
    */
    val metricsJsonString = page.evaluate("JSON.stringify(getPerformanceMetrics())").asInstanceOf[String]
    val metricsMap = objectMapper.readValue(metricsJsonString, classOf[util.HashMap[String, AnyVal]])
    metricsMap.forEach((key, value) => logger.info(s"WEB_VITALS$SEPARATOR$timestamp$SEPARATOR$requestName$SEPARATOR$key$SEPARATOR$value"))

  }

}
