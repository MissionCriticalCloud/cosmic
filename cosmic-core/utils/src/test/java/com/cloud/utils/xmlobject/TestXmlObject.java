//

//

package com.cloud.utils.xmlobject;

import org.junit.Test;

public class TestXmlObject {

    void p(final String str) {
        System.out.println(str);
    }

    @Test
    public void test() {

        // deprecated, since we no longer use component.xml.in any more
        /*
            XmlObject xo = XmlObjectParser.parseFromFile("z:/components.xml.in");
            p(xo.getTag());
            p((String) xo.get("system-integrity-checker.checker").toString());
            List<XmlObject> lst = xo.get("management-server.adapters");
            for (XmlObject x : lst) {
                List<XmlObject> lst1 = x.getAsList("adapter");
                for (XmlObject y : lst1) {
                    p(y.toString());
                }
            }
            */

        final XmlObject xml = new XmlObject("vlan").putElement("vlan-id", String.valueOf(19)).putElement("tagged",
                new XmlObject("teng").putElement("name", "0/0")
        ).putElement("shutdown", "false");
        System.out.println(xml.toString());
    }
}
