# Flow Action Example

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.

You can use `BrowserSession`, which acts as a wrapper for the default Gatling session and provides methods for storing and retrieving values.

```scala
def exampleFlow(page: Page, browserSession: BrowserSession): BrowserSession = {
    page.navigate("https://docs.gatling.io/")
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible()

    // Resolve EL value from the session. For available options, see:
    // https://docs.gatling.io/reference/script/core/session/el/
    val valueFromSession = browserSession.resolveSessionValue("#{url.exists()}")

    // Another example of resolving a session expression
    val modifiedValueFromSession = browserSession.resolveSessionValue(session => s"${session("url").as[String]}updated")

    page.navigate("https://playwright.dev/java/docs/debug")
    
    // Updating the session
    val session: Session = browserSession.getScalaSession().set("your_args", page.title())
    browserSession.updateBrowserSession(session)
}
```

You can manually measure action time using built-in functions.

```scala
def exampleFlow(page: Page, browserSession: BrowserSession): BrowserSession = {
    page.navigate("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE))
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible()

    /// How to evaluate JS in playwrights see ===> https://playwright.dev/java/docs/evaluating
    def timing: Map[String, Double] = page.evaluate("performance.timing").asInstanceOf[java.util.Map[String, Double]].asScala.toMap

    browserSession.setActionStartTime(timing("navigationStart").asInstanceOf[Long])
    browserSession.setActionEndTime(timing("loadEventEnd").asInstanceOf[Long])

    browserSession
}
```

## Handling Errors and Setting Action Status

You can handle errors and set the status of an action using `try-catch` blocks.

```scala
def exampleFlow(page: Page, browserSession: BrowserSession): BrowserSession = {
    try {
      page.navigate("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE))
      page.locator("//*[@id=\"ai-initial-message\"]").isVisible()
      
      PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText")
    }
    catch {
      case _: AssertionFailedError => browserSession.setStatusKO("Your custom textAssertion")
    }
    browserSession
}
```