export class WadEventSurface {
    listeners = new Map();

    addEventListener(type, callback) {
        const callbacks = this.listeners.get(type) ?? new Set();
        callbacks.add(callback);
        this.listeners.set(type, callbacks);
    }

    removeEventListener(type, callback) {
        this.listeners.get(type)?.delete(callback);
    }

    dispatch(type, properties = {}) {
        const event = {
            type,
            target: this,
            defaultPrevented: false,
            preventDefault() { this.defaultPrevented = true; },
            stopPropagation() {},
            ...properties,
        };
        for (const callback of [...(this.listeners.get(type) ?? [])]) callback(event);
        this[`on${type}`]?.(event);
        return event;
    }

    listenerCount() {
        return [...this.listeners.values()].reduce((count, callbacks) => count + callbacks.size, 0);
    }
}
