# Browser Session Function

Allows manipulation of a page without tracking it in reports.

**Note:** Typically used to extract data and store it in Gatling sessions.

```java
BrowserDsl.browserSessionFunction((page, browserSession) -> {
    Session session = browserSession.getGatlingSession().set("pageTitle", page.title());
    return browserSession.updateBrowserSession(session);
});
```

---
[← API overview](./Api.md) · [BrowserSession](./BrowserSession.md) · [Open Action](./OpenAction.md) · [Flow Action](./FlowAction.md) · [Clean Context](./CleanContext.md)
