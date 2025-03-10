package io.gatling.custom.browser.utils

object PlaywrightExceptionParser {

  private final val SPLITTER_STRING = "\nCall log:\n"


  def parseTimeoutErrorMessage(rawErrorMessage: String): Option[String] = {

    val messagePart = rawErrorMessage.split(SPLITTER_STRING)

    if (messagePart.nonEmpty || messagePart.size == 2) {
      Option.apply(s"Timeout exceeded ${messagePart.apply(1).split("\n").apply(0)}")
    }
    else {
      Option.apply(s"action: throw TimeoutError")
    }
  }

  def parseAssertionErrorMessage(rawErrorMessage: String): Option[String] = {

    val messagePart = rawErrorMessage.split(SPLITTER_STRING)

    if (messagePart.nonEmpty || messagePart.size == 2) {
      Option.apply(s"${messagePart.apply(0).split("\n").mkString("; ")}")
    }
    else {
      Option.apply(s"action: throw AssertionError")
    }
  }
}
