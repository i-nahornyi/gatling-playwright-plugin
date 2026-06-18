package io.gatling.custom.browser.utils

import com.microsoft.playwright.PlaywrightException
import com.microsoft.playwright.impl.TargetClosedError
import com.typesafe.scalalogging.StrictLogging
import io.gatling.commons.stats.KO
import io.gatling.custom.browser.actor.BrowserWorkerActor.ActionStatus
import org.opentest4j.AssertionFailedError

object PlaywrightExceptionHandler extends StrictLogging {


/* We assume that the standard error message is well-formatted and follows the structure below.
    To build our custom message, we should:

    1. Split the message by `"\nCall log:\n"` into two parts.
    2. Extract the locator from `part[1]`.
    3. Extract the reason from `part[0]`.
    4. Remove delimiters
    5. Construct the final string using both parts.


  Error {
    message='Timeout 30000ms exceeded.
    name='TimeoutError
    stack='TimeoutError: Timeout 30000ms exceeded.
      at _ProgressController.run (C:\temp\playwright-java-9903450257928512556\package\lib\coreBundle.js:12962:32)
      at _FrameDispatcher._runCommand (C:\temp\playwright-java-9903450257928512556\package\lib\coreBundle.js:24019:35)
      at DispatcherConnection.dispatch (C:\temp\playwright-java-9903450257928512556\package\lib\coreBundle.js:24253:44)
  }
  Call log:
  -   - waiting for locator("//*[@id=\"__docusaurus\"]/nav") to be hidden
  -     62 × locator resolved to visible <nav aria-label="Main" class="theme-layout-navbar navbar navbar--fixed-top">…</nav>

 */
 */


  private final val SPLITTER_STRING = "\nCall log:\n"
  private final val REASON_PART_START = "message='"
  private def sanitizeString(targetString: String): String = {
    targetString
      .replace(REASON_PART_START, "")
      .replaceAll("-\\s","")
      .trim
  }

  private def parseErrorMessage(rawErrorMessage: String, errorType: String): Option[String] = {

    val checkIsStandardFormat = rawErrorMessage.contains(SPLITTER_STRING)

    if (checkIsStandardFormat) {
      val messagePart = rawErrorMessage.split(SPLITTER_STRING)

      val reasonText = sanitizeString(messagePart.head.split("\n").apply(1))
      val locatorText = sanitizeString(messagePart.apply(1).split("\n").apply(0))
      Option.apply(s"$reasonText $locatorText")

    }
    else {
      val checkIsCanExtactErrorMessage = rawErrorMessage.contains(REASON_PART_START)

      if (checkIsCanExtactErrorMessage){
        val reasonText = sanitizeString(rawErrorMessage.split("\n").apply(1))
        return Option.apply(s"$reasonText")
      }
      Option.apply(s"Action: throw $errorType")
    }
  }

  /*
=====
  Without actual result:
=====
  Locator expected to be disabled
  Call log:
    - Assert "isDisabled" with timeout 5000ms
    - waiting for locator("//*[@id=\"__docusaurus\"]/nav")
      14 × locator resolved to <nav aria-label="Main" class="theme-layout-navbar navbar navbar--fixed-top">…</nav>
         - unexpected value "enabled"

   */
=====
  With actual result:
=====
  Page title expected to be
  Expected: Error expected title
  Received: Fast and reliable end-to-end testing for modern web apps | Playwright

  Call log:
    - Assert "hasTitle" with timeout 5000ms
      14 × unexpected value "Fast and reliable end-to-end testing for modern web apps | Playwright"
====

  */

  private def parseAssertionErrorMessage(assertionFailedError: AssertionFailedError): Option[String] = {

    val rawErrorMessage = assertionFailedError.getMessage
    val checkIsStandardFormat = rawErrorMessage.contains(SPLITTER_STRING)

    if (checkIsStandardFormat) {
      val messagePart = rawErrorMessage.split(SPLITTER_STRING)

      val headPart = messagePart.apply(0).split("\n").mkString(";")

      Option.apply(s"$headPart")
    }
    else {
      Option.apply(rawErrorMessage)
    }
  }


  def handleException(error: Throwable, requestName: String): ActionStatus = {
    error match {
      case assertionFailedError: AssertionFailedError =>
        logger.debug(s"AssertionFailedError:\nActionName=$requestName\n${assertionFailedError.getMessage}")
        ActionStatus(KO , parseAssertionErrorMessage(assertionFailedError))

      case targetClosedError: TargetClosedError =>
        logger.debug(s"TargetClosedError:\nActionName=$requestName\n${targetClosedError.getMessage}")
        ActionStatus(KO , Some("Target page, context or browser has been closed"))

      case playwrightException: PlaywrightException =>
        logger.debug(s"PlaywrightException:\nActionName=$requestName\n${playwrightException.getMessage}")
        ActionStatus(KO , parseErrorMessage(playwrightException.getMessage, playwrightException.getClass.getSimpleName))

      case throwable: Throwable =>
        logger.debug(s"Browser action crashed:\nActionName=$requestName\n${throwable.getMessage}")
        ActionStatus(KO, Some(s"crashed with ${throwable.getMessage}"), isCrashed = true)
    }
  }
}
