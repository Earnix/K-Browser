package com.earnix.webk.runtime.whatwg_dom.impl.integration;

import com.earnix.webk.runtime.whatwg_dom.impl.Connection;
import com.earnix.webk.runtime.whatwg_dom.impl.Jsoup;
import com.earnix.webk.runtime.whatwg_dom.impl.UncheckedIOException;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.Deflateservlet;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.EchoServlet;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.HelloServlet;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.InterruptedServlet;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.RedirectServlet;
import com.earnix.webk.runtime.whatwg_dom.impl.integration.servlets.SlowRider;
import com.earnix.webk.runtime.html.impl.DocumentImpl;
import com.earnix.webk.runtime.whatwg_dom.impl.ElementImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

import static com.earnix.webk.runtime.whatwg_dom.impl.integration.UrlConnectTest.browserUa;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests Jsoup.connect against a local server.
 */
public class ConnectTest {
    private static String echoUrl;

    @BeforeClass
    public static void setUp() {
        TestServer.start();
        echoUrl = EchoServlet.Url;
    }

    @AfterClass
    public static void tearDown() {
        TestServer.stop();
    }

    @Test
    public void canConnectToLocalServer() throws IOException {
        String url = HelloServlet.Url;
        DocumentImpl doc = Jsoup.connect(url).get();
        ElementImpl p = doc.selectFirst("p");
        assertEquals("Hello, World!", p.text());
    }

    @Test
    public void fetchURl() throws IOException {
        DocumentImpl doc = Jsoup.parse(new URL(echoUrl), 10 * 1000);
        assertTrue(doc.getTitle().contains("Environment Variables"));
    }

    @Test
    public void fetchURIWithWihtespace() throws IOException {
        Connection con = Jsoup.connect(echoUrl + "#with whitespaces");
        DocumentImpl doc = con.get();
        assertTrue(doc.getTitle().contains("Environment Variables"));
    }

    @Test
    public void exceptOnUnsupportedProtocol() {
        String url = "file://etc/passwd";
        boolean threw = false;
        try {
            DocumentImpl doc = Jsoup.connect(url).get();
        } catch (MalformedURLException e) {
            threw = true;
            assertEquals("java.net.MalformedURLException: Only http & https protocols supported", e.toString());
        } catch (IOException e) {
        }
        assertTrue(threw);
    }

    private static String ihVal(String key, DocumentImpl doc) {
        final ElementImpl first = doc.select("th:contains(" + key + ") + td").first();
        return first != null ? first.text() : null;
    }

    @Test
    public void doesPost() throws IOException {
        DocumentImpl doc = Jsoup.connect(echoUrl)
                .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
                .cookie("auth", "token")
                .post();

        assertEquals("POST", ihVal("Method", doc));
        assertEquals("gzip", ihVal("Accept-Encoding", doc));
        assertEquals("auth=token", ihVal("Cookie", doc));
        assertEquals("度一下", ihVal("百", doc));
        assertEquals("Jsoup, Jonathan", ihVal("uname", doc));
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", ihVal("Content-Type", doc));
    }

