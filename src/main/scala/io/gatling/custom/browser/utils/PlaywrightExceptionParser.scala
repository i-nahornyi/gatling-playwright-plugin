package io.gatling.custom.browser.utils

object PlaywrightExceptionParser {



  /*

  We assume that the standard error message is well-formatted and follows the structure below.
  To build our custom message, we should:

  1. Split the message by `"\nCall log:\n"` into two parts.
  2. Extract the locator from `part[1]`.
  3. Extract the reason from `part[0]`.
  4. Construct the final string using both parts.

  Error {
    message='Timeout 3000ms exceeded.
    name='TimeoutError
    stack='TimeoutError: Timeout 3000ms exceeded.
      at ProgressController.run (/private/var/folders/z4/bnlz_rt90h99pq7ylkp0_6hw0000gn/T/playwright-java-3427629845495357508/package/lib/server/progress.js:78:26)
  }
  Call log:
  - waiting for locator("#main-content") to be hidden
  -   locator resolved to visible <main class="body" id="main-content">…</main>
  -   locator resolved to visible <main class="body" id="main-content">…</main>
  */

  private final val SPLITTER_STRING = "\nCall log:\n"
  private final val REASON_PART_START = "message='"

  def parseErrorMessage(rawErrorMessage: String, errorType: String): Option[String] = {

    val checkIsStandardFormat = rawErrorMessage.contains(SPLITTER_STRING)

    if (checkIsStandardFormat) {
      val messagePart = rawErrorMessage.split(SPLITTER_STRING)

      val reasonText = messagePart.head.split("\n").apply(1).trim.replace(REASON_PART_START, "")
      val locatorText = messagePart.apply(1).split("\n").apply(0)
      Option.apply(s"$reasonText $locatorText")

    }
    else {
      val checkIsCanExtactErrorMessage = rawErrorMessage.contains(REASON_PART_START)

      if (checkIsCanExtactErrorMessage){
        val reasonText = rawErrorMessage.split("\n").apply(1).trim.replace(REASON_PART_START, "")
        return Option.apply(s"$reasonText")
      }
      Option.apply(s"action: throw $errorType")
    }
  }

  /*

  Without actual result:
=====
  Locator expected to be disabled
  Call log:
  Locator.expect with timeout 5000ms
  waiting for locator("locator")
    locator resolved to <nav aria-label="Main" class="navbar navbar--fixed-top">…</nav>
    unexpected value "enabled"
=====
  With actual result:
=====
  Page title expected to be: Error expected title
  Received: Fast and reliable end-to-end testing for modern web apps | Playwright

  Call log:
  Locator.expect with timeout 5000ms
  waiting for locator(":root")
    locator resolved to <html lang="en" dir="ltr" data-theme="light" data-has-hydrated="" class="plugin-pages plugin-id-default" data-rh="lang,dir,class,data-has-hydrated">…</html>
    unexpected value "Fast and reliable end-to-end testing for modern web apps | Playwright"
====

  */

  def parseAssertionErrorMessage(rawErrorMessage: String): Option[String] = {

    val checkIsStandardFormat = rawErrorMessage.contains(SPLITTER_STRING)

    if (checkIsStandardFormat) {
      val messagePart = rawErrorMessage.split(SPLITTER_STRING)

      val headPart = messagePart.apply(0).split("\n").mkString("; ")

      val errorPart = messagePart.apply(1).split("\n").apply(0)
      val locatorPart = messagePart.apply(1).split("\n").apply(1)

      Option.apply(s"$headPart === $errorPart - $locatorPart")
    }
    else {
      Option.apply(rawErrorMessage)
    }
  }
}
