# BrowserDsl JavaAPI

## Action name

```java
// with a static value
browserAction("HomePage")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("#{actionName}")
// a dynamic value computed from a function
browserAction(session -> session.getString("actionName"))
```


## Open action

```java
// with an absolute static url
browserAction("name").open("https://docs.gatling.io/")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("name").open("#{url}")
// a dynamic value computed from a function
browserAction("name").open(session -> session.getString("url")
```

It is possible to set additional navigation options as the second parameter.  
See the [NavigateOptions documentation here](https://javadoc.io/doc/com.microsoft.playwright/playwright/1.46.0/com/microsoft/playwright/Page.NavigateOptions.html).
```java
browserAction("name").open("https://docs.gatling.io/", new NavigateOptions().setWaitUntil(NETWORKIDLE))
```

## Flow Action

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.

**Note:** You should always return `browserSession`.

```java
browserAction("test").executeFlow((page, browserSession) -> {
    page.navigate("https://docs.gatling.io/");
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible();
    return browserSession;
});
```

For **advanced** example see [this guide](./FlowActionAdvanced.md)