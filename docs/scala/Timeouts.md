# Timeouts

The plugin does not impose a timeout of its own on `open`/`executeFlow` — a stuck browser action is bounded
by whatever timeout the underlying Playwright call is using, the same as in plain Playwright. There is no
separate plugin-level timeout to configure.

## Protocol-wide default

`.withDefaultTimeout(millis)` on the protocol builder is a convenience that forwards to Playwright's own
`BrowserContext.setDefaultTimeout`/`setDefaultNavigationTimeout` on every context the plugin creates, so you
don't have to repeat `NavigateOptions.setTimeout(...)` on every `open()` call.

```scala
val browserProtocol: Protocol = gatlingBrowser
    .withContextOptions(new NewContextOptions().setViewportSize(1920, 1080))
    .withLaunchOptions(new LaunchOptions().setHeadless(false))
    .withDefaultTimeout(15000)
    .buildProtocol()
```

## Overriding it for a single `open()` call

```scala
// 5s instead of Playwright's 30s default / the protocol's withDefaultTimeout
browserAction("name").open("https://docs.gatling.io/")
  .withNavigateOptions(new Page.NavigateOptions().setTimeout(5000))
```

A navigation that exceeds this timeout throws a Playwright `TimeoutError`, which the plugin reports as a KO
on this action rather than hanging the virtual user.

## Overriding it for an individual call inside `executeFlow`

Playwright calls you make on `page` inside `executeFlow` (clicks, fills, `waitFor*`, ...) keep their own
default timeout — either Playwright's built-in 30s, or whatever `.withDefaultTimeout(...)` set — unless you
override it per call, same as plain Playwright:

```scala
browserAction("test").executeFlow((page, browserSession) => {
  page.navigate("https://docs.gatling.io/")
  page.locator("//*[@id=\"ai-initial-message\"]").click(new Locator.ClickOptions().setTimeout(5000))
  browserSession
})
```

**Practical takeaways:** avoid `setTimeout(0)` in load tests (it disables the safety net Playwright already
gives you for free), and don't block inside an `executeFlow` callback with anything that isn't a
timeout-aware Playwright call.
