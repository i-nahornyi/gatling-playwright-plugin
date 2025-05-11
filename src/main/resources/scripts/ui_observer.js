(function() {

    window.performanceMetrics = { LCP: 0, CLS: 0 };

    const performanceObservers = {
        'largest-contentful-paint': (entry) => {
            window.performanceMetrics.LCP = entry.startTime || 0;
        },
        'layout-shift': (entry) => {
            if (!entry.hadRecentInput) window.performanceMetrics.CLS += entry.value || 0;
        }
    };

    Object.entries(performanceObservers).forEach(([type, callback]) => {
        new PerformanceObserver((entryList) => {
            entryList.getEntries().forEach(callback);
        }).observe({ type, buffered: true });
    });

    window.getPerformanceMetrics = function() {
        const { timing } = performance;
        const paint = performance.getEntriesByType('paint');

        const getMetric = (name) => paint.find(entry => entry.name === name)?.startTime || 0;

        return {
            FCP: Math.round(getMetric('first-contentful-paint') * 1000) / 1000,
            LCP: Math.round(window.performanceMetrics.LCP * 1000) / 1000,
            CLS: Math.round(window.performanceMetrics.CLS * 1000) / 1000,
            TTFB: timing.responseStart ? timing.responseStart - timing.navigationStart : 0,
            domLoad: timing.domContentLoadedEventEnd ? timing.domContentLoadedEventEnd - timing.navigationStart : 0,
            pageLoadTime: timing.loadEventEnd ? timing.loadEventEnd - timing.navigationStart : 0,
        };
    };
})();
