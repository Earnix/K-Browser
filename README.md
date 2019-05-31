[![Build Status](https://travis-ci.org/Earnix/Web-K.svg?branch=master)](https://travis-ci.org/Earnix/Web-K)

<p align="center"><img src="/web-k-core/src/main/resources/demos/Web-K.png"></img></p>

Web-K is [FlyingSaucer](https://github.com/flyingsaucerproject/flyingsaucer)-based pure Java browser and Swing browser component. In addition to FlyingSaucer features it supports:
* a `<script>` tag with limited set of JS features (like Canvas). Nashorn JavaScript runtime is used. See features list below. 
* Not strict HTML. Standard Java XML parser and DOM replaced with modified [JSoup](https://github.com/jhy/jsoup) library. It provides support of HTML5 at parsing level.
* Embedded `<svg>` elements, implemented with [svgSalamander](https://github.com/blackears/svgSalamander).

FlyingSaucer PDF and SWT rendering was removed.

Main use case - pure Java web view component. Not intended to be used as standalone web browser (at least at this moment).

### Usage entry point 
`com.earnix.webk.simple.XHTMLPanel` can be used as Swing web view component. `com.earnix.webk.browser.WebKApplication` launches demo browser application.

### JavaScript runtime

JavaScript APIs implementation based on [WHATWG DOM](https://dom.spec.whatwg.org/) and [WHATWG HTML](https://html.spec.whatwg.org/multipage/) specifications. It is currently very limited.

##### Examples of supported JS

To see full running example, download sources and launch `com.earnix.webk.browser.WebKApplication`. It will automatically load example web page which demonstrates renderering and scripting features. Small scripting features overview:

Element creation, updates and append:
```js
var div = document.createElement("div");
div.textContent = "Some text";
div.style.width = "200px";
div.style.height = "100px";
document.body.appendChild(div);

var main = document.getElementById("main");
main.innerHTML = "<p style='background-color: red; color: white'>Red Paragraph</p>"
```

Intervals and timeouts:
```js
// appends dot each 300 milliseconds
window.setInterval(function () {
    const target = document.getElementById("target");
    target.textContent = target.textContent + ".";
}, 300);

// appends message after 3 seconds timeout
window.setTimeout(function () {
    const div = document.createElement("div");
    document.body.appendChild(div);
}, 3000);
```

Mouse and change events:
```js
const div = document.querySelector("div#target");

div.onclick = function (event) {
    console.log("Click at " + event.clientX + " " + event.clientY);
};

div.ondblclick = function (event) {
    console.log("Double click at " + event.clientX + " " + event.clientY);
};

div.onmouseenter = function (event) {
    console.log("Mouse entered at " + event.clientX + " " + event.clientY);
};

const field = document.querySelector("input[type=text]");
field.onchange = function () {
    console.log("Input field value changed: " + field.value)
};
```

Basic AJAX with XMLHttpRequest:

```js
const request = new XMLHttpRequest();
const url = "some url";
request.addEventListener("load", function(){
    // And do whatever whatever is needed with response.
    let element = document.createElement("div");
    element.innerHTML = request.responseText;
    document.body.appendChild(element);
});
request.open("GET", url);
request.send();
``` 

Canvas creation and drawing:
```js
var canvas = document.createElement("canvas");
canvas.setAttribute("width", "500");
canvas.setAttribute("height", "500");
document.body.appendChild(canvas);
var ctx = canvas.getContext("2d");
ctx.strokeStyle = "rgba(0,255, 100, 0.5)";
ctx.lineWidth = 5;
ctx.strokeRect(10, 10, 80, 80);
ctx.fillStyle = "red";
ctx.fillText("Hello", 10, 10);
ctx.save();
ctx.lineCap = "round";
ctx.strokeStyle = "#AA1188";
ctx.moveTo(200, 200);
ctx.setLineDash([10, 20]);
ctx.lineDashOffset = 15;
ctx.lineTo(300, 300);
ctx.stroke();
ctx.restore();
ctx.stroke();
```
Browser supports chart based library [ChartJS](http://www.chartjs.org/).

##### Important technical limitations
* Setting attributes of global `window` object should be done via `window` reference.
* `this` of event listening callback may not refer to event target, so event target should be accessed directly. Example:
```js
let xhr = new XMLHttpRequest();
xhr.onreadystatechange = function() {
    // not working approach:
    console.log(this.responseText);
    // working approach:
    console.log(xhr.responseText);
}
```

##### JS Runtime support

Web-K provides current Nashorn ES6 features as `let` and `const` support, and also features provided by [es6-shim](https://github.com/paulmillr/es6-shim) polyfill. 

##### List of supported browser JS APIs
* HTMLCanvasElement
* CanvasRenderingContext2D (but not images-related functions)
* TextMetrics.width
* Document.getElementsByTagName
* Document.getElementsByClassName
* Document.createElement
* Document.getElementById
* Document.createTextNode
* Document.querySelector
* Document.querySelectorAll
* Document.body
* Document.childElementCount
* Element.tagName
* Element.className
* Element.id
* Element.classList
* Element.hasAttributes
* Element.attributes
* Element.getElementsByTagName
* Element.getElementsByClassName
* Element.previousElementSibling
* Element.nextElementSibling
* Element.children
* Element.style
* Element.innerHTML
* Element.outerHTML (read-only)
* Element.clientWidth
* Element.setAttribute
* Element.toggleAttribute
* Element.removeAttribute
* Element.hasAttribute
* Element.childElementCount
* HTMLSelectElement.value
* HTMLSelectElement.selectedIndex
* HTMLSelectElement.selectedOptions
* HTMLSelectElement.options
* HTMLSelectElement.type
* HTMLSelectElement.multiple
* Node.parentElement
* Node.childNodes
* Node.appendChild
* Node.addEventListener
* Node.removeEventListener
* Node.dispatchEvent
* NodeList
* CSSStyleAttribute
* Attr.name
* Attr.value
* Attr.nodeType
* Attr.nodeName
* Attr.parentElement
* Attr.nodeValue
* Attr.textContent
* Event.type
* HTMLCollection
* DOMTokenList.*
* Window.onload
* Window.document
* Window.console.log
* Window.console.error
* Window.setInterval
* Window.setTimeout
* Window.location
* Window.getComputedStyle

##### Web-K System Properties
| Property name | Default value | Description |
|---------------|---------------|-------------|
|com.earnix.eo.webk.network.xhr-file-url|true|Disables sending of AJAX GET requests to local file system|

##### For developers: Adding new JS features
1. Package `com.earnix.webk.runtime.web_idl` contains set of classes which provide apis to describe [Web IDL specifications](https://heycam.github.io/webidl/) with java language features (see `com.earnix.webk.runtime.html.canvas.CanvasRenderingContext2D` as example interface which is [CanvasRenderingContext2D WebIDL specification](https://html.spec.whatwg.org/multipage/canvas.html#canvasrenderingcontext2d) described with Java). To add new APIs, firstly select / create package of feature (or standard) in `com.earnix.webk.runtime`, and and describe there new APIs (as Java interfaces based on WebIDL)
2. Inside target package (like `com.earnix.webk.runtime.html` for general features of [WHATWG HTML standard](https://html.spec.whatwg.org/)), create or locate `impl` package, and implement interfaces created before (see `com.earnix.webk.runtime.html.canvas.impl.CanvasRenderingContext2DImpl` as example)
3. If something new should be added to global `window` object, describe it in `com.earnix.webk.runtime.ScriptContext#initEngine`. If you just need to expose a constructor it's enough to use `com.earnix.webk.runtime.web_idl.Exposed` on target WebIDL interface.

The core of analyze and adaptation of WebIDL implementations is done by `com.earnix.webk.runtime.web_idl.impl.WebIDLAdapter`, so if you need to support new WebILD feature (like annotation), you need to handle it there. It adapts Nashorn JavaScript object API to concrete WebIDL Java implementation classes.

Scripting features logging level can be changed within `web-k-core/src/main/resources/logback.xml`

## license
GNU Lesser General Public License v3.0
