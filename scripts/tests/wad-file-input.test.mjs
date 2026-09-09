import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';
import vm from 'node:vm';
import { WadEventSurface } from './fixtures/WadEventSurface.mjs';
import { WadFileReader } from './fixtures/WadFileReader.mjs';

const source = readFileSync(new URL('../../composeApp/src/wasmJsMain/resources/wad-file-input.js', import.meta.url), 'utf8');
const limit = 64 * 1024 * 1024;

// These fakes control browser events and asynchronous reads. The implementation
// under test is the same unmodified resource loaded by the web application.

function harness() {
    const document = new WadEventSurface();
    const window = new WadEventSurface();
    const inputs = [];
    const readers = [];
    document.body = {
        appendChild(input) { inputs.push(input); input.isConnected = true; },
        removeChild(input) { input.remove(); },
    };
    document.createElement = (tag) => {
        assert.equal(tag, 'input');
        const input = new WadEventSurface();
        Object.assign(input, {
            style: {},
            files: [],
            value: '',
            clicks: 0,
            click() { this.clicks++; },
            remove() { this.isConnected = false; },
            setAttribute(name, value) { this[name] = value; },
        });
        return input;
    };

    const FileReader = WadFileReader.bind(null, readers);
    FileReader.EMPTY = 0;
    FileReader.LOADING = 1;
    FileReader.DONE = 2;

    const context = vm.createContext({ document, window, FileReader, Int8Array });
    vm.runInContext(source, context, { filename: 'wad-file-input.js' });
    const selected = [];
    const errors = [];
    const dragging = [];
    const onSelected = (name, bytes) => selected.push({ name, bytes: Array.from(bytes) });
    const onError = (message) => errors.push(message);
    return {
        document, window, inputs, readers, selected, errors, dragging,
        picker: () => context.DoomWadFiles.createPicker(limit, onSelected, onError),
        drop: () => context.DoomWadFiles.createDropTarget(limit, onSelected, onError, (value) => dragging.push(value)),
    };
}

function file(name = 'DOOM.WAD', bytes = [73, 87, 65, 68, 0, 127, 128, 255], options = {}) {
    return { name, size: bytes.length, bytes, ...options };
}

function transfer(files, options = {}) {
    return {
        types: ['Files'],
        files,
        items: files.map(() => ({ kind: 'file', webkitGetAsEntry: () => ({ isDirectory: false }) })),
        dropEffect: 'none',
        ...options,
    };
}

function drop(h, files, options = {}) {
    return h.document.dispatch('drop', { dataTransfer: transfer(files, options) });
}

test('a disabled drop target prevents navigation without reading or activating hover', () => {
    const h = harness();
    const target = h.drop();
    assert.equal(drop(h, [file()]).defaultPrevented, true);
    assert.equal(h.readers.length, 0);
    target.setEnabled(true);
    assert.ok(h.document.listenerCount() > 0);
    target.setEnabled(false);
    assert.equal(h.window.listenerCount(), 0);
    assert.equal(drop(h, [file()]).defaultPrevented, true);
    h.document.dispatch('dragenter', { dataTransfer: transfer([file()]) });
    assert.equal(h.dragging.includes(true), false);
    assert.equal(h.readers.length, 0);
    target.dispose();
    assert.equal(h.document.listenerCount(), 0);
    assert.equal(drop(h, [file()]).defaultPrevented, false);
});

test('dropping one WAD prevents navigation and delivers its exact name and signed bytes', () => {
    const h = harness();
    h.drop().setEnabled(true);
    h.document.dispatch('dragenter', { dataTransfer: transfer([file()]) });
    assert.equal(drop(h, [file('Ultimate DoOm.WaD')]).defaultPrevented, true);
    assert.equal(h.selected.length, 0, 'selection waits until the file has been read');
    h.readers[0].succeed();
    assert.deepEqual(h.selected, [{ name: 'Ultimate DoOm.WaD', bytes: [73, 87, 65, 68, 0, 127, -128, -1] }]);
    assert.equal(h.dragging.at(-1), false);
    assert.deepEqual(h.errors, []);
});

