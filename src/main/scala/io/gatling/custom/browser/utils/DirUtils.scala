package io.gatling.custom.browser.utils

import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters.IteratorHasAsScala

object DirUtils {

  private val mvnBasePath    = Paths.get("target/gatling")
  private val gradleBasePath = Paths.get("reports/gatling")

  // List all subdirectories of a given path (if exists)
  private def listSubDirs(base: Path): Seq[Path] = {
    if (Files.exists(base) && Files.isDirectory(base)) {
      val stream = Files.list(base)
      try {
        stream.iterator().asScala
          .filter(Files.isDirectory(_))
          .toSeq
      } finally {
        stream.close()
      }
    } else Seq.empty
  }

  // Find latest subdirectory in a given path
  private def latestSubDir(base: Path): Option[Path] = {
    val subs = listSubDirs(base)
    if (subs.isEmpty) None
    else {
      Some(
        subs.maxBy { p =>
          val attrs = Files.readAttributes(p, classOf[BasicFileAttributes])
          val ctime = attrs.creationTime().toMillis
          if (ctime > 0) ctime else attrs.lastModifiedTime().toMillis
        }
      )
    }
  }

  // Check both Maven and Gradle base paths
  def latestGatlingRun(): Option[Path] = {
    val mvnLatest    = latestSubDir(mvnBasePath)
    val gradleLatest = latestSubDir(gradleBasePath)

    (mvnLatest, gradleLatest) match {
      case (Some(m), Some(g)) =>
        // pick the newer one
        val mTime = Files.readAttributes(m, classOf[BasicFileAttributes]).creationTime().toMillis
        val gTime = Files.readAttributes(g, classOf[BasicFileAttributes]).creationTime().toMillis

        if (mTime >= gTime) Some(m) else Some(g)

      case (Some(m), None) => Some(m)
      case (None, Some(g)) => Some(g)
      case _               => None
    }
  }
}
