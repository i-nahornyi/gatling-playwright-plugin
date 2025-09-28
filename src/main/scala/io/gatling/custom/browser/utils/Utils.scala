package io.gatling.custom.browser.utils

import io.gatling.core.util.ResourceCache

import java.nio.charset.Charset

object Utils extends ResourceCache{


  protected[browser] def readFileFromResources(filePath: String): String = {
    cachedResource(filePath).map(_.string(Charset.defaultCharset())).toOption.get
  }

}
