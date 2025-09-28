/// * This script uses code from browsertime library (https://github.com/sitespeedio/browsertime)
/// https://github.com/sitespeedio/browsertime/blob/main/lib/core/pageCompleteChecks/pageCompleteCheckByInactivity.js

waitTime => (function(waitTime) {

  console.log("[GatlingPlaywrightPlugin] Start page check by inactivity")
  const timing = window.performance.timing;
  const p = window.performance;
  const limit = waitTime;

  if (timing.loadEventEnd === 0) {
    return false;
  }

  let lastEntry = null;
  const resourceTimings = p.getEntriesByType('resource');
  if (resourceTimings.length > 0) {
    lastEntry = resourceTimings.pop();
    // This breaks getting resource timings so ...
    p.clearResourceTimings();
  }

  const loadTime = timing.loadEventEnd - timing.navigationStart;

  var result = false

  if (!lastEntry || lastEntry.responseEnd < loadTime) {
    result = p.now() - loadTime > limit;
  } else {
    result = p.now() - lastEntry.responseEnd > limit;
  }

  console.log("[GatlingPlaywrightPlugin] Finish page check by inactivity result: "+ result)
  return result
})(waitTime);