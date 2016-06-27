package com.cloud.servlet;

import org.junit.Assert;
import org.junit.Test;

public class ConsoleProxyServletTest {
    @Test
    public void escapeHTML() {
        Assert.assertNull(ConsoleProxyServlet.escapeHTML(null));
        Assert.assertEquals("", ConsoleProxyServlet.escapeHTML(""));
        Assert.assertEquals("&lt;strangevmname&gt;",
                ConsoleProxyServlet.escapeHTML("<strangevmname>"));
        Assert.assertEquals("&quot;strange&nbsp;vm&quot;",
                ConsoleProxyServlet.escapeHTML("\"strange vm\""));
        Assert.assertEquals("Nothing-extraordinary-anyway.",
                ConsoleProxyServlet.escapeHTML("Nothing-extraordinary-anyway."));
    }
}