    @Test
    public void sendsRequestBodyJsonWithData() throws IOException {
        final String body = "{key:value}";
        DocumentImpl doc = Jsoup.connect(echoUrl)
                .requestBody(body)
                .header("Content-Type", "application/json")
                .userAgent(browserUa)
                .data("foo", "true")
                .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("application/json", ihVal("Content-Type", doc));
        assertEquals("foo=true", ihVal("Query String", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

    @Test
    public void sendsRequestBodyJsonWithoutData() throws IOException {
        final String body = "{key:value}";
        DocumentImpl doc = Jsoup.connect(echoUrl)
                .requestBody(body)
                .header("Content-Type", "application/json")
                .userAgent(browserUa)
                .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("application/json", ihVal("Content-Type", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

    @Test
    public void sendsRequestBody() throws IOException {
        final String body = "{key:value}";
        DocumentImpl doc = Jsoup.connect(echoUrl)
                .requestBody(body)
                .header("Content-Type", "text/plain")
                .userAgent(browserUa)
                .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("text/plain", ihVal("Content-Type", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

    @Test
    public void sendsRequestBodyWithUrlParams() throws IOException {
        final String body = "{key:value}";
        DocumentImpl doc = Jsoup.connect(echoUrl)
                .requestBody(body)
                .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
                .header("Content-Type", "text/plain") // todo - if user sets content-type, we should append postcharset
                .userAgent(browserUa)
                .post();
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("uname=Jsoup&uname=Jonathan&%E7%99%BE=%E5%BA%A6%E4%B8%80%E4%B8%8B", ihVal("Query String", doc));
        assertEquals(body, ihVal("Post Data", doc));
    }

    @Test
    public void doesGet() throws IOException {
        Connection con = Jsoup.connect(echoUrl + "?what=the")
                .userAgent("Mozilla")
                .referrer("http://example.com")
                .data("what", "about & me?");

        DocumentImpl doc = con.get();
        assertEquals("what=the&what=about+%26+me%3F", ihVal("Query String", doc));
        assertEquals("the, about & me?", ihVal("what", doc));
        assertEquals("Mozilla", ihVal("User-Agent", doc));
        assertEquals("http://example.com", ihVal("Referer", doc));
    }

    @Test
    public void doesPut() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl)
                .data("uname", "Jsoup", "uname", "Jonathan", "百", "度一下")
                .cookie("auth", "token")
                .method(Connection.Method.PUT)
                .execute();

        DocumentImpl doc = res.parse();
        assertEquals("PUT", ihVal("Method", doc));
        assertEquals("gzip", ihVal("Accept-Encoding", doc));
        assertEquals("auth=token", ihVal("Cookie", doc));
    }

    // Slow Rider tests. Ignored by default so tests don't take aaages
    @Ignore
    @Test
    public void canInterruptBodyStringRead() throws IOException, InterruptedException {
        // todo - implement in interruptable channels, so it's immediate
        final String[] body = new String[1];
        Thread runner = new Thread(new Runnable() {
            public void run() {
                try {
                    Connection.Response res = Jsoup.connect(SlowRider.Url)
                            .timeout(15 * 1000)
                            .execute();
                    body[0] = res.body();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        runner.start();
        Thread.sleep(1000 * 3);
        runner.interrupt();
        assertTrue(runner.isInterrupted());
        runner.join();

        assertTrue(body[0].length() > 0);
        assertTrue(body[0].contains("<p>Are you still there?"));
    }

    @Ignore
    @Test
    public void canInterruptDocumentRead() throws IOException, InterruptedException {
        // todo - implement in interruptable channels, so it's immediate
        final String[] body = new String[1];
        Thread runner = new Thread(new Runnable() {
            public void run() {
                try {
                    Connection.Response res = Jsoup.connect(SlowRider.Url)
                            .timeout(15 * 1000)
                            .execute();
                    body[0] = res.parse().text();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        });

        runner.start();
        Thread.sleep(1000 * 3);
        runner.interrupt();
        assertTrue(runner.isInterrupted());
        runner.join();

        assertTrue(body[0].length() == 0); // doesn't ready a failed doc
    }

    @Ignore
    @Test
    public void totalTimeout() throws IOException {
        int timeout = 3 * 1000;
        long start = System.currentTimeMillis();
        boolean threw = false;
        try {
            Jsoup.connect(SlowRider.Url).timeout(timeout).get();
        } catch (SocketTimeoutException e) {
            long end = System.currentTimeMillis();
            long took = end - start;
            assertTrue(("Time taken was " + took), took > timeout);
            assertTrue(("Time taken was " + took), took < timeout * 1.2);
            threw = true;
        }

        assertTrue(threw);
    }

    @Ignore
    @Test
    public void slowReadOk() throws IOException {
        // make sure that a slow read that is under the request timeout is still OK
        DocumentImpl doc = Jsoup.connect(SlowRider.Url)
                .data(SlowRider.MaxTimeParam, "2000") // the request completes in 2 seconds
                .get();

        ElementImpl h1 = doc.selectFirst("h1");
        assertEquals("outatime", h1.text());
    }

    @Ignore
    @Test
    public void infiniteReadSupported() throws IOException {
        DocumentImpl doc = Jsoup.connect(SlowRider.Url)
                .timeout(0)
                .data(SlowRider.MaxTimeParam, "2000")
                .get();

        ElementImpl h1 = doc.selectFirst("h1");
        assertEquals("outatime", h1.text());
    }

    /**
     * Tests upload of content to a remote service.
     */
//    @Test
    public void postFiles() throws IOException {
        File thumb = ParseTest.getFile("/htmltests/thumb.jpg");
        File html = ParseTest.getFile("/htmltests/google-ipod.html");

        DocumentImpl res = Jsoup
                .connect(EchoServlet.Url)
                .data("firstname", "Jay")
                .data("firstPart", thumb.getName(), new FileInputStream(thumb), "image/jpeg")
                .data("secondPart", html.getName(), new FileInputStream(html)) // defaults to "application-octetstream";
                .data("surname", "Soup")
                .post();

        assertEquals("4", ihVal("Parts", res));

        assertEquals("application/octet-stream", ihVal("Part secondPart ContentType", res));
        assertEquals("secondPart", ihVal("Part secondPart Name", res));
        assertEquals("google-ipod.html", ihVal("Part secondPart Filename", res));
        assertEquals("43963", ihVal("Part secondPart Size", res));

        assertEquals("image/jpeg", ihVal("Part firstPart ContentType", res));
        assertEquals("firstPart", ihVal("Part firstPart Name", res));
        assertEquals("thumb.jpg", ihVal("Part firstPart Filename", res));
        assertEquals("1052", ihVal("Part firstPart Size", res));

        assertEquals("Jay", ihVal("firstname", res));
        assertEquals("Soup", ihVal("surname", res));

        /*
        <tr><th>Part secondPart ContentType</th><td>application/octet-stream</td></tr>
        <tr><th>Part secondPart Name</th><td>secondPart</td></tr>
        <tr><th>Part secondPart Filename</th><td>google-ipod.html</td></tr>
        <tr><th>Part secondPart Size</th><td>43972</td></tr>
        <tr><th>Part firstPart ContentType</th><td>image/jpeg</td></tr>
        <tr><th>Part firstPart Name</th><td>firstPart</td></tr>
        <tr><th>Part firstPart Filename</th><td>thumb.jpg</td></tr>
        <tr><th>Part firstPart Size</th><td>1052</td></tr>
         */
    }

    @Test
    public void multipleParsesOkAfterBufferUp() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute().bufferUp();

        DocumentImpl doc = res.parse();
        assertTrue(doc.getTitle().contains("Environment"));

        DocumentImpl doc2 = res.parse();
        assertTrue(doc2.getTitle().contains("Environment"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void bodyAfterParseThrowsValidationError() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        DocumentImpl doc = res.parse();
        String body = res.body();
    }

    @Test
    public void bodyAndBytesAvailableBeforeParse() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        String body = res.body();
        assertTrue(body.contains("Environment"));
        byte[] bytes = res.bodyAsBytes();
        assertTrue(bytes.length > 100);

        DocumentImpl doc = res.parse();
        assertTrue(doc.getTitle().contains("Environment"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseParseThrowsValidates() throws IOException {
        Connection.Response res = Jsoup.connect(echoUrl).execute();
        DocumentImpl doc = res.parse();
        assertTrue(doc.getTitle().contains("Environment"));
        DocumentImpl doc2 = res.parse(); // should blow up because the response input stream has been drained
    }


    @Test
    public void multiCookieSet() throws IOException {
        Connection con = Jsoup.connect("http://direct.infohound.net/tools/302-cookie.pl");
        Connection.Response res = con.execute();

        // test cookies set by redirect:
        Map<String, String> cookies = res.cookies();
        assertEquals("asdfg123", cookies.get("token"));
        assertEquals("jhy", cookies.get("uid"));

        // send those cookies into the echo URL by map:
        DocumentImpl doc = Jsoup.connect(echoUrl).cookies(cookies).get();
        assertEquals("token=asdfg123; uid=jhy", ihVal("Cookie", doc));
    }

    //    @Test
    public void supportsDeflate() throws IOException {
        Connection.Response res = Jsoup.connect(Deflateservlet.Url).execute();
        assertEquals("deflate", res.header("Content-Encoding"));

        DocumentImpl doc = res.parse();
        assertEquals("Hello, World!", doc.selectFirst("p").text());
    }

    //    @Test
    public void handlesEmptyStreamDuringParseRead() throws IOException {
        // this handles situations where the remote server sets a content length greater than it actually writes

        Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
                .timeout(200)
                .execute();

        boolean threw = false;
        try {
            DocumentImpl document = res.parse();
            assertEquals("Something", document.title().get());
        } catch (IOException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    //    @Test
    public void handlesEmtpyStreamDuringBufferdRead() throws IOException {
        Connection.Response res = Jsoup.connect(InterruptedServlet.Url)
                .timeout(200)
                .execute();

        boolean threw = false;
        try {
            res.bufferUp();
        } catch (UncheckedIOException e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void handlesRedirect() throws IOException {
        DocumentImpl doc = Jsoup.connect(RedirectServlet.Url)
                .data(RedirectServlet.LocationParam, HelloServlet.Url)
                .get();

        ElementImpl p = doc.selectFirst("p");
        assertEquals("Hello, World!", p.text());

        assertEquals(HelloServlet.Url, doc.getLocation());
    }

    @Test
    public void handlesEmptyRedirect() throws IOException {
        boolean threw = false;
        try {
            Connection.Response res = Jsoup.connect(RedirectServlet.Url)
                    .execute();
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Too many redirects"));
            threw = true;
        }
        assertTrue(threw);
    }

    @Test
    public void doesNotPostFor302() throws IOException {
        final DocumentImpl doc = Jsoup.connect(RedirectServlet.Url)
                .data("Hello", "there")
                .data(RedirectServlet.LocationParam, EchoServlet.Url)
                .post();

        assertEquals(EchoServlet.Url, doc.getLocation());
        assertEquals("GET", ihVal("Method", doc));
        assertNull(ihVal("Hello", doc)); // data not sent
    }

    @Test
    public void doesPostFor307() throws IOException {
        final DocumentImpl doc = Jsoup.connect(RedirectServlet.Url)
                .data("Hello", "there")
                .data(RedirectServlet.LocationParam, EchoServlet.Url)
                .data(RedirectServlet.CodeParam, "307")
                .post();

        assertEquals(EchoServlet.Url, doc.getLocation());
        assertEquals("POST", ihVal("Method", doc));
        assertEquals("there", ihVal("Hello", doc));
    }
}
