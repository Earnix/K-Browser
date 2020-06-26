package com.earnix.webk.runtime.dom.impl.integration.servlets;

import com.earnix.webk.runtime.dom.impl.helper.DataUtil;
import com.earnix.webk.runtime.dom.impl.integration.TestServer;
import com.earnix.webk.runtime.dom.impl.internal.StringUtil;
import org.eclipse.jetty.server.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Enumeration;

import static com.earnix.webk.runtime.dom.impl.nodes.Entities.escape;

public class EchoServlet extends BaseServlet {
    public static final String Url = TestServer.map(EchoServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doIt(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doIt(req, res);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doIt(req, res);
    }

    private void doIt(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        boolean isMulti = maybeEnableMultipart(req);

        res.setContentType(TextHtml);
        res.setStatus(HttpServletResponse.SC_OK);
        PrintWriter w = res.getWriter();

        w.write("<title>Webserver Environment Variables</title>\n" +
                "    <style type=\"text/css\">\n" +
                "      body, td, th {font: 10pt Verdana, Arial, sans-serif; text-align: left}\n" +
                "      th {font-weight: bold}        \n" +
                "    </style>\n" +
                "    <body>\n" +
                "    <table border=\"0\">");

        // some get items
        write(w, "Method", req.getMethod());
        write(w, "Request URI", req.getRequestURI());
        write(w, "Query String", req.getQueryString());

        // request headers (why is it an enumeration?)
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            Enumeration<String> headers = req.getHeaders(header);
            while (headers.hasMoreElements()) {
                write(w, header, headers.nextElement());
            }
        }

        // the request params
        Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String[] values = req.getParameterValues(name);
            write(w, name, StringUtil.join(values, ", "));
        }

        // post body
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(req.getInputStream(), 0);
        String postData = new String(byteBuffer.array(), "UTF-8");
        if (!StringUtil.isBlank(postData)) {
            write(w, "Post Data", postData);
        }

        // file uploads
        if (isMulti) {
            Collection<Part> parts = req.getParts();
            write(w, "Parts", String.valueOf(parts.size()));

            for (Part part : parts) {
                String name = part.getName();
                write(w, "Part " + name + " ContentType", part.getContentType());
                write(w, "Part " + name + " Name", name);
                write(w, "Part " + name + " Filename", part.getSubmittedFileName());
                write(w, "Part " + name + " Size", String.valueOf(part.getSize()));
                part.delete();
            }
        }

        w.println("</table>");
    }

    private static void write(PrintWriter w, String key, String val) {
        w.println("<tr><th>" + escape(key) + "</th><td>" + escape(val) + "</td></tr>");
    }

    // allow the servlet to run as a main program, for local test
    public static void main(String[] args) {
        TestServer.start();
        System.out.println(Url);
    }

    private static boolean maybeEnableMultipart(HttpServletRequest req) {
        boolean isMulti = req.getContentType() != null
                && req.getContentType().startsWith("multipart/form-data");

        if (isMulti) {
            req.setAttribute(Request.MULTIPART_CONFIG_ELEMENT, new MultipartConfigElement(
                    System.getProperty("java.io.tmpdir")));
        }
        return isMulti;
    }
}
