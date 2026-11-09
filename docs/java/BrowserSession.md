# BrowserSession

`BrowserSession` is the object passed into your `executeFlow`/`browserSessionFunction` callbacks (see
[Flow Action](./FlowAction.md) and [Browser Session Function](./BrowserSessionFunction.md)). It wraps the
current Gatling `Session` and adds a few browser-action-specific extras: resolving Gatling EL expressions,
overriding the reported timing of the action, and setting its OK/KO status by hand.

**Gatling sessions are immutable**, and `BrowserSession` follows the same model: none of its methods mutate
it in place. To persist any session change you make, call `updateBrowserSession(...)` and return the
`BrowserSession` it gives back — the same rule as returning a new `Session` from a plain Gatling `exec`.

```java
BrowserDsl.browserAction("test").executeFlow((page, browserSession) -> {
    Session updated = browserSession.getGatlingSession().set("your_args", "value");
    return browserSession.updateBrowserSession(updated); // <- must return this, not browserSession itself
});
```

## Reading and writing the Gatling session

| Method                                  | Returns          | Description                                                                                                             |
|-----------------------------------------|------------------|-------------------------------------------------------------------------------------------------------------------------|
| `getGatlingSession()`                   | `Session`        | The current Gatling session, read-only.                                                                                 |
| `updateBrowserSession(Session session)` | `BrowserSession` | A new `BrowserSession` wrapping `session`. This is the only way to persist a session change — always return the result. |

## Resolving values from the session

| Method                                                    | Returns  | Description                                                                                                                                  |
|-----------------------------------------------------------|----------|----------------------------------------------------------------------------------------------------------------------------------------------|
| `resolveSessionValue(String elExpression)`                | `Object` | Resolves a [Gatling EL](https://docs.gatling.io/reference/script/core/session/el/) expression, e.g. `"#{url}"`, against the current session. |
| `resolveSessionValue(Function<Session, Object> function)` | `Object` | Resolves a value by running an arbitrary function against the current session.                                                               |

```java
Boolean exists = (Boolean) browserSession.resolveSessionValue("#{url.exists()}");
String updated = browserSession.resolveSessionValue(s -> s.getString("url") + "-suffix").toString();
```

## Overriding action timing

By default, an action's reported start/end time is when the plugin actually started/finished it. Use these
if you want to report different timestamps instead — for example, timings read from
`page.evaluate("performance.timing")` (see [advanced Flow Action examples](./FlowActionAdvanced.md)).

| Method                               | Description                                                         |
|--------------------------------------|---------------------------------------------------------------------|
| `setActionStartTime()`               | Sets the reported start time to now.                                |
| `setActionStartTime(long timestamp)` | Sets the reported start time to an explicit epoch-millis timestamp. |
| `getActionStartTime()`               | Returns the previously set start time, or `0` if never set.         |
| `setActionEndTime()`                 | Sets the reported end time to now.                                  |
| `setActionEndTime(long timestamp)`   | Sets the reported end time to an explicit epoch-millis timestamp.   |
| `getActionEndTime()`                 | Returns the previously set end time, or `0` if never set.           |

## Setting the action status

| Method                             | Description                                                             |
|------------------------------------|-------------------------------------------------------------------------|
| `setStatusOK()`                    | Marks this action as successful.                                        |
| `setStatusKO()`                    | Marks this action as failed, with no custom error message.              |
| `setStatusKO(String errorMessage)` | Marks this action as failed with a custom error message.                |
| `getStatus()`                      | Returns the `Status` (`OK`/`KO`) that will be reported for this action. |
| `getErrorMessage()`                | Returns the error message set via `setStatusKO(String)`, if any.        |

`getErrorMessage()` returns a Scala `scala.Option<String>` rather than a plain (possibly-`null`) `String`,
since the underlying implementation is shared with the Scala API. From Java, check `isDefined()` before
`get()`, or rely on it being defined right after you called `setStatusKO(message)` yourself:

```java
browserSession.setStatusKO("custom failure reason");
if (browserSession.getErrorMessage().isDefined()) {
    String message = browserSession.getErrorMessage().get();
}
```

Setting a KO status here does **not** stop your callback or throw — code after `setStatusKO(...)` still
runs, and it doesn't retroactively mark `browserSession.getGatlingSession().isFailed()` as `true` either;
the plugin only applies that to the Gatling session *after* your callback returns. If you want to skip the
rest of the current callback once you've called `setStatusKO(...)`, use an ordinary early `return`.

`getGatlingSession().isFailed()` is still useful, but across actions rather than within one: check it at
the top of a *later* `executeFlow` in the same scenario to skip risky work once an *earlier* action already
failed — see the guard pattern in [advanced Flow Action examples](./FlowActionAdvanced.md).

---
[← API overview](./Api.md) · [Flow Action](./FlowAction.md) · [Browser Session Function](./BrowserSessionFunction.md)
