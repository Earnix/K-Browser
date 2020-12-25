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

import com.earnix.webk.util.GeneralUtil;
import com.earnix.webk.util.XRLog;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.logging.Level;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class WebKApplication {

    private final static String UI_SCALE_ENABLED_PROP = "sun.java2d.uiScale.enabled";


    BrowserActions actions;
    ValidationHandler validationHandler = new ValidationHandler();
    BrowserMenuBar menu;
    BrowserPanel panel;
    String startPage;

    JFrame frame;
    @Setter JFrame validationConsole = null;

    WebKApplication() {
        this(System.getProperty("url", "demo:demos/index.html"));
    }

    private WebKApplication(String startPage) {
        log.info("starting up");
        this.startPage = startPage;
    }

    /**
     * Initializes all UI components but does not display frame and does not load any pages.
     */
    void initUI() {
        if (GeneralUtil.isMacOSX()) {
            try {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("com.apple.mrj.application.apple.menu.about.name", "FS Browser");
            } catch (Exception ex) {
                try {
                    log.warn("Error initializing the mac properties", ex);
                } catch (Exception ex2) {
                    //System.out.println("error writing to the log file!" + ex2);
                    //ex2.printStackTrace();
                }
            }
        } else {
            setLookAndFeel();
        }

        frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        log.info("creating UI");
        actions = new BrowserActions(this);
        actions.init();

        panel = new BrowserPanel(this, new FrameBrowserPanelListener());
        panel.init();
        panel.createActions();

        menu = new BrowserMenuBar(this);
        menu.init();
        menu.createLayout();
        menu.createActions();

        frame.setJMenuBar(menu);

        frame.getContentPane().add(panel.getToolbar(), BorderLayout.PAGE_START);
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
        frame.getContentPane().add(panel.getStatus(), BorderLayout.PAGE_END);
        frame.pack();
        frame.setSize(1024, 768);
    }

    public static void main(final String[] args) {
        if (System.getProperty(UI_SCALE_ENABLED_PROP) == null)
        {
            System.setProperty(UI_SCALE_ENABLED_PROP, "false");
        }
        EventQueue.invokeLater(() -> {
            final WebKApplication bs = new WebKApplication();
            bs.initUI();
            bs.launch();
        });
    }

    /**
     * Loads the first page (specified in the constructor) and shows the frame.
     */
    void launch() {
        try {
            panel.loadPage(startPage);
            frame.setVisible(true);
        } catch (Exception ex) {
            XRLog.general(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private static void setLookAndFeel() {
        boolean lnfSet = false;
        try {
            UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            lnfSet = true;
        } catch (Throwable ignored) {
        }
        if (!lnfSet) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    class FrameBrowserPanelListener implements BrowserPanelListener {

        public void pageLoadSuccess(String url, String title) {
            String esComment = GeneralUtil.isJavascriptES6() ? "(Nashorn ES6)" : "(Nashorn EcmaScript2015)";
            frame.setTitle(title + (title.length() > 0 ? " - " : "") + "Web-K Browser " + esComment);
        }
    }
}