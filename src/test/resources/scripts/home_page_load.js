(function () {
    const el = document.querySelector("img.header-hp_image-lines");
    return document.readyState === "complete" &&
        el !== null &&
        el.getBoundingClientRect().width > 0 &&
        el.getBoundingClientRect().height > 0;
})();