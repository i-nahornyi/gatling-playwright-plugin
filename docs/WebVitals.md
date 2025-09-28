# UI Metrics

UI metrics are collected only for the **Open** action.

---

## 1. Enable UI Metrics in Protocol Builder

Ensure UI metrics are enabled in your protocol builder:

```java
ProtocolBuilder browserProtocol = BrowserDsl.gatlingBrowser()
        .enableUIMetrics()
        .buildProtocol();
```

---

## 2. Check the Log File

UI metrics are stored in folder with report and use following format:

```csv
WEB_VITALS,1757624157101,homePage,OK,FCP,1042
WEB_VITALS,1757624157101,homePage,OK,LCP,1042
WEB_VITALS,1757624157101,homePage,OK,CLS,0.02
WEB_VITALS,1757624157101,homePage,OK,TTFB,87
```

**File Structure:**
`MetricType, timestamp, actionName, status, metricName, value`