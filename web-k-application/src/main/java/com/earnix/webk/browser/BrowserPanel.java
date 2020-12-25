/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.earnix.webk.browser;

import com.earnix.webk.event.DocumentListener;
import com.earnix.webk.layout.SharedContext;
import com.earnix.webk.resource.XMLResource;
import com.earnix.webk.runtime.html.impl.DocumentImpl;
import com.earnix.webk.simple.FSScrollPane;
import com.earnix.webk.swing.ImageResourceLoader;
import com.earnix.webk.swing.ScalableXHTMLPanel;
import com.earnix.webk.swing.SwingReplacedElementFactory;
import com.earnix.webk.util.GeneralUtil;
import com.earnix.webk.util.XRLog;
import com.earnix.webk.util.XRRuntimeException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;


@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class BrowserPanel extends JPanel implements DocumentListener {

    static final Logger logger = Logger.getLogger("app.browser");

    WebKApplication browserApplication;
    BrowserPanelListener listener;
    ScalableXHTMLPanel view;
    BrowserStatus status;
    BrowserUserAgent manager;

    JButton forward;
    JButton backward;
    JButton stop;
    JButton reload;
    JButton goHome;
    JTextField url;
    JScrollPane scroll;
    JButton print_preview;
    JButton goToPage;
    JToolBar toolbar;

    BrowserPanel(WebKApplication root, BrowserPanelListener listener) {
        super();
        this.browserApplication = root;
        this.listener = listener;
    }

    void init() {
        forward = new JButton();
        backward = new JButton();
        stop = new JButton();
        reload = new JButton();
        goToPage = new JButton();
        goHome = new JButton();

        url = new JTextField();
        url.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                url.selectAll();
            }

            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                url.select(0, 0);
            }
        });


        manager = new BrowserUserAgent(getBrowserApplication());
        view = new ScalableXHTMLPanel(manager);
        view.setFocusCycleRoot(true);
        manager.setRepaintListener(view);
        ImageResourceLoader irl = new ImageResourceLoader();
        irl.setRepaintListener(view);
        manager.setImageResourceLoader(irl);
        view.getSharedContext().setReplacedElementFactory(new SwingReplacedElementFactory(view, irl));
        view.addDocumentListener(manager);
        view.setCenteredPagedView(true);
        view.setBackground(Color.LIGHT_GRAY);
        scroll = new FSScrollPane(view);
        print_preview = new JButton();

        loadCustomFonts();

        status = new BrowserStatus();
        status.init();

        initToolbar();

        int textWidth = 200;
        view.setPreferredSize(new Dimension(textWidth, textWidth));

        setLayout(new BorderLayout());
        this.add(scroll, BorderLayout.CENTER);


        view.setFormSubmissionListener(query -> view.setDocument(query, true));
    }

    private void initToolbar() {
        toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.add(backward);
        toolbar.add(forward);
        toolbar.add(reload);
        toolbar.add(goHome);
        toolbar.add(url);
        toolbar.add(goToPage);
        // disabled for R6
        // toolbar.add(print);
        toolbar.setFloatable(false);
    }

    private void loadCustomFonts() {
        SharedContext rc = view.getSharedContext();
        try
        {
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream fontFileStream = classLoader.getResourceAsStream("demos/rendering/fonts/fuzz.ttf");
            rc.setFontMapping("Fuzz", Font.createFont(Font.TRUETYPE_FONT, fontFileStream));
        } catch (Exception ex) {
            log.error("loadCustomFonts", ex);
        }
    }

    public void createLayout() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gbl);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gbl.setConstraints(toolbar, c);
        add(toolbar);

        //c.gridx = 0;
        c.gridx++;
        c.gridy++;
        c.weightx = c.weighty = 0.0;
        c.insets = new Insets(5, 0, 5, 5);
        gbl.setConstraints(backward, c);
        add(backward);

        c.gridx++;
        gbl.setConstraints(forward, c);
        add(forward);

        c.gridx++;
        gbl.setConstraints(reload, c);
        add(reload);

        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = c.weighty = 0.0;
        gbl.setConstraints(print_preview, c);
        add(print_preview);

        c.gridx++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipadx = 5;
        c.ipady = 5;
        c.weightx = 10.0;
        c.insets = new Insets(5, 0, 5, 0);
        gbl.setConstraints(url, c);
        url.setBorder(BorderFactory.createLoweredBevelBorder());
        add(url);

        c.gridx++;
        c.fill = GridBagConstraints.NONE;
        c.weightx = c.weighty = 0.0;
        c.insets = new Insets(0, 5, 0, 0);
        gbl.setConstraints(goToPage, c);
        add(goToPage);

        c.gridx = 0;
        c.gridy++;
        c.ipadx = 0;
        c.ipady = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 7;
        c.weightx = c.weighty = 10.0;
        gbl.setConstraints(scroll, c);
        add(scroll);

        c.gridx = 0;
        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.1;
        gbl.setConstraints(status, c);
        add(status);
    }

    void createActions() {
        // set text to "" to avoid showing action text in button--
        // we only want it in menu items
        val actions = browserApplication.getActions();
        backward.setAction(actions.getBackward());
        backward.setText("");
        forward.setAction(actions.getForward());
        forward.setText("");
        reload.setAction(actions.getReload());
        reload.setText("");
        goHome.setAction(actions.getGoHome());
        goHome.setText("");
        print_preview.setAction(actions.getPrintPreview());
        print_preview.setText("");

        url.setAction(actions.getLoad());
        goToPage.setAction(actions.getGoToPage());
        updateButtons();
    }


    void goForward() {
        String uri = manager.getForward();
        view.setDocument(uri);
        updateButtons();
    }

    void goBack() {
        String uri = manager.getBack();
        view.setDocument(uri);
        updateButtons();
    }

    void reloadPage() {
        logger.info("Reloading Page: ");
        if (manager.getBaseURL() != null) {
            loadPage(manager.getBaseURL());
        }
    }

    //TODO: make this part of an implementation of UserAgentCallback instead
    public void loadPage(final String url) {
        try {
            logger.info("Loading Page: " + url);
            view.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            view.setDocument(url);
            view.addDocumentListener(BrowserPanel.this);

            updateButtons();

            setStatus("Successfully loaded: " + url);

            if (listener != null) {
                listener.pageLoadSuccess(url, view.getDocumentTitle());
            }
        } catch (XRRuntimeException ex) {
            XRLog.general(Level.SEVERE, "Runtime exception", ex);
            setStatus("Can't load document");
            handlePageLoadFailed(url, ex);
        } catch (Exception ex) {
            XRLog.general(Level.SEVERE, "Could not load page for display.", ex);
            ex.printStackTrace();
        }
    }

    private void handlePageLoadFailed(String url_text, XRRuntimeException ex) {
        final DocumentImpl xr;
        final String rootCause = getRootCause(ex);
        final String msg = GeneralUtil.escapeHTML(addLineBreaks(rootCause, 80));
        String notFound =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<!DOCTYPE html PUBLIC \" -//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" +
                        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\n" +
                        "<body>\n" +
                        "<h1>Document can't be loaded</h1>\n" +
                        "<p>Could not load the page at \n" +
                        "<pre>" + GeneralUtil.escapeHTML(url_text) + "</pre>\n" +
                        "</p>\n" +
                        "<p>The page failed to load; the error was </p>\n" +
                        "<pre>" + msg + "</pre>\n" +
                        "</body>\n" +
                        "</html>";

        xr = XMLResource.load(new StringReader(notFound));
        SwingUtilities.invokeLater(() -> browserApplication.getPanel().view.setDocument(xr, null));
    }

    private String addLineBreaks(String _text, int maxLineLength) {
        StringBuffer broken = new StringBuffer(_text.length() + 10);
        boolean needBreak = false;
        for (int i = 0; i < _text.length(); i++) {
            if (i > 0 && i % maxLineLength == 0) needBreak = true;

            final char c = _text.charAt(i);
            if (needBreak && Character.isWhitespace(c)) {
                System.out.println("Breaking: " + broken.toString());
                needBreak = false;
                broken.append('\n');
            } else {
                broken.append(c);
            }
        }
        System.out.println("Broken! " + broken.toString());
        return broken.toString();
    }

    private String getRootCause(Exception ex) {
        // FIXME
        Throwable cause = ex;
        while (cause != null) {
            cause = cause.getCause();
        }

        return cause == null ? ex.getMessage() : cause.getMessage();
    }

    @Override
    public void documentStarted() {
        // TODO...
    }

    @Override
    public void documentLoaded() {
    }

    @Override
    public void documentRendered() {
        view.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void setStatus(String txt) {
        status.getText().setText(txt);
    }

    protected void updateButtons() {
        val actions = browserApplication.getActions();
        if (manager.hasBack()) {
            actions.getBackward().setEnabled(true);
        } else {
            actions.getBackward().setEnabled(false);
        }
        if (manager.hasForward()) {
            actions.getForward().setEnabled(true);
        } else {
            actions.getForward().setEnabled(false);
        }

        url.setText(manager.getBaseURL());
    }


    public void onLayoutException(Throwable t) {
        // TODO: clean
        t.printStackTrace();
    }

    public void onRenderException(Throwable t) {
        // TODO: clean
        t.printStackTrace();
    }
}