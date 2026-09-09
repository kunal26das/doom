export class GraphicsMutationObserver {
    targets = [];
    disconnected = false;
    constructor(observers, callback) { this.callback = callback; observers.push(this); }
    observe(target) { this.targets.push(target); this.disconnected = false; }
    disconnect() { this.targets = []; this.disconnected = true; }
}
