package com.earnix.webk.runtime.dom.impl.parser;

import com.earnix.webk.runtime.dom.impl.Jsoup;
import com.earnix.webk.runtime.dom.impl.TextUtil;
import com.earnix.webk.runtime.dom.impl.internal.StringUtil;

import com.earnix.webk.runtime.dom.impl.nodes.XmlDeclarationModel;
import com.earnix.webk.runtime.dom.impl.select.Elements;
import com.earnix.webk.runtime.html.impl.DocumentImpl;
import com.earnix.webk.runtime.dom.impl.ElementImpl;
import com.earnix.webk.runtime.dom.impl.NodeImpl;
import com.earnix.webk.runtime.dom.impl.CDATASectionImpl;
import com.earnix.webk.runtime.dom.impl.TextImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests XmlTreeBuilder.
 *
 * @author Jonathan Hedley
 */
public class XmlTreeBuilderTest {
    @Test
    public void testSimpleXmlParse() {
        String xml = "<doc id=2 href='/bar'>Foo <br /><link>One</link><link>Two</link></doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        DocumentImpl doc = tb.parse(xml, "http://foo.com/");
        Assert.assertEquals("<doc id=\"2\" href=\"/bar\">Foo <br /><link>One</link><link>Two</link></doc>",
                TextUtil.stripNewlines(doc.html()));
        assertEquals(doc.getElementById("2").absUrl("href"), "http://foo.com/bar");
    }

