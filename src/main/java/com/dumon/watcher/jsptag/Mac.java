package com.dumon.watcher.jsptag;

import com.dumon.watcher.helper.ConversionHelper;

import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * Custom tag helps converting MAC Address from long to user-friendly UI view
 */
public class Mac extends SimpleTagSupport {
    private StringWriter sw = new StringWriter();

    public void doTag() throws JspException, IOException {
        String mac = parseAndConvert();
        getJspContext().getOut().println(mac);
    }

    private String parseAndConvert() throws JspException, IOException {
        getJspBody().invoke(sw);
        long mac = Long.parseLong(sw.toString());
        return ConversionHelper.longMacToString(mac);
    }
}
