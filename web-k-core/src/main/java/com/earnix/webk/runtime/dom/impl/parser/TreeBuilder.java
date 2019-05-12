package com.earnix.webk.runtime.dom.impl.parser;

import com.earnix.webk.runtime.dom.impl.helper.Validate;
import com.earnix.webk.runtime.dom.impl.nodes.AttributesModel;
import com.earnix.webk.runtime.dom.impl.HTMLSelectElementImpl;
import com.earnix.webk.runtime.html.canvas.impl.HTMLCanvasElementImpl;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.runtime.dom.impl.NodeImpl;
import com.earnix.webk.runtime.html.impl.DocumentImpl;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jonathan Hedley
 */
public abstract class TreeBuilder {
    protected Parser parser;
    CharacterReader reader;
    Tokeniser tokeniser;
    protected DocumentImpl doc; // current doc we are building into
    protected ArrayList<ElementImpl> stack; // the stack of open elements
    protected String baseUri; // current base uri, for creating new elements
    protected Token currentToken; // currentToken is used only for error tracking.
    protected ParseSettings settings;

    private Token.StartTag start = new Token.StartTag(); // start tag to process
    private Token.EndTag end = new Token.EndTag();

    abstract ParseSettings defaultSettings();

    protected void initialiseParse(Reader input, String baseUri, Parser parser) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new DocumentImpl(baseUri);
        doc.parser(parser);
        this.parser = parser;
        settings = parser.settings();
        reader = new CharacterReader(input);
        currentToken = null;
        tokeniser = new Tokeniser(reader, parser.getErrors());
        stack = new ArrayList<>(32);
        this.baseUri = baseUri;
    }

    DocumentImpl parse(Reader input, String baseUri, Parser parser) {
        initialiseParse(input, baseUri, parser);
        runParser();
        return doc;
    }

    abstract List<NodeImpl> parseFragment(String inputFragment, ElementImpl context, String baseUri, Parser parser);

    protected void runParser() {
        while (true) {
            Token token = tokeniser.read();
            process(token);
            token.reset();

            if (token.type == Token.TokenType.EOF)
                break;
        }
    }

    protected abstract boolean process(Token token);

    protected boolean processStartTag(String name) {
        if (currentToken == start) { // don't recycle an in-use token
            return process(new Token.StartTag().name(name));
        }
        return process(start.reset().name(name));
    }

    public boolean processStartTag(String name, AttributesModel attrs) {
        if (currentToken == start) { // don't recycle an in-use token
            return process(new Token.StartTag().nameAttr(name, attrs));
        }
        start.reset();
        start.nameAttr(name, attrs);
        return process(start);
    }

    protected boolean processEndTag(String name) {
        if (currentToken == end) { // don't recycle an in-use token
            return process(new Token.EndTag().name(name));
        }
        return process(end.reset().name(name));
    }


    protected ElementImpl currentElement() {
        int size = stack.size();
        return size > 0 ? stack.get(size - 1) : null;
    }
    
    public static ElementImpl createElement(Tag tag, String baseUri, AttributesModel attributes) {
        switch (tag.getName().trim().toLowerCase()) {
            case "canvas":
                return new HTMLCanvasElementImpl(tag, baseUri, attributes);
            case "select":
                return new HTMLSelectElementImpl(tag, baseUri, attributes);
            default: return new ElementImpl(tag, baseUri, attributes);
        }
    }
    
    public static ElementImpl createElement(Tag tag, String baseUri) {
        return createElement(tag, baseUri, null);
    }
}
