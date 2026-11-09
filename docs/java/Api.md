# BrowserDsl JavaAPI


## Browser Protocol

Used to configure the Gatling browser protocol.

```java
ProtocolBuilder browserProtocol = BrowserDsl
        .gatlingBrowser()
        // Optional setup block
        .withLaunchOptions(new LaunchOptions().setHeadless(false))
        .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080).setIsMobile(true))
        .enableUIMetrics()
        .withDefaultTimeout(15000)
        //
        .buildProtocol();
```

`.withDefaultTimeout(millis)` sets a default action/navigation timeout for every browser action — see
[Timeouts](./Timeouts.md) for how it interacts with `open`/`executeFlow`.

## Action name

```java
// with a static value
BrowserDsl.browserAction("HomePage")
// with a dynamic value computed from a Gatling Expression Language String
BrowserDsl.browserAction("#{actionName}")
// a dynamic value computed from a function
BrowserDsl.browserAction(session -> session.getString("actionName"))
```

## Actions

| Action                                                   | Description                                                    |
|-----------------------------------------------------------|-----------------------------------------------------------------|
| [Open](./OpenAction.md)                                    | Navigate to a URL — tracked as a normal request                 |
| [Flow](./FlowAction.md)                                    | Run a scripted Playwright flow — tracked as a normal request     |
| [Browser Session Function](./BrowserSessionFunction.md)   | Manipulate the page without tracking it in reports              |
| [Clean Context](./CleanContext.md)                         | Clean up the Playwright `BrowserContext`                        |

See also: [BrowserSession](./BrowserSession.md) — the object passed into `executeFlow`/`browserSessionFunction`
callbacks — and [Timeouts](./Timeouts.md).
