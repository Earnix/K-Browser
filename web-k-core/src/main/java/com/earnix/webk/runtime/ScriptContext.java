package com.earnix.webk.runtime;

import com.earnix.webk.event.DocumentListener;
import com.earnix.webk.runtime.html.canvas.impl.CanvasGradientImpl;
import com.earnix.webk.runtime.html.canvas.impl.CanvasPatternImpl;
import com.earnix.webk.runtime.html.impl.DocumentImpl;
import com.earnix.webk.runtime.html.impl.WindowImpl;
import com.earnix.webk.runtime.ui_events.UIEventInit;
import com.earnix.webk.runtime.ui_events.impl.MouseEventsAdapter;
import com.earnix.webk.runtime.ui_events.impl.UIEventImpl;
import com.earnix.webk.runtime.web_idl.Constructor;
import com.earnix.webk.runtime.web_idl.Exposed;
import com.earnix.webk.runtime.web_idl.impl.FunctionAdapter;
import com.earnix.webk.runtime.web_idl.impl.WebIDLAdapter;
import com.earnix.webk.runtime.dom.impl.EventImpl;
import com.earnix.webk.runtime.dom.impl.EventManager;
import com.earnix.webk.runtime.xhr.impl.XMLHttpRequestImpl;
import com.earnix.webk.swing.BasicPanel;
import jdk.nashorn.api.scripting.AbstractJSObject;
import jdk.nashorn.api.scripting.NashornException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import jdk.nashorn.api.scripting.URLReader;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptException;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static com.earnix.webk.util.GeneralUtil.isJavascriptES6;
import static javax.script.ScriptContext.ENGINE_SCOPE;

