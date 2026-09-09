import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';
import { GraphicsEventSurface } from './fixtures/GraphicsEventSurface.mjs';

const source = readFileSync(new URL('../../composeApp/src/wasmJsMain/resources/browser-lifecycle.js', import.meta.url), 'utf8');

function harness({ visible = true, focused = true } = {}) {
    const document = new GraphicsEventSurface();
    const window = new GraphicsEventSurface();
    const states = [];
    document.visibilityState = visible ? 'visible' : 'hidden';
    document.hasFocus = () => focused;
    const context = vm.createContext({ document, window });
    vm.runInContext(source, context, { filename: 'browser-lifecycle.js' });
    const observer = context.DoomPageLifecycle.observe(state => states.push(state));
    return {
        document, window, states, observer,
        focus(value) {
            focused = value;
            window.dispatch(value ? 'focus' : 'blur');
        },
        visibility(value) {
            document.visibilityState = value ? 'visible' : 'hidden';
            document.dispatch('visibilitychange');
        },
    };
}

test('initial activity requires both a visible page and a focused document', () => {
    for (const [visible, focused, expected] of [
        [true, true, 'resumed'], [true, false, 'started'],
        [false, true, 'created'], [false, false, 'created'],
    ]) {
        const h = harness({ visible, focused });
        assert.deepEqual(h.states, [expected]);
        h.observer.dispose();
    }
});

test('focus before visibility resumes the game after the tab becomes visible', () => {
    const h = harness({ visible: false, focused: false });
    h.focus(true);
    assert.deepEqual(h.states, ['created'], 'a focused but hidden tab must remain paused');
    h.visibility(true);
    assert.deepEqual(h.states, ['created', 'resumed']);
});

test('visibility before focus remains paused until the window is focused', () => {
    const h = harness({ visible: false, focused: false });
    h.visibility(true);
    assert.deepEqual(h.states, ['created', 'started']);
    h.focus(true);
    assert.deepEqual(h.states, ['created', 'started', 'resumed']);
});

test('a late visible notification does not demote a focused running game', () => {
    const h = harness({ focused: false });
    h.focus(true);
    h.visibility(true);
    h.visibility(true);
    assert.deepEqual(h.states, ['started', 'resumed']);
});

test('blur pauses and refocus resumes without recreating the observer', () => {
    const h = harness();
    h.focus(false);
    h.visibility(true);
    assert.deepEqual(h.states, ['resumed', 'started'], 'visibility alone cannot resume a blurred game');
    h.focus(true);
    assert.deepEqual(h.states, ['resumed', 'started', 'resumed']);
});

test('hiding and showing a focused page pauses and resumes even without focus events', () => {
    const h = harness();
    h.visibility(false);
    h.visibility(true);
    assert.deepEqual(h.states, ['resumed', 'created', 'resumed']);
});

test('pagehide pauses before visibility updates and a back-forward cache return resumes', () => {
    const h = harness();
    h.window.dispatch('pagehide', { persisted: true });
    h.focus(true);
    h.visibility(true);
    assert.deepEqual(h.states, ['resumed', 'created']);
    h.window.dispatch('pageshow', { persisted: true });
    assert.deepEqual(h.states, ['resumed', 'created', 'resumed']);
});

test('returning from the back-forward cache does not resume a blurred page', () => {
    const h = harness();
    h.window.dispatch('pagehide', { persisted: true });
    h.focus(false);
    h.window.dispatch('pageshow', { persisted: true });
    assert.deepEqual(h.states, ['resumed', 'created', 'started']);
});

test('disposal removes every listener and later events cannot update the old owner', () => {
    const h = harness();
    h.observer.dispose();
    h.observer.dispose();
    assert.equal(h.document.listenerCount(), 0);
    assert.equal(h.window.listenerCount(), 0);
    h.focus(false);
    h.visibility(false);
    h.window.dispatch('pagehide');
    h.window.dispatch('pageshow', { persisted: true });
    assert.deepEqual(h.states, ['resumed']);
});

test('disposing an old subscription leaves its replacement active', () => {
    const h = harness();
    const replacementStates = [];
    const context = vm.createContext({ document: h.document, window: h.window });
    vm.runInContext(source, context);
    const replacement = context.DoomPageLifecycle.observe(state => replacementStates.push(state));
    h.observer.dispose();
    h.focus(false);
    assert.deepEqual(h.states, ['resumed']);
    assert.deepEqual(replacementStates, ['resumed', 'started']);
    replacement.dispose();
    assert.equal(h.document.listenerCount(), 0);
    assert.equal(h.window.listenerCount(), 0);
});

test('touch and pen activate a visible page even when mobile Safari reports no focus', () => {
    for (const pointerType of ['touch', 'pen']) {
        const h = harness({ focused: false });
        h.document.dispatch('pointerdown', { pointerType });
        h.visibility(true);
        assert.deepEqual(h.states, ['started', 'resumed'], 'later visibility events must preserve touch activation');
    }
});

test('mouse or unclassified pointers cannot resume a genuinely unfocused page', () => {
    const h = harness({ focused: false });
    for (const pointerType of ['mouse', '', undefined]) {
        h.document.dispatch('pointerdown', { pointerType });
    }
    assert.deepEqual(h.states, ['started']);
});

test('hidden-page and pagehide pointer events cannot activate the game', () => {
    const hidden = harness({ visible: false, focused: false });
    hidden.document.dispatch('pointerdown', { pointerType: 'touch' });
    hidden.visibility(true);
    assert.deepEqual(hidden.states, ['created', 'started']);

    const cached = harness({ focused: false });
    cached.window.dispatch('pagehide', { persisted: true });
    cached.document.dispatch('pointerdown', { pointerType: 'pen' });
    cached.window.dispatch('pageshow', { persisted: true });
    assert.deepEqual(cached.states, ['started', 'created', 'started']);
});

test('genuine blur clears touch activation until a new visible touch or focus', () => {
    const h = harness({ focused: false });
    h.document.dispatch('pointerdown', { pointerType: 'touch' });
    h.focus(false);
    h.visibility(true);
    assert.deepEqual(h.states, ['started', 'resumed', 'started']);
    h.document.dispatch('pointerdown', { pointerType: 'pen' });
    assert.deepEqual(h.states, ['started', 'resumed', 'started', 'resumed']);
});

test('hiding or caching the page clears its previous touch activation', () => {
    for (const cache of [false, true]) {
        const h = harness({ focused: false });
        h.document.dispatch('pointerdown', { pointerType: 'touch' });
        if (cache) {
            h.window.dispatch('pagehide', { persisted: true });
            h.window.dispatch('pageshow', { persisted: true });
        } else {
            h.visibility(false);
            h.visibility(true);
        }
        assert.deepEqual(h.states, ['started', 'resumed', 'created', 'started']);
    }
});

test('disposed touch activation is not retained by a replacement subscription', () => {
    const h = harness({ focused: false });
    h.document.dispatch('pointerdown', { pointerType: 'touch' });
    h.observer.dispose();
    h.document.dispatch('pointerdown', { pointerType: 'pen' });
    const states = [];
    const context = vm.createContext({ document: h.document, window: h.window });
    vm.runInContext(source, context);
    const replacement = context.DoomPageLifecycle.observe(state => states.push(state));
    assert.deepEqual(h.states, ['started', 'resumed']);
    assert.deepEqual(states, ['started']);
    replacement.dispose();
    assert.equal(h.document.listenerCount(), 0);
    assert.equal(h.window.listenerCount(), 0);
});
