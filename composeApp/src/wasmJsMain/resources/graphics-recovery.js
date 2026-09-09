(function () {
    'use strict';

    let installed = null;

    function install() {
        if (installed) return installed.dispose;

        const canvases = new Map();
        let observedRoots = new Set();
        let observer = null;
        let running = false;
        let disposed = false;
        let overlay = null;
        let overlayCleanup = null;
        let description = null;
        let interruption = null;

        function showInterruption(message) {
            interruption = message;
            if (!running || !document.body) return;
            if (overlay) {
                description.textContent = message;
                return;
            }

            // This is ordinary DOM so a failed Compose/WebGL surface cannot hide it.
            overlay = document.createElement('div');
            overlay.setAttribute('role', 'alertdialog');
            overlay.setAttribute('aria-modal', 'true');
            overlay.setAttribute('aria-labelledby', 'doom-graphics-recovery-title');
            overlay.setAttribute('aria-describedby', 'doom-graphics-recovery-description');
            overlay.style.cssText = 'position:fixed;inset:0;z-index:2147483647;display:flex;align-items:center;justify-content:center;padding:24px;box-sizing:border-box;background:rgba(13,11,10,.96);color:#e8ddd2;font-family:system-ui,sans-serif;';

            const panel = document.createElement('div');
            panel.style.cssText = 'width:100%;max-width:480px;padding:28px;box-sizing:border-box;border:1px solid #6a5034;border-radius:8px;background:#211a13;';
            const title = document.createElement('h2');
            title.id = 'doom-graphics-recovery-title';
            title.textContent = 'Graphics interrupted';
            title.style.cssText = 'margin:0 0 16px;font-size:24px;line-height:1.25;';
            description = document.createElement('p');
            description.id = 'doom-graphics-recovery-description';
            description.textContent = message;
            description.style.cssText = 'margin:0 0 24px;font-size:16px;line-height:1.5;color:#cfc0b4;';
            const reload = document.createElement('button');
            reload.type = 'button';
            reload.textContent = 'RELOAD GAME';
            reload.style.cssText = 'width:100%;min-height:48px;padding:12px 20px;border:0;border-radius:4px;background:#d6a15a;color:#1b1108;font:700 16px system-ui,sans-serif;cursor:pointer;outline-offset:4px;';
            const reloadGame = () => window.location.reload();
            const keepFocus = event => {
                // Keep the single recovery action reachable without sending keys to the game.
                event.stopPropagation();
                if (event.key === 'Tab') {
                    event.preventDefault();
                    reload.focus();
                }
            };
            reload.addEventListener('click', reloadGame);
            overlay.addEventListener('keydown', keepFocus);
            const dialog = overlay;
            overlayCleanup = () => {
                reload.removeEventListener('click', reloadGame);
                dialog.removeEventListener('keydown', keepFocus);
            };
            panel.appendChild(title);
            panel.appendChild(description);
            panel.appendChild(reload);
            overlay.appendChild(panel);
            document.body.appendChild(overlay);
            reload.focus();
        }

        function removeOverlay() {
            if (overlayCleanup) overlayCleanup();
            overlayCleanup = null;
            if (overlay) overlay.remove();
            overlay = null;
            description = null;
        }

        function discover(root, roots, found) {
            roots.add(root);
            for (const element of root.querySelectorAll('*')) {
                if (element.tagName === 'CANVAS') found.add(element);
                if (element.shadowRoot) discover(element.shadowRoot, roots, found);
            }
        }

        function detach(canvas, listeners) {
            canvas.removeEventListener('webglcontextlost', listeners.lost);
            canvas.removeEventListener('webglcontextrestored', listeners.restored);
            canvases.delete(canvas);
        }

        function reconcile() {
            if (!running) return;
            const roots = new Set();
            const found = new Set();
            discover(document, roots, found);
            for (const [canvas, listeners] of canvases) {
                if (!found.has(canvas)) detach(canvas, listeners);
            }
            for (const canvas of found) {
                if (canvases.has(canvas)) continue;
                const lost = event => {
                    if (!running || !canvases.has(canvas)) return;
                    event.preventDefault();
                    showInterruption('The browser stopped drawing the game. Reload the page to play again. Unsaved progress will be lost; your remembered game file and saved games are kept.');
                };
                const restored = () => {
                    if (!running || !interruption || !canvases.has(canvas)) return;
                    // Skiko cannot rebuild its lost graphics resources through a public API.
                    showInterruption('The graphics connection returned, but the game needs to reload before it can draw again. Unsaved progress will be lost; your remembered game file and saved games are kept.');
                };
                canvas.addEventListener('webglcontextlost', lost);
                canvas.addEventListener('webglcontextrestored', restored);
                canvases.set(canvas, { lost, restored });
            }
            if (roots.size !== observedRoots.size || [...roots].some(root => !observedRoots.has(root))) {
                observer.disconnect();
                for (const root of roots) observer.observe(root, { childList: true, subtree: true });
                observedRoots = roots;
            }
        }

        function resume() {
            if (disposed || running) return;
            running = true;
            observer = new MutationObserver(reconcile);
            reconcile();
            if (interruption) showInterruption(interruption);
        }

        function suspend() {
            running = false;
            if (observer) observer.disconnect();
            observer = null;
            observedRoots.clear();
            for (const [canvas, listeners] of canvases) detach(canvas, listeners);
            removeOverlay();
        }

        function onPageShow(event) {
            if (event.persisted) resume();
        }

        function dispose() {
            if (disposed) return;
            disposed = true;
            suspend();
            window.removeEventListener('pagehide', suspend);
            window.removeEventListener('pageshow', onPageShow);
            installed = null;
        }

        installed = { dispose };
        window.addEventListener('pagehide', suspend);
        window.addEventListener('pageshow', onPageShow);
        resume();
        return dispose;
    }

    globalThis.DoomGraphicsRecovery = { install };
}());
