# BrowserDsl JavaAPI


## Browser Protocol

Used to configure the Gatling browser protocol.

```java
ProtocolBuilder browserProtocol = BrowserDsl
        .gatlingBrowser()
        // Optional setup block
        .withLaunchOptions(new LaunchOptions().setHeadless(false))
        .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080).setIsMobile(true))
        //
        .buildProtocol();
```

## Action name

```java
// with a static value
BrowserDsl.browserAction("HomePage")
// with a dynamic value computed from a Gatling Expression Language String
BrowserDsl.browserAction("#{actionName}")
// a dynamic value computed from a function
BrowserDsl.browserAction(session -> session.getString("actionName"))
```


## Open action

```java
// with an absolute static url
BrowserDsl.browserAction("name").open("https://docs.gatling.io/")
// with a dynamic value computed from a Gatling Expression Language String
BrowserDsl.browserAction("name").open("#{url}")
// a dynamic value computed from a function
BrowserDsl.browserAction("name").open(session -> session.getString("url")
```

It is possible to set additional navigation options as the second parameter.  
See the [NavigateOptions documentation here](https://javadoc.io/doc/com.microsoft.playwright/playwright/1.46.0/com/microsoft/playwright/Page.NavigateOptions.html).
```java
BrowserDsl.browserAction("name").open("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE))
```

## Flow Action

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.

**Note:** You should always return `browserSession`.

```java
BrowserDsl.browserAction("test").executeFlow((page, browserSession) -> {
    page.navigate("https://docs.gatling.io/");
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible();
    return browserSession;
});
```

For **advanced** example see [this guide](./FlowActionAdvanced.md)

## Clean Context

Provides a way to clean up the Playwright `BrowserContext`.

**Note:** Usually used at the end of a loop.

```java
BrowserDsl.browserCleanContext()
```
