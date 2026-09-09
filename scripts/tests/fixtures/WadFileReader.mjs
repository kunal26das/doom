export class WadFileReader {
    result = null;
    readyState = 0;
    aborted = false;

    constructor(readers) { readers.push(this); }

    readAsArrayBuffer(file) {
        if (file.throwOnRead) throw new Error('Local file is unavailable');
        this.file = file;
        this.readyState = 1;
    }

    abort() {
        this.aborted = true;
        this.readyState = 2;
        this.onabort?.({ target: this });
    }

    succeed(bytes = this.file.bytes) {
        this.result = Uint8Array.from(bytes).buffer;
        this.readyState = 2;
        this.onload?.({ target: this });
    }

    fail() {
        this.readyState = 2;
        this.onerror?.({ target: this });
    }
}