    @Test
    public void testPopToClose() {
        // test: </val> closes Two, </bar> ignored
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        DocumentImpl doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void testCommentAndDocType() {
        String xml = "<!DOCTYPE HTML><!-- a comment -->One <qux />Two";
        XmlTreeBuilder tb = new XmlTreeBuilder();
        DocumentImpl doc = tb.parse(xml, "http://foo.com/");
        assertEquals("<!DOCTYPE HTML><!-- a comment -->One <qux />Two",
                TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void testSupplyParserToJsoupClass() {
        String xml = "<doc><val>One<val>Two</val></bar>Three</doc>";
        DocumentImpl doc = Jsoup.parse(xml, "http://foo.com/", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

    @Ignore
    @Test
    public void testSupplyParserToConnection() throws IOException {
        String xmlUrl = "http://direct.infohound.net/tools/jsoup-xml-test.xml";

        // parse with both xml and html parser, ensure different
        DocumentImpl xmlDoc = Jsoup.connect(xmlUrl).parser(Parser.xmlParser()).get();
        DocumentImpl htmlDoc = Jsoup.connect(xmlUrl).parser(Parser.htmlParser()).get();
        DocumentImpl autoXmlDoc = Jsoup.connect(xmlUrl).get(); // check connection auto detects xml, uses xml parser

        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(xmlDoc.html()));
        assertFalse(htmlDoc.equals(xmlDoc));
        assertEquals(xmlDoc, autoXmlDoc);
        assertEquals(1, htmlDoc.select("head").size()); // html parser normalises
        assertEquals(0, xmlDoc.select("head").size()); // xml parser does not
        assertEquals(0, autoXmlDoc.select("head").size()); // xml parser does not
    }

    @Test
    public void testSupplyParserToDataStream() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-test.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        DocumentImpl doc = Jsoup.parse(inStream, null, "http://foo.com", Parser.xmlParser());
        assertEquals("<doc><val>One<val>Two</val>Three</val></doc>",
                TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void testDoesNotForceSelfClosingKnownTags() {
        // html will force "<br>one</br>" to logically "<br />One<br />". XML should be stay "<br>one</br> -- don't recognise tag.
        DocumentImpl htmlDoc = Jsoup.parse("<br>one</br>");
        assertEquals("<br>one\n<br>", htmlDoc.getBody().html());

        DocumentImpl xmlDoc = Jsoup.parse("<br>one</br>", "", Parser.xmlParser());
        assertEquals("<br>one</br>", xmlDoc.html());
    }

    @Test
    public void handlesXmlDeclarationAsDeclaration() {
        String html = "<?xml encoding='UTF-8' ?><body>One</body><!-- comment -->";
        DocumentImpl doc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<?xml encoding=\"UTF-8\"?> <body> One </body> <!-- comment -->",
                StringUtil.normaliseWhitespace(doc.outerHtml()));
        assertEquals("#declaration", doc.childNode(0).nodeName());
        assertEquals("#comment", doc.childNode(2).nodeName());
    }

    @Test
    public void xmlFragment() {
        String xml = "<one src='/foo/' />Two<three><four /></three>";
        List<NodeImpl> nodes = Parser.parseXmlFragment(xml, "http://example.com/");
        assertEquals(3, nodes.size());

        assertEquals("http://example.com/foo/", nodes.get(0).absUrl("src"));
        assertEquals("one", nodes.get(0).nodeName());
        assertEquals("Two", ((TextImpl) nodes.get(1)).text());
    }

    @Test
    public void xmlParseDefaultsToHtmlOutputSyntax() {
        DocumentImpl doc = Jsoup.parse("x", "", Parser.xmlParser());
        assertEquals(com.earnix.webk.runtime.dom.impl.DocumentImpl.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    public void testDoesHandleEOFInTag() {
        String html = "<img src=asdf onerror=\"alert(1)\" x=";
        DocumentImpl xmlDoc = Jsoup.parse(html, "", Parser.xmlParser());
        assertEquals("<img src=\"asdf\" onerror=\"alert(1)\" x=\"\" />", xmlDoc.html());
    }

    @Test
    public void testDetectCharsetEncodingDeclaration() throws IOException, URISyntaxException {
        File xmlFile = new File(XmlTreeBuilder.class.getResource("/htmltests/xml-charset.xml").toURI());
        InputStream inStream = new FileInputStream(xmlFile);
        DocumentImpl doc = Jsoup.parse(inStream, null, "http://example.com/", Parser.xmlParser());
        assertEquals("ISO-8859-1", doc.getCharset().name());
        assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> <data>äöåéü</data>",
                TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void testParseDeclarationAttributes() {
        String xml = "<?xml version='1' encoding='UTF-8' something='else'?><val>One</val>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        XmlDeclarationModel decl = (XmlDeclarationModel) doc.childNode(0);
        assertEquals("1", decl.attr("version"));
        assertEquals("UTF-8", decl.attr("encoding"));
        assertEquals("else", decl.attr("something"));
        assertEquals("version=\"1\" encoding=\"UTF-8\" something=\"else\"", decl.getWholeDeclaration());
        assertEquals("<?xml version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", decl.outerHtml());
    }

    @Test
    public void caseSensitiveDeclaration() {
        String xml = "<?XML version='1' encoding='UTF-8' something='else'?>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<?XML version=\"1\" encoding=\"UTF-8\" something=\"else\"?>", doc.outerHtml());
    }

    @Test
    public void testCreatesValidProlog() {
        DocumentImpl document = DocumentImpl.createShell("");
        document.outputSettings().syntax(com.earnix.webk.runtime.dom.impl.DocumentImpl.OutputSettings.Syntax.xml);
        document.charset(Charset.forName("utf-8"));
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<html>\n" +
                " <head></head>\n" +
                " <body></body>\n" +
                "</html>", document.outerHtml());
    }

    @Test
    public void preservesCaseByDefault() {
        String xml = "<CHECK>One</CHECK><TEST ID=1>Check</TEST>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("<CHECK>One</CHECK><TEST ID=\"1\">Check</TEST>", TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void appendPreservesCaseByDefault() {
        String xml = "<One>One</One>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Elements one = doc.select("One");
        one.append("<Two ID=2>Two</Two>");
        assertEquals("<One>One<Two ID=\"2\">Two</Two></One>", TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void canNormalizeCase() {
        String xml = "<TEST ID=1>Check</TEST>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser().settings(ParseSettings.htmlDefault));
        assertEquals("<test id=\"1\">Check</test>", TextUtil.stripNewlines(doc.html()));
    }

    @Test
    public void normalizesDiscordantTags() {
        Parser parser = Parser.xmlParser().settings(ParseSettings.htmlDefault);
        DocumentImpl document = Jsoup.parse("<div>test</DIV><p></p>", "", parser);
        assertEquals("<div>\n test\n</div>\n<p></p>", document.html());
        // was failing -> toString() = "<div>\n test\n <p></p>\n</div>"
    }

    @Test
    public void roundTripsCdata() {
        String xml = "<div id=1><![CDATA[\n<html>\n <foo><&amp;]]></div>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());

        ElementImpl div = doc.getElementById("1");
        assertEquals("<html>\n <foo><&amp;", div.text());
        assertEquals(0, div.getChildren().size());
        assertEquals(1, div.childNodeSize()); // no elements, one text node

        assertEquals("<div id=\"1\"><![CDATA[\n<html>\n <foo><&amp;]]>\n</div>", div.outerHtml());

        CDATASectionImpl cdata = (CDATASectionImpl) div.textNodes().get(0);
        assertEquals("\n<html>\n <foo><&amp;", cdata.text());
    }

    @Test
    public void cdataPreservesWhiteSpace() {
        String xml = "<script type=\"text/javascript\">//<![CDATA[\n\n  foo();\n//]]></script>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals(xml, doc.outerHtml());

        assertEquals("//\n\n  foo();\n//", doc.selectFirst("script").text());
    }

    @Test
    public void handlesDodgyXmlDecl() {
        String xml = "<?xml version='1.0'><val>One</val>";
        DocumentImpl doc = Jsoup.parse(xml, "", Parser.xmlParser());
        assertEquals("One", doc.select("val").text());
    }
}
