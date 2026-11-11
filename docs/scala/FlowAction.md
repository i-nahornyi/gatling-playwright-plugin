# Flow Action

Sometimes, you need to verify that a page has loaded programmatically. In such cases, you can use a script.
Tracked as a normal Gatling request/response.

**Note:** You should always return `browserSession`.

```scala
browserAction("test").executeFlow((page,browserSession) => {
    page.navigate("https://docs.gatling.io/")
    page.locator("//*[@id=\"ai-initial-message\"]").isVisible()
    browserSession
})
```

**Timeouts:** see [Timeouts](./Timeouts.md) — the same Playwright-native timeout rules apply to calls you
make on `page` inside `executeFlow`, and callbacks must avoid blocking outside of them.

For **advanced** example see [this guide](./FlowActionAdvanced.md)

---
[← API overview](./Api.md) · [BrowserSession](./BrowserSession.md) · [Open Action](./OpenAction.md) · [Browser Session Function](./BrowserSessionFunction.md) · [Clean Context](./CleanContext.md)
