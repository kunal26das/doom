(function () {
    'use strict';

    function observe(onState) {
        let disposed = false;
        let pageHidden = false;
        let pointerActivated = false;
        let previous = null;

        function update() {
            if (disposed) return;
            const visible = !pageHidden && document.visibilityState === 'visible';
            if (!visible) pointerActivated = false;
            const active = document.hasFocus() || pointerActivated;
            const next = visible ? (active ? 'resumed' : 'started') : 'created';
            if (next === previous) return;
            previous = next;
            onState(next);
        }

        function hide() {
            pageHidden = true;
            pointerActivated = false;
            update();
        }

        function show() {
            pageHidden = false;
            update();
        }

        function blur() {
            pointerActivated = false;
            update();
        }

        function activatePointer(event) {
            if (disposed || pageHidden || document.visibilityState !== 'visible') return;
            if (event.pointerType !== 'touch' && event.pointerType !== 'pen') return;

            pointerActivated = true;
            update();
        }



        document.addEventListener('visibilitychange', update);
        document.addEventListener('pointerdown', activatePointer, true);
        window.addEventListener('focus', update);
        window.addEventListener('blur', blur);
        window.addEventListener('pagehide', hide);
        window.addEventListener('pageshow', show);
        update();

        return {
            dispose() {
                if (disposed) return;
                disposed = true;
                pointerActivated = false;
                document.removeEventListener('visibilitychange', update);
                document.removeEventListener('pointerdown', activatePointer, true);
                window.removeEventListener('focus', update);
                window.removeEventListener('blur', blur);
                window.removeEventListener('pagehide', hide);
                window.removeEventListener('pageshow', show);
            },
        };
    }

    globalThis.DoomPageLifecycle = { observe };
}());
