package io.gatling.custom.browser.stats

import io.gatling.custom.browser.utils.DirUtils

import java.io.{BufferedWriter, FileWriter}

object UIMetricFileWriter {


  private val latestCreatedSubdirName = DirUtils.latestGatlingRun().get

  private val file = new BufferedWriter(new FileWriter(s"$latestCreatedSubdirName/ui_metrics.csv", true))

  protected [browser] def recordMetric(data: String): Unit = {
    file.write(data)
    file.flush()
  }

  protected [browser] def closeFile(): Unit = {
    file.close()
  }

}
