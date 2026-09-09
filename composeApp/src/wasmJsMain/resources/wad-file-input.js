(function () {
    'use strict';

    // The picker and drop target share one local read. A newer selection replaces it.
    let activeRead = null;

    function cancelRead(owner) {
        if (!activeRead || (owner && activeRead.owner !== owner)) return;
        const operation = activeRead;
        activeRead = null;
        operation.cancelled = true;
        operation.reader.onload = null;
        operation.reader.onerror = null;
        operation.reader.onabort = null;
        try { operation.reader.abort(); } catch (_) { /* Already finished. */ }
    }

    function readFile(owner, file, limit, onSelected, onError) {
        if (owner.disposed || !owner.enabled) return;
        cancelRead();
        if (!file || !/\.wad$/i.test(file.name || '')) {
            onError('Choose a .wad file, such as DOOM.WAD or DOOM2.WAD.');
            return;
        }
        if (file.size > limit) {
            onError('Choose a WAD no larger than 64 MB.');
            return;
        }
        if (file.size === 0) {
            onError('This WAD is empty. Choose the WAD from your installed game.');
            return;
        }

        const reader = new FileReader();
        const operation = { owner, reader, cancelled: false };
        activeRead = operation;
        const isCurrent = () => !operation.cancelled && !owner.disposed && owner.enabled;
        const release = () => {
            reader.onload = null;
            reader.onerror = null;
            reader.onabort = null;
            if (activeRead === operation) activeRead = null;
        };
        reader.onload = () => {
            if (!isCurrent()) { release(); return; }
            let bytes;
            try { bytes = new Int8Array(reader.result); }
            catch (_) {
                release();
                onError('Could not read this file. Choose a locally available WAD and try again.');
                return;
            }
            release();
            if (bytes.byteLength > limit) onError('Choose a WAD no larger than 64 MB.');
            else onSelected(file.name, bytes);
        };
        reader.onerror = () => {
            const report = isCurrent();
            release();
            if (report) onError('Could not read this file. Choose a locally available WAD and try again.');
        };
        reader.onabort = release;
        try { reader.readAsArrayBuffer(file); }
        catch (_) {
            const report = isCurrent();
            release();
            if (report) onError('The selected file could not be opened. Please try again.');
        }
    }

    function createPicker(limit, onSelected, onError) {
        const owner = { enabled: true, disposed: false };
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.wad,application/x-doom';
        input.style.display = 'none';
        input.multiple = false;
        document.body.appendChild(input);
        input.onchange = () => {
            const files = Array.from(input.files || []);
            input.value = '';
            if (owner.disposed || files.length === 0) return;
            cancelRead();
            if (files.length !== 1) {
                onError('Choose one WAD file at a time.');
                return;
            }
            readFile(owner, files[0], limit, onSelected, onError);
        };
        return {
            // Called synchronously in the Compose button click to keep user activation.
            open() { if (!owner.disposed) input.click(); },
            dispose() {
                if (owner.disposed) return;
                owner.disposed = true;
                input.onchange = null;
                cancelRead(owner);
                input.remove();
            },
        };
    }

    function hasFiles(event) {
        const transfer = event.dataTransfer;
        return !!transfer && (
            Array.from(transfer.types || []).includes('Files') ||
            Array.from(transfer.items || []).some(item => item.kind === 'file')
        );
    }

    function createDropTarget(limit, onSelected, onError, onDraggingChanged) {
        const owner = { enabled: false, disposed: false };
        let depth = 0;
        let dragging = false;
        function setDragging(value) {
            if (dragging === value) return;
            dragging = value;
            onDraggingChanged(value);
        }
        function reset() {
            depth = 0;
            setDragging(false);
        }
        // Keep files from navigating away while the launcher is opening a selection.
        // Plain text and URL drags retain their normal browser behavior.
        function guard(event) {
            if (!hasFiles(event)) return;
            event.preventDefault();
            if (!owner.enabled && event.type === 'dragover') event.dataTransfer.dropEffect = 'none';
        }
        function enter(event) {
            if (!hasFiles(event)) return;
            event.preventDefault();
            depth += 1;
            setDragging(true);
        }
        function over(event) {
            if (!hasFiles(event)) return;
            event.preventDefault();
            event.dataTransfer.dropEffect = 'copy';
            setDragging(true);
        }
        function leave(event) {
            if (!hasFiles(event) && depth === 0) return;
            depth = Math.max(0, depth - 1);
            if (depth === 0) setDragging(false);
        }
        function drop(event) {
            if (!hasFiles(event)) return;
            event.preventDefault();
            reset();
            cancelRead();
            const transfer = event.dataTransfer;
            const items = Array.from(transfer.items || []).filter(item => item.kind === 'file');
            const isDirectory = items.some(item => {
                if (typeof item.webkitGetAsEntry !== 'function') return false;
                try { return !!item.webkitGetAsEntry()?.isDirectory; }
                catch (_) { return false; }
            });
            if (isDirectory) {
                onError('Drop a WAD file, not a folder.');
                return;
            }
            const files = Array.from(transfer.files || []);
            if (files.length !== 1 || items.length > 1) {
                onError('Drop one WAD file at a time.');
                return;
            }
            readFile(owner, files[0], limit, onSelected, onError);
        }
        const listeners = { dragenter: enter, dragover: over, dragleave: leave, drop };
        function setEnabled(enabled) {
            if (owner.disposed || owner.enabled === enabled) return;
            owner.enabled = enabled;
            for (const [type, listener] of Object.entries(listeners)) {
                if (enabled) document.addEventListener(type, listener, true);
                else document.removeEventListener(type, listener, true);
            }
            if (enabled) window.addEventListener('dragend', reset, true);
            else {
                window.removeEventListener('dragend', reset, true);
                reset();
                cancelRead(owner);
            }
        }
        document.addEventListener('dragover', guard, true);
        document.addEventListener('drop', guard, true);
        return {
            setEnabled,
            dispose() {
                if (owner.disposed) return;
                setEnabled(false);
                owner.disposed = true;
                document.removeEventListener('dragover', guard, true);
                document.removeEventListener('drop', guard, true);
            },
        };
    }

    globalThis.DoomWadFiles = { createPicker, createDropTarget };
}());
