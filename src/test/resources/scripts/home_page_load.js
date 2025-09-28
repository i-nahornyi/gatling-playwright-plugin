(function () {
    const el = document.querySelector('[data-test-id="chat-widget-iframe"]');
    return document.readyState === "complete" &&
        el !== null &&
        el.getBoundingClientRect().width > 0 &&
        el.getBoundingClientRect().height > 0;
})();