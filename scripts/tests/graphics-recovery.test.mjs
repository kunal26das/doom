import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';
import { GraphicsEventSurface } from './fixtures/GraphicsEventSurface.mjs';
import { GraphicsElement } from './fixtures/GraphicsElement.mjs';
import { GraphicsMutationObserver } from './fixtures/GraphicsMutationObserver.mjs';

const source = readFileSync(new URL('../../composeApp/src/wasmJsMain/resources/graphics-recovery.js', import.meta.url), 'utf8');




function harness() {
    const document = new GraphicsEventSurface();
    const window = new GraphicsEventSurface();
    const elements = [];
    const observers = [];
    const timers = new Map();
    let timerId = 0;
    let reloads = 0;

    const Element = GraphicsElement.bind(null, document, elements);
    const MutationObserver = GraphicsMutationObserver.bind(null, observers);
    document.documentElement = new Element('html');
    document.body = new Element('body');
    document.documentElement.appendChild(document.body);
    document.activeElement = document.body;
    document.readyState = 'complete';
    document.createElement = (tag) => new Element(tag);
    document.querySelectorAll = (selector) => document.documentElement.querySelectorAll(selector);
    document.querySelector = (selector) => document.querySelectorAll(selector)[0] ?? null;
    document.getElementById = (id) => document.querySelector(`#${id}`);

    const setTimer = (callback) => { timers.set(++timerId, callback); return timerId; };
    const clearTimer = (id) => timers.delete(id);
    window.location = { reload() { reloads++; } };
    Object.assign(window, {
        setTimeout: setTimer, clearTimeout: clearTimer,
        setInterval: setTimer, clearInterval: clearTimer,
    });
    const context = vm.createContext({
        document, window, MutationObserver, HTMLElement: Element, HTMLCanvasElement: Element,
        setTimeout: setTimer, clearTimeout: clearTimer, setInterval: setTimer, clearInterval: clearTimer,
        location: window.location, console: { warn() {}, error() {}, log() {} },
    });
    vm.runInContext(source, context, { filename: 'graphics-recovery.js' });

    return {
        document, window, elements, observers, timers,
        install: () => context.DoomGraphicsRecovery.install(),
        get reloads() { return reloads; },
        dialogs: () => document.querySelectorAll('[role="alertdialog"]'),
        mutate() {
            for (const observer of [...observers]) {
                if (!observer.disconnected) observer.callback([{ type: 'childList', target: document.body }]);
            }
        },
        canvas({ shadow = false } = {}) {
            const host = document.createElement('div');
            const root = shadow ? host.attachShadow({ mode: 'open' }) : host;
            const canvas = document.createElement('canvas');
            root.appendChild(canvas);
            document.body.appendChild(host);
            return { host, root, canvas };
        },
    };
}

function assertNoAutomaticRecovery(h) {
    assert.equal(h.reloads, 0, 'a graphics failure must not erase the current session by reloading it');
    assert.equal(h.window.events.includes('resize'), false, 'recovery must not recreate contexts through synthetic resize');
}

test('context loss provides a focused recovery action and warns about unsaved progress', () => {
    const h = harness();
    const { canvas } = h.canvas({ shadow: true });
    h.install();
    const event = canvas.dispatch('webglcontextlost');
    assert.equal(event.defaultPrevented, true, 'the browser must be allowed to restore the WebGL context');
    const [dialog] = h.dialogs();
    assert.ok(dialog?.isConnected, 'the failure is explained outside the broken Compose canvas');
    assert.equal(dialog.getAttribute('aria-modal'), 'true');
    assert.ok(h.document.getElementById(dialog.getAttribute('aria-labelledby')));
    assert.ok(h.document.getElementById(dialog.getAttribute('aria-describedby')));
    assert.match(dialog.textContent, /unsaved/i);
    const button = dialog.querySelector('button');
    assert.ok(button, 'recovery must be available using a native keyboard-accessible button');
    assert.match(button.textContent, /reload/i);
    assert.equal(h.document.activeElement, button);
    assertNoAutomaticRecovery(h);
    button.click();
    assert.equal(h.reloads, 1, 'only the user action reloads the game');
});

test('keyboard navigation keeps the recovery button reachable and does not reach the game', () => {
    const h = harness();
    const { canvas } = h.canvas();
    h.install();
    canvas.dispatch('webglcontextlost');
    const [dialog] = h.dialogs();
    const button = dialog.querySelector('button');
    for (const shiftKey of [false, true]) {
        h.document.activeElement = h.document.body;
        const event = dialog.dispatch('keydown', { key: 'Tab', shiftKey });
        assert.equal(event.defaultPrevented, true);
        assert.equal(event.propagationStopped, true);
        assert.equal(h.document.activeElement, button);
    }
    const enter = dialog.dispatch('keydown', { key: 'Enter', target: button });
    assert.equal(enter.defaultPrevented, false, 'native button activation must remain available');
    assert.equal(enter.propagationStopped, true);
    assertNoAutomaticRecovery(h);
});

test('repeated installation and context loss do not duplicate listeners or dialogs', () => {
    const h = harness();
    const { canvas } = h.canvas();
    const dispose = h.install();
    const listeners = canvas.listenerCount();
    const observers = h.observers.length;
    assert.equal(h.install(), dispose);
    h.mutate();
    h.mutate();
    assert.equal(canvas.listenerCount(), listeners);
    assert.equal(h.observers.length, observers);
    canvas.dispatch('webglcontextlost');
    canvas.dispatch('webglcontextlost');
    assert.equal(h.dialogs().length, 1);
    assertNoAutomaticRecovery(h);
});