test('hover stays active while crossing nested elements and clears after leaving the page', () => {
    const h = harness();
    h.drop().setEnabled(true);
    const dataTransfer = transfer([file()]);
    h.document.dispatch('dragenter', { dataTransfer });
    h.document.dispatch('dragenter', { dataTransfer });
    h.document.dispatch('dragleave', { dataTransfer });
    assert.equal(h.dragging.at(-1), true);
    h.document.dispatch('dragleave', { dataTransfer });
    assert.equal(h.dragging.at(-1), false);
    h.document.dispatch('dragenter', { dataTransfer });
    h.window.dispatch('dragend');
    assert.equal(h.dragging.at(-1), false);
});

test('dragover accepts a file without reading it', () => {
    const h = harness();
    h.drop().setEnabled(true);
    const dataTransfer = transfer([file()]);
    const event = h.document.dispatch('dragover', { dataTransfer });
    assert.equal(event.defaultPrevented, true);
    assert.equal(dataTransfer.dropEffect, 'copy');
    assert.equal(h.readers.length, 0);
});

test('text and URL dragging retain their normal browser behavior', () => {
    const h = harness();
    h.drop().setEnabled(true);
    for (const type of ['dragenter', 'dragover', 'dragleave', 'drop']) {
        const event = h.document.dispatch(type, {
            dataTransfer: transfer([], { types: ['text/plain', 'text/uri-list'], items: [{ kind: 'string' }] }),
        });
        assert.equal(event.defaultPrevented, false);
    }
    assert.equal(h.readers.length, 0);
    assert.deepEqual(h.errors, []);
    assert.equal(h.dragging.includes(true), false);
});

test('file items work even if the browser omits the Files type', () => {
    const h = harness();
    h.drop().setEnabled(true);
    assert.equal(drop(h, [file()], { types: [] }).defaultPrevented, true);
    h.readers[0].succeed();
    assert.equal(h.selected.length, 1);
});

for (const [name, files, options] of [
    ['multiple files', [file(), file('DOOM2.WAD')], {}],
    ['a different file type', [file('DOOM.zip')], {}],
    ['an empty file', [file('DOOM.WAD', [])], {}],
    ['a file above 64 MB', [file('DOOM.WAD', [1], { size: limit + 1 })], {}],
    ['a directory', [file()], { items: [{ kind: 'file', webkitGetAsEntry: () => ({ isDirectory: true }) }] }],
]) {
    test(`dropping ${name} reports an error before reading`, () => {
        const h = harness();
        h.drop().setEnabled(true);
        assert.equal(drop(h, files, options).defaultPrevented, true);
        assert.equal(h.readers.length, 0);
        assert.equal(h.errors.length, 1);
        assert.ok(h.errors[0].length > 0);
        assert.deepEqual(h.selected, []);
    });
}

test('an exact 64 MB size is accepted for reading', () => {
    const h = harness();
    h.drop().setEnabled(true);
    drop(h, [file('DOOM.WAD', [1], { size: limit })]);
    assert.equal(h.readers.length, 1);
    assert.deepEqual(h.errors, []);
});

test('read failures and synchronous read errors recover for the next selection', () => {
    const h = harness();
    h.drop().setEnabled(true);
    drop(h, [file()]);
    h.readers[0].fail();
    assert.equal(h.errors.length, 1);
    drop(h, [file('DOOM.WAD', [1], { throwOnRead: true })]);
    assert.equal(h.errors.length, 2);
    drop(h, [file()]);
    h.readers[2].succeed();
    assert.equal(h.selected.length, 1);
});

test('a newer drop aborts an unfinished read and suppresses its stale callback', () => {
    const h = harness();
    h.drop().setEnabled(true);
    drop(h, [file('DOOM.WAD')]);
    const first = h.readers[0];
    const staleLoad = first.onload;
    drop(h, [file('DOOM2.WAD', [1, 2, 3])]);
    assert.equal(first.aborted, true);
    first.result = Uint8Array.from([9, 9]).buffer;
    staleLoad?.({ target: first });
    h.readers[1].succeed();
    assert.deepEqual(h.selected, [{ name: 'DOOM2.WAD', bytes: [1, 2, 3] }]);
    assert.deepEqual(h.errors, []);
});

