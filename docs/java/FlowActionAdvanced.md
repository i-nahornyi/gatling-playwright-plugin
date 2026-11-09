# Flow Action Example

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.
This page assumes you're already familiar with the basics in [Flow Action](./FlowAction.md).

You can use `BrowserSession`, which acts as a wrapper for the default Gatling session and provides methods for storing and retrieving values.

## Reading and Updating Session Values

```java
BiFunction<Page, BrowserSession, BrowserSession> exampleFlow = (page, browserSession) -> {
    page.navigate("https://docs.gatling.io/");
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible();

    // Resolve EL value from the session. For available options, see:
    // https://docs.gatling.io/reference/script/core/session/el/
    Boolean valueFromSession = (Boolean) browserSession.resolveSessionValue("#{url.exists()}");

    // Another example of resolving a session expression
    String anotherValueFromSession = browserSession.resolveSessionValue(s -> s.getString("url") + "updated").toString();

    // Updating the session
    Session session = browserSession.getGatlingSession().set("your_args", page.title());
    return browserSession.updateBrowserSession(session);
};
```

## Measuring Action Timing

You can manually measure action time using built-in functions.

```java
BiFunction<Page, BrowserSession, BrowserSession> exampleFlow = (page, browserSession) -> {
    page.navigate("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE));
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible();

    // How to evaluate JS in Playwright, see: https://playwright.dev/java/docs/evaluating
    Map<String, Double> timing = (Map<String, Double>) page.evaluate("performance.timing");

    browserSession.setActionStartTime(timing.get("navigationStart").longValue());
    browserSession.setActionEndTime(timing.get("loadEventEnd").longValue());

    return browserSession;
};
```

## Handling Errors and Setting Action Status

You can handle errors and set the status of an action using `try-catch` blocks.

```java
BiFunction<Page, BrowserSession, BrowserSession> exampleFlow = (page, browserSession) -> {
    try {
        page.navigate("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE));
        page.locator("//*[@id=\"ai-initial-message\"]").isVisible();

        // Perform an assertion
        PlaywrightAssertions.assertThat(page).hasTitle("FailedAssertionText");
    } catch (AssertionFailedError e) {
        // Set a custom failure status if the assertion fails
        browserSession.setStatusKO("Your custom textAssertion");
    }
    return browserSession;
};
```

---
[← Flow Action](./FlowAction.md) · [BrowserSession](./BrowserSession.md) · [API overview](./Api.md)
