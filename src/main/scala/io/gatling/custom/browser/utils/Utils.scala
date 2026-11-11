package io.gatling.custom.browser.utils

import io.gatling.core.util.ResourceCache

import java.io.FileNotFoundException
import java.nio.charset.Charset

object Utils extends ResourceCache{


  protected[browser] def readFileFromResources(filePath: String): String = {
    val loadedScript = cachedResource(filePath).map(_.string(Charset.defaultCharset())).toOption
    loadedScript.getOrElse(throw new FileNotFoundException(s"File $filePath not found"))
  }

}
