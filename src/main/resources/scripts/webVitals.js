/// * This script uses the web-vitals library (https://github.com/GoogleChrome/web-vitals)
/// * Copyright 2020 Google LLC

"use strict";
(() => {
  // dist/modules/lib/bfcache.js
  var bfcacheRestoreTime = -1;
  var getBFCacheRestoreTime = () => bfcacheRestoreTime;
  var onBFCacheRestore = (cb) => {
    addEventListener("pageshow", (event) => {
      if (event.persisted) {
        bfcacheRestoreTime = event.timeStamp;
        cb(event);
      }
    }, true);
  };

  // dist/modules/lib/bindReporter.js
  var getRating = (value, thresholds) => {
    if (value > thresholds[1]) {
      return "poor";
    }
    if (value > thresholds[0]) {
      return "needs-improvement";
    }
    return "good";
  };
  var bindReporter = (callback, metric, thresholds, reportAllChanges) => {
    let prevValue;
    let delta;
    return (forceReport) => {
      if (metric.value >= 0) {
        if (forceReport || reportAllChanges) {
          delta = metric.value - (prevValue ?? 0);
          if (delta || prevValue === void 0) {
            prevValue = metric.value;
            metric.delta = delta;
            metric.rating = getRating(metric.value, thresholds);
            callback(metric);
          }
        }
      }
    };
  };

  // dist/modules/lib/doubleRAF.js
  var doubleRAF = (cb) => {
    requestAnimationFrame(() => requestAnimationFrame(() => cb()));
  };

  // dist/modules/lib/generateUniqueID.js
  var generateUniqueID = () => {
    return `v5-${Date.now()}-${Math.floor(Math.random() * (9e12 - 1)) + 1e12}`;
  };

  // dist/modules/lib/getNavigationEntry.js
  var getNavigationEntry = () => {
    const navigationEntry = performance.getEntriesByType("navigation")[0];
    if (navigationEntry && navigationEntry.responseStart > 0 && navigationEntry.responseStart < performance.now()) {
      return navigationEntry;
    }
  };

  // dist/modules/lib/getActivationStart.js
  var getActivationStart = () => {
    const navEntry = getNavigationEntry();
    return navEntry?.activationStart ?? 0;
  };

  // dist/modules/lib/initMetric.js
  var initMetric = (name, value = -1) => {
    const navEntry = getNavigationEntry();
    let navigationType = "navigate";
    if (getBFCacheRestoreTime() >= 0) {
      navigationType = "back-forward-cache";
    } else if (navEntry) {
      if (document.prerendering || getActivationStart() > 0) {
        navigationType = "prerender";
      } else if (document.wasDiscarded) {
        navigationType = "restore";
      } else if (navEntry.type) {
        navigationType = navEntry.type.replace(/_/g, "-");
      }
    }
    const entries = [];
    return {
      name,
      value,
      rating: "good",
      // If needed, will be updated when reported. `const` to keep the type from widening to `string`.
      delta: 0,
      entries,
      id: generateUniqueID(),
      navigationType
    };
  };

  // dist/modules/lib/initUnique.js
  var instanceMap = /* @__PURE__ */ new WeakMap();
  function initUnique(identityObj, ClassObj) {
    if (!instanceMap.get(identityObj)) {
      instanceMap.set(identityObj, new ClassObj());
    }
    return instanceMap.get(identityObj);
  }

  // dist/modules/lib/LayoutShiftManager.js
  var LayoutShiftManager = class {
    _onAfterProcessingUnexpectedShift;
    _sessionValue = 0;
    _sessionEntries = [];
    _processEntry(entry) {
      if (entry.hadRecentInput)
        return;
      const firstSessionEntry = this._sessionEntries[0];
      const lastSessionEntry = this._sessionEntries.at(-1);
      if (this._sessionValue && firstSessionEntry && lastSessionEntry && entry.startTime - lastSessionEntry.startTime < 1e3 && entry.startTime - firstSessionEntry.startTime < 5e3) {
        this._sessionValue += entry.value;
        this._sessionEntries.push(entry);
      } else {
        this._sessionValue = entry.value;
        this._sessionEntries = [entry];
      }
      this._onAfterProcessingUnexpectedShift?.(entry);
    }
  };

  // dist/modules/lib/observe.js
  var observe = (type, callback, opts = {}) => {
    try {
      if (PerformanceObserver.supportedEntryTypes.includes(type)) {
        const po2 = new PerformanceObserver((list) => {
          Promise.resolve().then(() => {
            callback(list.getEntries());
          });
        });
        po2.observe({ type, buffered: true, ...opts });
        return po2;
      }
    } catch {
    }
    return;
  };

  // dist/modules/lib/runOnce.js
  var runOnce = (cb) => {
    let called = false;
    return () => {
      if (!called) {
        cb();
        called = true;
      }
    };
  };

  // dist/modules/lib/getVisibilityWatcher.js
  var firstHiddenTime = -1;
  var onHiddenFunctions = /* @__PURE__ */ new Set();
  var initHiddenTime = () => {
    return document.visibilityState === "hidden" && !document.prerendering ? 0 : Infinity;
  };
  var onVisibilityUpdate = (event) => {
    if (document.visibilityState === "hidden") {
      if (event.type === "visibilitychange") {
        for (const onHiddenFunction of onHiddenFunctions) {
          onHiddenFunction();
        }
      }
      if (!isFinite(firstHiddenTime)) {
        firstHiddenTime = event.type === "visibilitychange" ? event.timeStamp : 0;
        removeEventListener("prerenderingchange", onVisibilityUpdate, true);
      }
    }
  };
  var getVisibilityWatcher = () => {
    if (firstHiddenTime < 0) {
      const activationStart = getActivationStart();
      const firstVisibilityStateHiddenTime = !document.prerendering ? globalThis.performance.getEntriesByType("visibility-state").filter((e) => e.name === "hidden" && e.startTime > activationStart)[0]?.startTime : void 0;
      firstHiddenTime = firstVisibilityStateHiddenTime ?? initHiddenTime();
      addEventListener("visibilitychange", onVisibilityUpdate, true);
      addEventListener("prerenderingchange", onVisibilityUpdate, true);
      onBFCacheRestore(() => {
        setTimeout(() => {
          firstHiddenTime = initHiddenTime();
        });
      });
    }
    return {
      get firstHiddenTime() {
        return firstHiddenTime;
      },
      onHidden(cb) {
        onHiddenFunctions.add(cb);
      }
    };
  };

  // dist/modules/lib/whenActivated.js
  var whenActivated = (callback) => {
    if (document.prerendering) {
      addEventListener("prerenderingchange", () => callback(), true);
    } else {
      callback();
    }
  };

  // dist/modules/onFCP.js
  var FCPThresholds = [1800, 3e3];
  var onFCP = (onReport, opts = {}) => {
    whenActivated(() => {
      const visibilityWatcher = getVisibilityWatcher();
      let metric = initMetric("FCP");
      let report;
      const handleEntries = (entries) => {
        for (const entry of entries) {
          if (entry.name === "first-contentful-paint") {
            po2.disconnect();
            if (entry.startTime < visibilityWatcher.firstHiddenTime) {
              metric.value = Math.max(entry.startTime - getActivationStart(), 0);
              metric.entries.push(entry);
              report(true);
            }
          }
        }
      };
      const po2 = observe("paint", handleEntries);
      if (po2) {
        report = bindReporter(onReport, metric, FCPThresholds, opts.reportAllChanges);
        onBFCacheRestore((event) => {
          metric = initMetric("FCP");
          report = bindReporter(onReport, metric, FCPThresholds, opts.reportAllChanges);
          doubleRAF(() => {
            metric.value = performance.now() - event.timeStamp;
            report(true);
          });
        });
      }
    });
  };

  // dist/modules/onCLS.js
  var CLSThresholds = [0.1, 0.25];
  var onCLS = (onReport, opts = {}) => {
    const visibilityWatcher = getVisibilityWatcher();
    onFCP(runOnce(() => {
      let metric = initMetric("CLS", 0);
      let report;
      const layoutShiftManager = initUnique(opts, LayoutShiftManager);
      const handleEntries = (entries) => {
        for (const entry of entries) {
          layoutShiftManager._processEntry(entry);
        }
        if (layoutShiftManager._sessionValue > metric.value) {
          metric.value = layoutShiftManager._sessionValue;
          metric.entries = layoutShiftManager._sessionEntries;
          report();
        }
      };
      const po2 = observe("layout-shift", handleEntries);
      if (po2) {
        report = bindReporter(onReport, metric, CLSThresholds, opts.reportAllChanges);
        visibilityWatcher.onHidden(() => {
          handleEntries(po2.takeRecords());
          report(true);
        });
        onBFCacheRestore(() => {
          layoutShiftManager._sessionValue = 0;
          metric = initMetric("CLS", 0);
          report = bindReporter(onReport, metric, CLSThresholds, opts.reportAllChanges);
          doubleRAF(() => report());
        });
        setTimeout(report);
      }
    }));
  };

  // dist/modules/lib/polyfills/interactionCountPolyfill.js
  var interactionCountEstimate = 0;
  var minKnownInteractionId = Infinity;
  var maxKnownInteractionId = 0;
  var updateEstimate = (entries) => {
    for (const entry of entries) {
      if (entry.interactionId) {
        minKnownInteractionId = Math.min(minKnownInteractionId, entry.interactionId);
        maxKnownInteractionId = Math.max(maxKnownInteractionId, entry.interactionId);
        interactionCountEstimate = maxKnownInteractionId ? (maxKnownInteractionId - minKnownInteractionId) / 7 + 1 : 0;
      }
    }
  };
  var po;
  var getInteractionCount = () => {
    return po ? interactionCountEstimate : performance.interactionCount ?? 0;
  };
  var initInteractionCountPolyfill = () => {
    if ("interactionCount" in performance || po)
      return;
    po = observe("event", updateEstimate, {
      type: "event",
      buffered: true,
      durationThreshold: 0
    });
  };

  // dist/modules/lib/InteractionManager.js
  var MAX_INTERACTIONS_TO_CONSIDER = 10;
  var prevInteractionCount = 0;
  var getInteractionCountForNavigation = () => {
    return getInteractionCount() - prevInteractionCount;
  };
  var InteractionManager = class {
    /**
     * A list of longest interactions on the page (by latency) sorted so the
     * longest one is first. The list is at most MAX_INTERACTIONS_TO_CONSIDER
     * long.
     */
    _longestInteractionList = [];
    /**
     * A mapping of longest interactions by their interaction ID.
     * This is used for faster lookup.
     */
    _longestInteractionMap = /* @__PURE__ */ new Map();
    _onBeforeProcessingEntry;
    _onAfterProcessingINPCandidate;
    _resetInteractions() {
      prevInteractionCount = getInteractionCount();
      this._longestInteractionList.length = 0;
      this._longestInteractionMap.clear();
    }
    /**
     * Returns the estimated p98 longest interaction based on the stored
     * interaction candidates and the interaction count for the current page.
     */
    _estimateP98LongestInteraction() {
      const candidateInteractionIndex = Math.min(this._longestInteractionList.length - 1, Math.floor(getInteractionCountForNavigation() / 50));
      return this._longestInteractionList[candidateInteractionIndex];
    }
    /**
     * Takes a performance entry and adds it to the list of worst interactions
     * if its duration is long enough to make it among the worst. If the
     * entry is part of an existing interaction, it is merged and the latency
     * and entries list is updated as needed.
     */
    _processEntry(entry) {
      this._onBeforeProcessingEntry?.(entry);
      if (!(entry.interactionId || entry.entryType === "first-input"))
        return;
      const minLongestInteraction = this._longestInteractionList.at(-1);
      let interaction = this._longestInteractionMap.get(entry.interactionId);
      if (interaction || this._longestInteractionList.length < MAX_INTERACTIONS_TO_CONSIDER || // If the above conditions are false, `minLongestInteraction` will be set.
      entry.duration > minLongestInteraction._latency) {
        if (interaction) {
          if (entry.duration > interaction._latency) {
            interaction.entries = [entry];
            interaction._latency = entry.duration;
          } else if (entry.duration === interaction._latency && entry.startTime === interaction.entries[0].startTime) {
            interaction.entries.push(entry);
          }
        } else {
          interaction = {
            id: entry.interactionId,
            entries: [entry],
            _latency: entry.duration
          };
          this._longestInteractionMap.set(interaction.id, interaction);
          this._longestInteractionList.push(interaction);
        }
        this._longestInteractionList.sort((a, b) => b._latency - a._latency);
        if (this._longestInteractionList.length > MAX_INTERACTIONS_TO_CONSIDER) {
          const removedInteractions = this._longestInteractionList.splice(MAX_INTERACTIONS_TO_CONSIDER);
          for (const interaction2 of removedInteractions) {
            this._longestInteractionMap.delete(interaction2.id);
          }
        }
        this._onAfterProcessingINPCandidate?.(interaction);
      }
    }
  };

  // dist/modules/lib/whenIdleOrHidden.js
  var whenIdleOrHidden = (cb) => {
    const rIC = globalThis.requestIdleCallback || setTimeout;
    if (document.visibilityState === "hidden") {
      cb();
    } else {
      cb = runOnce(cb);
      addEventListener("visibilitychange", cb, { once: true, capture: true });
      rIC(() => {
        cb();
        removeEventListener("visibilitychange", cb, { capture: true });
      });
    }
  };

  // dist/modules/onINP.js
  var INPThresholds = [200, 500];
  var DEFAULT_DURATION_THRESHOLD = 40;
  var onINP = (onReport, opts = {}) => {
    if (!(globalThis.PerformanceEventTiming && "interactionId" in PerformanceEventTiming.prototype)) {
      return;
    }
    const visibilityWatcher = getVisibilityWatcher();
    whenActivated(() => {
      initInteractionCountPolyfill();
      let metric = initMetric("INP");
      let report;
      const interactionManager = initUnique(opts, InteractionManager);
      const handleEntries = (entries) => {
        whenIdleOrHidden(() => {
          for (const entry of entries) {
            interactionManager._processEntry(entry);
          }
          const inp = interactionManager._estimateP98LongestInteraction();
          if (inp && inp._latency !== metric.value) {
            metric.value = inp._latency;
            metric.entries = inp.entries;
            report();
          }
        });
      };
      const po2 = observe("event", handleEntries, {
        // Event Timing entries have their durations rounded to the nearest 8ms,
        // so a duration of 40ms would be any event that spans 2.5 or more frames
        // at 60Hz. This threshold is chosen to strike a balance between usefulness
        // and performance. Running this callback for any interaction that spans
        // just one or two frames is likely not worth the insight that could be
        // gained.
        durationThreshold: opts.durationThreshold ?? DEFAULT_DURATION_THRESHOLD
      });
      report = bindReporter(onReport, metric, INPThresholds, opts.reportAllChanges);
      if (po2) {
        po2.observe({ type: "first-input", buffered: true });
        visibilityWatcher.onHidden(() => {
          handleEntries(po2.takeRecords());
          report(true);
        });
        onBFCacheRestore(() => {
          interactionManager._resetInteractions();
          metric = initMetric("INP");
          report = bindReporter(onReport, metric, INPThresholds, opts.reportAllChanges);
        });
      }
    });
  };

  // dist/modules/lib/LCPEntryManager.js
  var LCPEntryManager = class {
    _onBeforeProcessingEntry;
    _processEntry(entry) {
      this._onBeforeProcessingEntry?.(entry);
    }
  };

  // dist/modules/onLCP.js
  var LCPThresholds = [2500, 4e3];
  var onLCP = (onReport, opts = {}) => {
    whenActivated(() => {
      const visibilityWatcher = getVisibilityWatcher();
      let metric = initMetric("LCP");
      let report;
      const lcpEntryManager = initUnique(opts, LCPEntryManager);
      const handleEntries = (entries) => {
        if (!opts.reportAllChanges) {
          entries = entries.slice(-1);
        }
        for (const entry of entries) {
          lcpEntryManager._processEntry(entry);
          if (entry.startTime < visibilityWatcher.firstHiddenTime) {
            metric.value = Math.max(entry.startTime - getActivationStart(), 0);
            metric.entries = [entry];
            report();
          }
        }
      };
      const po2 = observe("largest-contentful-paint", handleEntries);
      if (po2) {
        report = bindReporter(onReport, metric, LCPThresholds, opts.reportAllChanges);
        const stopListening = runOnce(() => {
          handleEntries(po2.takeRecords());
          po2.disconnect();
          report(true);
        });
        const stopListeningWrapper = (event) => {
          if (event.isTrusted) {
            whenIdleOrHidden(stopListening);
            removeEventListener(event.type, stopListeningWrapper, {
              capture: true
            });
          }
        };
        for (const type of ["keydown", "click", "visibilitychange"]) {
          addEventListener(type, stopListeningWrapper, {
            capture: true
          });
        }
        onBFCacheRestore((event) => {
          metric = initMetric("LCP");
          report = bindReporter(onReport, metric, LCPThresholds, opts.reportAllChanges);
          doubleRAF(() => {
            metric.value = performance.now() - event.timeStamp;
            report(true);
          });
        });
      }
    });
  };

  // dist/modules/onTTFB.js
  var TTFBThresholds = [800, 1800];
  var whenReady = (callback) => {
    if (document.prerendering) {
      whenActivated(() => whenReady(callback));
    } else if (document.readyState !== "complete") {
      addEventListener("load", () => whenReady(callback), true);
    } else {
      setTimeout(callback);
    }
  };
  var onTTFB = (onReport, opts = {}) => {
    let metric = initMetric("TTFB");
    let report = bindReporter(onReport, metric, TTFBThresholds, opts.reportAllChanges);
    whenReady(() => {
      const navigationEntry = getNavigationEntry();
      if (navigationEntry) {
        metric.value = Math.max(navigationEntry.responseStart - getActivationStart(), 0);
        metric.entries = [navigationEntry];
        report(true);
        onBFCacheRestore(() => {
          metric = initMetric("TTFB", 0);
          report = bindReporter(onReport, metric, TTFBThresholds, opts.reportAllChanges);
          report(true);
        });
      }
    });
  };


  function storeMetrics(metricName,metric){
    if(metricName == "CLS"){
        window.__vitals[metricName] = metric.value.toFixed(4)
    }
    else {

        let oldValue = window.__vitals[metricName] ? window.__vitals[metricName] : 0
        let newValue = metric.value.toFixed(0)
        window.__vitals[metricName] = oldValue < newValue ? newValue : oldValue;
    }

  }


  function revalidateMeasuring(){
      new PerformanceObserver((entryList) => {
          const entries = entryList.getEntries();
          const lastEntry = entries[entries.length - 1]; // Use the latest LCP candidate
          const entry = Math.max(lastEntry.renderTime, lastEntry.loadTime);
          storeMetrics("LCP",{ "value": entry})
        }).observe({ type: "largest-contentful-paint", buffered: true });

       new PerformanceObserver((entryList) => {
          for (const entry of entryList.getEntriesByName('first-contentful-paint')) {
            storeMetrics("FCP",{"value": entry.startTime})
          }
        }).observe({type: 'paint', buffered: true});
  }

  // dist/modules/collectVitals.js
  window.__vitals = {};
  window.__vitalsFull = {};
  onCLS((v) => storeMetrics("CLS",v),{reportAllChanges: true});
  onLCP((v) => storeMetrics("LCP",v),{reportAllChanges: true});
  onINP((v) => storeMetrics("INP",v),{reportAllChanges: true});
  onFCP((v) => storeMetrics("FCP",v),{reportAllChanges: true});
  onTTFB((v) => storeMetrics("TTFB",v),{reportAllChanges: true});

  revalidateMeasuring()

  window.getPerformanceMetrics = function() {
    console.log("[GatlingPlaywrightPlugin] Metrics collected")
    console.log(window.__vitals)



    return window.__vitals;
  };

})();
