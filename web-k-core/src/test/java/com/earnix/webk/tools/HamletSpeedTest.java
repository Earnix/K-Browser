package com.earnix.webk.tools;

import com.earnix.webk.simple.Graphics2DRenderer;
import com.earnix.webk.util.XRLog;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.util.Date;

@Slf4j
public class HamletSpeedTest {

    @Test
    public void test() throws Exception {
        XRLog.setLoggingEnabled(true);
        long total = 0;
        for (int i = 0; i < 10; i++) {
            Date start = new Date();
            Graphics2DRenderer.renderToImage(
                    new File("tests/profiling/xhtml/hamlet.xhtml").toURL().toExternalForm(),
                    700,
                    700
            );
            Date end = new Date();
            long diff = (end.getTime() - start.getTime());
            log.info("ms = " + diff);
            total += diff;
        }
        long avg = total / 10;
        log.trace("Average : " + avg);
        assert avg < 8000;
    }

}