test('canvases mounted later inside open shadow roots receive recovery handling', () => {
    const h = harness();
    h.install();
    const { root, canvas } = h.canvas({ shadow: true });
    h.mutate();
    assert.ok(h.observers.some((observer) => observer.targets.includes(root)));
    assert.equal(canvas.dispatch('webglcontextlost').defaultPrevented, true);
    assert.equal(h.dialogs().length, 1);
    assertNoAutomaticRecovery(h);
});

test('replacing a canvas releases its old listeners and handles the replacement', () => {
    const h = harness();
    const previous = h.canvas({ shadow: true });
    h.install();
    previous.host.remove();
    const next = h.canvas({ shadow: true });
    h.mutate();
    assert.equal(previous.canvas.listenerCount(), 0);
    assert.equal(previous.canvas.dispatch('webglcontextlost').defaultPrevented, false);
    assert.equal(next.canvas.dispatch('webglcontextlost').defaultPrevented, true);
    assert.equal(h.dialogs().length, 1);
});

test('context restoration never resizes or reloads the page automatically', () => {
    const h = harness();
    const { canvas } = h.canvas({ shadow: true });
    h.install();
    canvas.dispatch('webglcontextlost');
    canvas.dispatch('webglcontextrestored');
    assertNoAutomaticRecovery(h);
    assert.equal(h.dialogs().length, 1, 'restoration alone cannot prove the old renderer is usable');
    assert.match(h.dialogs()[0].textContent, /connection returned/i);
    assert.match(h.dialogs()[0].textContent, /unsaved/i);
});

test('a restore event without a previous interruption leaves the game alone', () => {
    const h = harness();
    const { canvas } = h.canvas();
    h.install();
    canvas.dispatch('webglcontextrestored');
    assert.equal(h.dialogs().length, 0);
    assertNoAutomaticRecovery(h);
});

test('disposing after a graphics failure removes listeners, observers, timers, and the dialog', () => {
    const h = harness();
    const { canvas } = h.canvas({ shadow: true });
    const dispose = h.install();
    canvas.dispatch('webglcontextlost');
    const staleButton = h.dialogs()[0].querySelector('button');
    dispose();
    dispose();
    assert.equal(h.dialogs().length, 0);
    assert.equal(h.window.listenerCount(), 0);
    assert.equal(h.document.listenerCount(), 0);
    assert.equal(h.elements.reduce((total, element) => total + element.listenerCount(), 0), 0);
    assert.ok(h.observers.every((observer) => observer.disconnected));
    assert.equal(h.timers.size, 0);
    h.mutate();
    assert.equal(canvas.dispatch('webglcontextlost').defaultPrevented, false);
    staleButton.click();
    assertNoAutomaticRecovery(h);
});

test('pagehide removes recovery UI and releases its installed resources', () => {
    const h = harness();
    const { canvas } = h.canvas();
    h.install();
    canvas.dispatch('webglcontextlost');
    h.window.dispatch('pagehide');
    assert.equal(h.dialogs().length, 0);
    assert.equal(canvas.listenerCount(), 0);
    assert.ok(h.observers.every((observer) => observer.disconnected));
    assert.equal(h.timers.size, 0);
    assertNoAutomaticRecovery(h);
});

test('explicit installation after disposal handles a new page lifetime', () => {
    const h = harness();
    const { canvas } = h.canvas();
    h.install()();
    h.install();
    assert.equal(canvas.dispatch('webglcontextlost').defaultPrevented, true);
    assert.equal(h.dialogs().length, 1);
    assertNoAutomaticRecovery(h);
});

for (const interrupted of [false, true]) {
    test(`back-forward cache restores listeners${interrupted ? ' and the interruption notice' : ' without disturbing a healthy game'}`, () => {
        const h = harness();
        const { canvas } = h.canvas({ shadow: true });
        h.install();
        const listeners = canvas.listenerCount();
        if (interrupted) canvas.dispatch('webglcontextlost');
        h.window.dispatch('pagehide', { persisted: true });
        assert.equal(canvas.listenerCount(), 0);
        assert.equal(h.dialogs().length, 0);
        h.window.dispatch('pageshow', { persisted: true });
        h.window.dispatch('pageshow', { persisted: true });
        assert.equal(canvas.listenerCount(), listeners);
        assert.equal(h.dialogs().length, interrupted ? 1 : 0);
        if (interrupted) assert.match(h.dialogs()[0].textContent, /unsaved/i);
        assertNoAutomaticRecovery(h);
    });
}

test('explicit disposal while suspended prevents later page events reactivating recovery', () => {
    const h = harness();
    const { canvas } = h.canvas();
    const dispose = h.install();
    h.window.dispatch('pagehide', { persisted: true });
    dispose();
    h.window.dispatch('pageshow', { persisted: true });
    assert.equal(canvas.listenerCount(), 0);
    assert.equal(h.window.listenerCount(), 0);
    assert.equal(canvas.dispatch('webglcontextlost').defaultPrevented, false);
    assert.equal(h.dialogs().length, 0);
    assertNoAutomaticRecovery(h);
});
