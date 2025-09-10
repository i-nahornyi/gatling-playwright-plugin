# BrowserDsl ScalaAPI

Add the following import:

```scala
import io.gatling.custom.browser.Predef._
```

## Browser Protocol

Used to configure the Gatling browser protocol.

```scala
val browserProtocol: Protocol = gatlingBrowser
    //// This part of setup block is optional
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))
    .enableUIMetrics()
    ////
    .buildProtocol()
```

## Action name

```scala
// with a static value
browserAction("HomePage")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("#{actionName}")
// a dynamic value computed from a function
browserAction(session => session("actionName").as[String])
```


## Open action

```scala
// with an absolute static url
browserAction("HomePage").open("https://docs.gatling.io/")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("name").open("#{url}")
// a dynamic value computed from a function
browserAction("name").open(session => session("url").as[String])
```

It is possible to set additional navigation options as the second parameter.  
See the [NavigateOptions documentation here](https://javadoc.io/doc/com.microsoft.playwright/playwright/1.46.0/com/microsoft/playwright/Page.NavigateOptions.html).
```scala
browserAction("name").open("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE))
```

## Flow Action

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.

**Note:** You should always return `browserSession`.

```scala
browserAction("test").executeFlow((page,browserSession) => {
    page.navigate("https://docs.gatling.io/")
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible()
    browserSession
})
```

For **advanced** example see [this guide](./FlowActionAdvanced.md)

## Browser Session Function

Allows manipulation of a page without tracking it in reports.

**Note:** Typically used to extract data and store it in Gatling sessions.

```scala
browserSessionFunction((page, browserSession) => {
      val session = browserSession.getScalaSession().set("pageTitle",page.title())
      browserSession.updateBrowserSession(session)
});
```


## Clean Context

Provides a way to clean up the Playwright `BrowserContext`.

**Note:** Usually used at the end of a loop.

```scala
browserCleanContext()
```
