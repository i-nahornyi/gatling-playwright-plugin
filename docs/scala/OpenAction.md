# Open Action

Navigates the current page to a URL. Tracked as a normal Gatling request/response.

```scala
// with an absolute static url
browserAction("HomePage").open("https://docs.gatling.io/")
// with a dynamic value computed from a Gatling Expression Language String
browserAction("name").open("#{url}")
// a dynamic value computed from a function
browserAction("name").open(session => session("url").as[String])
```

It is possible to set additional **navigation** options or page load **validation**.

## Navigation Options

See the NavigateOptions documentation [here](https://javadoc.io/doc/com.microsoft.playwright/playwright/1.62.0/com/microsoft/playwright/Page.NavigateOptions.html).
```scala
browserAction("name").open("https://docs.gatling.io/").withNavigateOptions(new Page.NavigateOptions().setWaitUntil(LOAD))
```

## Load Validations

See the LoadValidations documentations [here](https://playwright.dev/java/docs/api/class-page#page-wait-for-function)
```scala
val validationScript =
  """
            (function () {
              const el = document.querySelector('[data-test-id="chat-widget-iframe"]');
              return document.readyState === "complete" &&
                     el !== null &&
                     el.getBoundingClientRect().width > 0 &&
                     el.getBoundingClientRect().height > 0;
            })();
  """

val pageLoadValidator = PageLoadValidator(validationScript, null, new Page.WaitForFunctionOptions().setPollingInterval(100).setTimeout(30000))

browserAction("HomePage").open("https://gatling.io/").withLoadValidations(pageLoadValidator)
```

You can use it with default script that check [pageCompleteCheckByInactivity](https://github.com/sitespeedio/browsertime/blob/main/lib/core/pageCompleteChecks/pageCompleteCheckByInactivity.js)
similar that make sitespeed tool
```scala
browserAction("name").open("https://docs.gatling.io/").withLoadValidations()
```

**Timeouts:** see [Timeouts](./Timeouts.md) — `open` is bounded by Playwright's own navigation timeout, not
a plugin-level one.

---
[← API overview](./Api.md) · [Flow Action](./FlowAction.md) · [Browser Session Function](./BrowserSessionFunction.md) · [Clean Context](./CleanContext.md)