for (const replacement of ['drop', 'picker']) {
    test(`a rejected ${replacement} cancels the older selection before reporting its error`, () => {
        const h = harness();
        h.picker();
        h.drop().setEnabled(true);
        drop(h, [file()]);
        const reader = h.readers[0];
        const staleLoad = reader.onload;
        if (replacement === 'drop') drop(h, [file(), file('DOOM2.WAD')]);
        else {
            h.inputs[0].files = [file('DOOM.zip')];
            h.inputs[0].dispatch('change');
        }
        assert.equal(reader.aborted, true);
        assert.equal(h.errors.length, 1);
        reader.result = Uint8Array.from([1, 2]).buffer;
        staleLoad?.({ target: reader });
        assert.deepEqual(h.selected, [], 'an older game must not launch after the replacement was rejected');
    });
}

for (const action of ['disable', 'dispose']) {
    test(`${action} during a read aborts it, resets hover, and suppresses stale callbacks`, () => {
        const h = harness();
        const target = h.drop();
        target.setEnabled(true);
        h.document.dispatch('dragenter', { dataTransfer: transfer([file()]) });
        drop(h, [file()]);
        const reader = h.readers[0];
        const staleLoad = reader.onload;
        const staleError = reader.onerror;
        if (action === 'disable') target.setEnabled(false);
        else target.dispose();
        assert.equal(reader.aborted, true);
        reader.result = Uint8Array.from([1, 2]).buffer;
        staleLoad?.({ target: reader });
        staleError?.({ target: reader });
        assert.equal(h.window.listenerCount(), 0);
        assert.equal(h.dragging.at(-1), false);
        assert.deepEqual(h.selected, []);
        assert.deepEqual(h.errors, []);
        if (action === 'dispose') {
            assert.equal(h.document.listenerCount(), 0);
            target.setEnabled(true);
            assert.equal(h.document.listenerCount(), 0, 'a disposed target cannot be reactivated');
        } else {
            assert.equal(drop(h, [file()]).defaultPrevented, true);
            assert.equal(h.readers.length, 1, 'disabled drops cannot start a read');
        }
    });
}

test('the picker opens, resets its value, and can select the same WAD again', () => {
    const h = harness();
    const picker = h.picker();
    const input = h.inputs[0];
    picker.open();
    assert.equal(input.clicks, 1);
    for (let i = 0; i < 2; i++) {
        input.files = [file()];
        input.value = 'C:\\fakepath\\DOOM.WAD';
        input.dispatch('change');
        assert.equal(input.value, '');
        h.readers[i].succeed();
    }
    assert.equal(h.selected.length, 2);
    assert.deepEqual(h.errors, []);
});

test('cancelling the picker leaves the current selection and errors alone', () => {
    const h = harness();
    const picker = h.picker();
    h.drop().setEnabled(true);
    drop(h, [file()]);
    picker.open();
    h.inputs[0].files = [];
    h.inputs[0].dispatch('change');
    assert.equal(h.readers[0].aborted, false);
    h.readers[0].succeed();
    assert.equal(h.selected.length, 1);
    assert.deepEqual(h.errors, []);
});

test('picker disposal removes its input, aborts reads, and makes open a no-op', () => {
    const h = harness();
    const picker = h.picker();
    const input = h.inputs[0];
    input.files = [file()];
    input.dispatch('change');
    const reader = h.readers[0];
    const staleLoad = reader.onload;
    picker.dispose();
    assert.equal(input.isConnected, false);
    assert.equal(reader.aborted, true);
    picker.open();
    assert.equal(input.clicks, 0);
    reader.result = Uint8Array.from([1, 2]).buffer;
    staleLoad?.({ target: reader });
    assert.deepEqual(h.selected, []);
    assert.deepEqual(h.errors, []);
});

test('a valid drop supersedes an unfinished picker selection', () => {
    const h = harness();
    h.picker();
    h.drop().setEnabled(true);
    h.inputs[0].files = [file('DOOM.WAD')];
    h.inputs[0].dispatch('change');
    const first = h.readers[0];
    const staleLoad = first.onload;
    drop(h, [file('DOOM2.WAD', [1, 2])]);
    assert.equal(first.aborted, true);
    first.result = Uint8Array.from([9]).buffer;
    staleLoad?.({ target: first });
    h.readers[1].succeed();
    assert.deepEqual(h.selected, [{ name: 'DOOM2.WAD', bytes: [1, 2] }]);
});
