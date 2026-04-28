package io.gatling.custom.browser.stats

import io.gatling.custom.browser.utils.DirUtils

import java.io.{BufferedWriter, FileWriter}
import java.util.concurrent.{ConcurrentLinkedQueue, Executors, TimeUnit}

object UIMetricFileWriter {

  private val latestCreatedSubdirName = DirUtils.latestGatlingRun().get
  private val file = new BufferedWriter(new FileWriter(s"$latestCreatedSubdirName/ui_metrics.csv", true))

  private val queue = new ConcurrentLinkedQueue[String]()

  // Background executor
  private val executor = Executors.newSingleThreadScheduledExecutor()
  executor.scheduleAtFixedRate(() => flushQueue(), 1, 5, TimeUnit.SECONDS)

  protected[browser] def recordMetric(data: String): Unit = {
    queue.offer(data)
  }
  protected[browser] def stop(): Unit = {
    executor.shutdown()
    executor.awaitTermination(3, TimeUnit.SECONDS)
    flushQueue() // write remaining metrics
    file.close()
  }
  private def flushQueue(): Unit = {
    var entry = queue.poll()
    var wrote = false

    while (entry != null) {
      file.write(entry)
      wrote = true
      entry = queue.poll()
    }

    if (wrote) file.flush()
  }
}
