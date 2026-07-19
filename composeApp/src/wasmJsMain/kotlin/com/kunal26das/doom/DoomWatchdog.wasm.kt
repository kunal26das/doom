package com.kunal26das.doom

/**
 * Recovers presentation when Compose-for-web stops drawing.
 *
 * Skiko's web renderer is a demand-driven, single-shot requestAnimationFrame
 * chain with no WebGL context-loss handling: one failed render permanently
 * halts the chain (upstream CMP-8615, unfixed as of CMP 1.12), leaving the
 * canvas black while the engine and audio keep running. The only path in the
 * stack that rebuilds the WebGL context + Skia surface and re-primes the
 * chain is ComposeWindow's window-resize handler — which is why manually
 * resizing the window "fixes" the blank screen.
 *
 * The watchdog dispatches a synthetic resize when either happens:
 *  - the tab is visible but no frame has been presented for 3s (the engine
 *    presents ~35fps continuously, so this cannot occur in healthy play);
 *  - the browser restores a lost WebGL context (preventDefault on
 *    webglcontextlost is required for restoration to be possible at all).
 *
 * Kicks fire only when rendering is already stopped, so they cannot race an
 * in-flight frame (the race is what triggers CMP-8615 in the first place).
 */
internal fun installRenderWatchdog(): Unit = js(
    """{
    if (window.__doomWatchdog) return;
    window.__doomWatchdog = true;
    window.__doomLastFrame = performance.now();
    var kick = function (reason) {
        console.warn('[doom] watchdog: ' + reason + '; forcing surface recreation');
        window.__doomLastFrame = performance.now();
        window.dispatchEvent(new Event('resize'));
    };
    var hookCanvas = function () {
        var canvas = null;
        var all = document.getElementsByTagName('*');
        for (var i = 0; i < all.length && !canvas; i++) {
            if (all[i].shadowRoot) canvas = all[i].shadowRoot.querySelector('canvas');
        }
        if (!canvas) { setTimeout(hookCanvas, 500); return; }
        canvas.addEventListener('webglcontextlost', function (e) {
            e.preventDefault();
            console.warn('[doom] webgl context lost');
        });
        canvas.addEventListener('webglcontextrestored', function () {
            kick('webgl context restored');
        });
    };
    hookCanvas();
    setInterval(function () {
        if (!window.__doomBooted || document.visibilityState !== 'visible') {
            window.__doomLastFrame = performance.now();
            return;
        }
        var idle = performance.now() - window.__doomLastFrame;
        if (idle > 3000) kick('no frame presented for ' + Math.round(idle) + 'ms');
    }, 1500);
}"""
)

internal fun noteFramePresented(): Unit = js(
    """{
    window.__doomLastFrame = performance.now();
    window.__doomBooted = true;
}"""
)