/**
 * @author Taras Maslov
 * 5/21/2018
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScriptContext implements DocumentListener {
    //    static ScriptContext instance;
    @Getter NashornScriptEngine engine;
    javax.script.ScriptContext context;

    BasicPanel panel;
    DocumentImpl document;

    @Getter
    EventManager eventManager;

    @Getter
    MouseEventsAdapter mouseEventsAdapter;


    WindowImpl window;
    private WebIDLAdapter<WindowImpl> windowAdapter;

    int documentHash;
    boolean rendered;

    public ScriptContext(BasicPanel panel) {
        this.panel = panel;
        // initializing mouse events translation
        eventManager = new EventManager(this);
        mouseEventsAdapter = new MouseEventsAdapter(this);
    }

    public void dispatchLoadEvents() {

        if (rendered) {
            //Type	load
            //Interface	UIEvent if generated from a user interface, Event otherwise.
            //Sync / Async	Async
            //Bubbles	No
            //Trusted Targets	Window, Document, Element
            //Cancelable	No
            //Default action	None
            //Context
            //(trusted events)	
            //Event.target : common object whose contained resources have loaded
            //UIEvent.view : Window
            //UIEvent.detail : 0

            storeDocumentHash();
            val eventInit = new UIEventInit();
            eventInit.bubbles = false;
            eventInit.cancelable = false;
            eventInit.view = window;
            eventManager.publishEvent(window, new UIEventImpl("load", eventInit));
            handleDocumentHashUpdate();
        } else {
            SwingUtilities.invokeLater(this::dispatchLoadEvents);
        }
    }
    
    private void dispatchUnloadEvents() {

        val eventInit = new UIEventInit();
        eventInit.bubbles = false;
        eventInit.cancelable = false;
        eventInit.view = window;
        eventManager.publishEvent(window, new UIEventImpl("unload", eventInit));
        
        document.walkElementsTree(e -> {
            eventInit.bubbles = false;
            eventInit.cancelable = false;
            eventInit.view = window;

            val event = new UIEventImpl("unload", eventInit);
            eventManager.publishEvent(e, event);
        });

      
    }

    /**
     * @return browser panel
     */
    public BasicPanel getPanel() {
        return panel;
    }

    @Override
    public void documentStarted() { 
    }

    @Override
    public void documentRendered() {
        // on render all replaced elements are re-created, need to update their listeners
        mouseEventsAdapter.addChildrenListeners();
        rendered = true;
    }

    @Override
    public void documentLoaded() {
        handleNewDocument();
    }

    @Override
    public void onLayoutException(Throwable t) {
    }

    @Override
    public void onRenderException(Throwable t) {
    }

    private void initEngine() {
        List<String> options = new ArrayList<>();
        options.add("--no-java");

        if(isJavascriptES6())
        {
            options.add("--language=es6");
        }

        NashornScriptEngineFactory jsFactory = new NashornScriptEngineFactory();
        engine = (NashornScriptEngine) jsFactory.getScriptEngine(options.toArray(new String[options.size()]));
        context = engine.getContext();

        expose(CanvasGradientImpl.class);
        expose(CanvasPatternImpl.class);
        expose(XMLHttpRequestImpl.class);
        expose(EventImpl.class);

        window = new WindowImpl(this);
        windowAdapter = WebIDLAdapter.obtain(this, window);

        try {
            context.setAttribute("window", engine.eval("this"), ENGINE_SCOPE);
            context.setAttribute("self", engine.eval("this"), ENGINE_SCOPE);
            context.setAttribute("__win", windowAdapter, ENGINE_SCOPE);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }


        // no way to track attributes write operation on global scope...
        // limitation - to use "window" property
        windowAdapter.keySet().forEach(key -> {
            if (!key.equals("window") && !key.equals("self")) {
                try {
                    engine.eval("this." + key + " = " + " __win." + key + ";");
                    engine.eval("self." + key + " = " + " __win." + key + ";");
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            engine.eval(new URLReader(ScriptContext.class.getResource("/symbol-polyfill.js")), context);
            engine.eval(new URLReader(ScriptContext.class.getResource("/es6-shim.min.js")), context);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    private void expose(Class implementationClass) {

        ClassUtils.getAllInterfaces(implementationClass).forEach(i -> {
            if (i.isAnnotationPresent(Exposed.class)) {
                context.setAttribute(i.getSimpleName(), new AbstractJSObject() {
                    @Override
                    public Object newObject(Object... args) {
                        try {

                            Object target;
                            try {
                                @SuppressWarnings("unchecked") val constructor = implementationClass.getDeclaredConstructor(ScriptContext.class);
                                target = constructor.newInstance(ScriptContext.this);
                            } catch (NoSuchMethodException e) {
                                target = implementationClass.newInstance();
                            } 

                            val adapter = WebIDLAdapter.obtain(ScriptContext.this, target);
                            // todo check here @Constructor annotation
                            FunctionAdapter constructor = (FunctionAdapter) adapter.getMember("constructor");
                            if (constructor != null && i.getAnnotation(Constructor.class) != null) {
                                constructor.call(this, args);
                            }
                            return adapter;
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public String getClassName() {
                        return i.getSimpleName();
                    }
                }, ENGINE_SCOPE);
            }
        });

    }

    private void handleNewDocument() {
        val nextDocument = panel.getDocument();
        if (nextDocument != document) {
            rendered = false;
            if (window != null) {
                window.clearTasks();
            }
            
            if(document != null){
                dispatchUnloadEvents();
            }
            
            initEngine();

//            val doc = new DocumentImpl(getPanel().getSharedContext().getBaseURL())
            nextDocument.setScriptContext(this);
            window.setDocument(nextDocument);
            context.setAttribute("document", windowAdapter.getMember("document"), ENGINE_SCOPE);
            
            document = nextDocument;
            
            val scripts = panel.getDocument().getElementsByTag("script");
            log.trace("Document has {} scripts", scripts.size());
            for (int i = 0; i < scripts.size(); i++) {
                val script = scripts.get(i);
                if (StringUtils.isNotBlank(script.data())) {
                    try {
                        eval(script.data());
                    } catch (Exception e) {
                        log.warn("script.eval", e);
                    }
                } else {
                    val scriptUri = script.getAttributes().get("src");
                    if (StringUtils.isNotBlank(scriptUri)) {
                        try {
                            val scriptText = panel.getSharedContext().getUac().getScriptResource(scriptUri);
                            eval(scriptText);
                        } catch (RuntimeException e) {
                            log.debug("script.src", e);
                        }
                    }
                }

            }
            
            dispatchLoadEvents();

        }
    }

    public Object eval(String scr) {
        Object res;
        try {
            res = engine.eval(scr, context);
            windowAdapter.keySet().forEach(key -> {
                if (!key.equals("window") && !key.equals("self")) {
                    try {
                        if ((Boolean) engine.eval("!!__win." + key)) {
                            engine.eval("this." + key + " = " + " __win." + key);
                            engine.eval("self." + key + " = " + " __win." + key);
                        }
                    } catch (ScriptException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            synchronize();
            panel.reset();
        } catch (ScriptException e) {
            if (e.getCause() instanceof NashornException) {
                log.error(NashornException.getScriptStackString(e.getCause()));
            }
            throw new RuntimeException(e);
        }
        return res;
    }

    /**
     * Updates fields of window WebIDL implementation from current fields of global Nashorn context
     */
    private void synchronize() {
        try {
            engine.eval("this.__keys = Object.keys(this); for (var i = 0; i < __keys.length; i++) { var tempI = i; if "
                    + "(this[__keys[tempI]]) this.__win[__keys[tempI]] = this[__keys[tempI]]; }");
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }
    
    public WindowImpl getWindow() {
        return window;
    }


    public void storeDocumentHash() {
        documentHash = document != null ? document.outerHtml().hashCode() : 0;
    }

    /**
     * Checks whether document model is change since last {@link #storeDocumentHash()} call and 
     * resets browser panel (causes it's re-rendering), if so.
     */
    public void handleDocumentHashUpdate() {
        int currentHash = document != null ? document.outerHtml().hashCode() : 0;
        if (documentHash != currentHash) {
            panel.reset();
        }
    }

    public void setRendered(boolean rendered) {
        this.rendered = rendered;
    }
}
