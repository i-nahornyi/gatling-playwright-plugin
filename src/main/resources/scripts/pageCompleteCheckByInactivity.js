/// * This script inspired by code from browsertime library (https://github.com/sitespeedio/browsertime)
/// https://github.com/sitespeedio/browsertime/blob/main/lib/core/pageCompleteChecks/pageCompleteCheckByInactivity.js

waitTime => (function(waitTime) {
  const p = window.performance;
  const timing = p.timing;
  const now = p.now();


  if (!window.__lastCheckTime) {
    window.__lastCheckTime = 0;
  }

  if (timing.loadEventEnd === 0) {
    return false;
  }

  const entries = p.getEntriesByType("resource");
  const lastCheck = window.__lastCheckTime;

  // Iterate backwards
  for (let i = entries.length - 1; i >= 0; i--) {
    const end = entries[i].responseEnd;

    if (end > lastCheck) {
      window.__lastCheckTime = now;
      return false;
    }
  }

  const result = (now - lastCheck) > waitTime;

  return result;

})(waitTime);