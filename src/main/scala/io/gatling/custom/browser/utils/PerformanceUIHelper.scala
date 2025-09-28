package io.gatling.custom.browser.utils

import com.fasterxml.jackson.databind.{JsonMappingException, ObjectMapper}
import com.microsoft.playwright.{Page, PlaywrightException}
import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.Predef.Status
import io.gatling.core.util.ResourceCache
import io.gatling.custom.browser.model.PageLoadValidator
import io.gatling.custom.browser.protocol.DefaultProtocolOptions
import io.gatling.custom.browser.stats.UIMetricFileWriter

import java.util

object PerformanceUIHelper extends ResourceCache with StrictLogging {

  private final val objectMapper = new ObjectMapper()
  private final val SEPARATOR = ","

  ///* UI SCRIPTS
  private val webVitalsJS: String = Utils.readFileFromResources("scripts/webVitals.js")
  private val pageCompleteCheckByInactivityJS = Utils.readFileFromResources("scripts/pageCompleteCheckByInactivity.js")
  ///*

  protected [browser] val defaultPageLoadValidator: PageLoadValidator = PageLoadValidator(pageCompleteCheckByInactivityJS,
    DefaultProtocolOptions.defaultResourceInactivityTime,
    DefaultProtocolOptions.defaultWaitPageLoadOptions
  )

  def injectMetricTrackingScript(page: Page): Unit = {
      page.addInitScript(webVitalsJS)
  }

  def checkIsPageLoaded(page: Page, pageLoadValidator: PageLoadValidator): Unit = {
    try {
      page.waitForFunction(pageLoadValidator.expression, pageLoadValidator.arg, pageLoadValidator.options)
    }
    catch {
      case playwrightException: PlaywrightException =>
        logger.trace(f"Script executed with failure: ${playwrightException.getMessage}")
        logger.trace(pageLoadValidator.toString)
        throw playwrightException
    }

  }

  def reportUIMetrics(timestamp: Long, requestName: String, page: Page, status: Status): Unit = {

    val metricsMap = extractMetricsFromBrowser(page)

    if(metricsMap.isEmpty){
      logger.trace(s"No web-vitals metrics collected for page ---> $requestName")
    }
    else {
      metricsMap.forEach((key, value) => {
        logger.trace(s"reported $key metric for page ---> $requestName ")
        val csvString = generateCsvMetricString(timestamp, requestName, status, key, value)
        UIMetricFileWriter.recordMetric(csvString)
      })
    }

  }

  private def generateCsvMetricString(timestamp: Long, requestName: String, status: Status, key: String, value: AnyVal): String = {
    s"WEB_VITALS$SEPARATOR$timestamp$SEPARATOR$requestName$SEPARATOR${status.name}$SEPARATOR$key$SEPARATOR$value\n"
  }

  private def extractMetricsFromBrowser(page: Page): util.HashMap[String,AnyVal] = {
    try {
      val metricsJsonString = page.evaluate("JSON.stringify(getPerformanceMetrics())").asInstanceOf[String]
      val metricMap = objectMapper.readValue(metricsJsonString, classOf[util.HashMap[String, AnyVal]])

      if (metricMap == null){
        throw new NullPointerException("MetricMap is null injected script is broken")
      }
      metricMap
    }
    catch {
      case nullPointerException: NullPointerException =>
        logger.debug(nullPointerException.getMessage)
        new util.HashMap[String, AnyVal]()
      case jsonMappingException: JsonMappingException =>
        logger.debug(f"Cant deserialize web-vitals metrics: ${jsonMappingException.getMessage}")
        new util.HashMap[String, AnyVal]()
      case playwrightException: PlaywrightException =>
        logger.debug(f"Cant extract web-vitals metrics from browser: ${playwrightException.getMessage}")
        new util.HashMap[String, AnyVal]()
    }
  }

}
