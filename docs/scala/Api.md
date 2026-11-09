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
    .withDefaultTimeout(15000)
    ////
```

`.withDefaultTimeout(millis)` sets a default action/navigation timeout for every browser action — see
[Timeouts](./Timeouts.md) for how it interacts with `open`/`executeFlow`.

> `.buildProtocol()` is **deprecated** and no longer needed — a `BrowserProtocolBuilder` implicitly
> converts to a `Protocol` wherever one is expected (as above, via the `val`'s type annotation). It
> still works if called explicitly, just unnecessary going forward.

## Action name

```scala
// with a static value
browserAction("HomePage")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("#{actionName}")
// a dynamic value computed from a function
browserAction(session => session("actionName").as[String])
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
