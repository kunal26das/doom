import { GraphicsEventSurface } from './GraphicsEventSurface.mjs';

export class GraphicsElement extends GraphicsEventSurface {
    #elements;
    constructor(document, elements, tagName, nodeType = 1) {
        super();
        this.tagName = tagName.toUpperCase();
        this.nodeType = nodeType;
        this.children = [];
        this.parentNode = null;
        this.shadowRoot = null;
        this.style = {};
        this.attributes = new Map();
        this.ownText = '';
        this.ownerDocument = document;
        this.#elements = elements;
        elements.push(this);
    }

    get isConnected() {
        return this === this.ownerDocument.documentElement || Boolean((this.parentNode ?? this.host)?.isConnected);
    }
    get childNodes() { return this.children; }
    get textContent() { return this.ownText + this.children.map((child) => child.textContent).join(' '); }
    set textContent(value) { this.ownText = value; this.children = []; }
    setAttribute(name, value) { this.attributes.set(name, String(value)); }
    getAttribute(name) { return this.attributes.get(name) ?? null; }
    removeAttribute(name) { this.attributes.delete(name); }
    appendChild(child) { child.remove(); this.children.push(child); child.parentNode = this; return child; }
    append(...children) { for (const child of children) this.appendChild(child); }
    removeChild(child) { child.remove(); return child; }
    remove() {
        if (this.parentNode) this.parentNode.children = this.parentNode.children.filter((child) => child !== this);
        this.parentNode = null;
    }
    contains(node) { return this === node || this.children.some((child) => child.contains(node)); }
    matches(selector) {
        if (selector === '*') return this.nodeType === 1;
        if (selector.startsWith('#')) return this.id === selector.slice(1);
        const attribute = selector.match(/^\[([^=]+)=["']?([^"'\]]+)["']?\]$/);
        if (attribute) return this.getAttribute(attribute[1]) === attribute[2];
        return this.tagName.toLowerCase() === selector;
    }
    querySelectorAll(selector) {
        return this.children.flatMap((child) => [
            ...(child.matches(selector) ? [child] : []), ...child.querySelectorAll(selector),
        ]);
    }
    querySelector(selector) { return this.querySelectorAll(selector)[0] ?? null; }
    focus() { this.ownerDocument.activeElement = this; }
    click() { this.dispatch('click'); }
    attachShadow() {
        this.shadowRoot = new GraphicsElement(this.ownerDocument, this.#elements, '#shadow-root', 11);
        this.shadowRoot.host = this;
        return this.shadowRoot;
    }
}
