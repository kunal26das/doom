export class GraphicsEventSurface {
    listeners = new Map();
    events = [];

    addEventListener(type, callback) {
        const callbacks = this.listeners.get(type) ?? new Set();
        callbacks.add(callback);
        this.listeners.set(type, callbacks);
    }

    removeEventListener(type, callback) { this.listeners.get(type)?.delete(callback); }

    dispatch(type, properties = {}) {
        const event = {
            type, target: this, defaultPrevented: false, propagationStopped: false,
            preventDefault() { this.defaultPrevented = true; },
            stopPropagation() { this.propagationStopped = true; },
            ...properties,
        };
        this.dispatchEvent(event);
        return event;
    }

    dispatchEvent(event) {
        this.events.push(event.type);
        for (const callback of [...(this.listeners.get(event.type) ?? [])]) callback(event);
        this[`on${event.type}`]?.(event);
        return !event.defaultPrevented;
    }

    listenerCount() {
        return [...this.listeners.values()].reduce((count, callbacks) => count + callbacks.size, 0);
    }
}
