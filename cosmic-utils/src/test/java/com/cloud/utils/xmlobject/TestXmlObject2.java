package com.cloud.utils.xmlobject;

import org.junit.Test;

public class TestXmlObject2 {
    @Test
    public void test() {
        XmlObject root = new XmlObject("test");
        root.putElement("key1", "value1").putElement("key2", "value2");
        p(root.dump());

        final XmlObject c1 = new XmlObject("child1");
        final XmlObject c2 = new XmlObject("child2");
        c2.putElement("ckey1", "value1");
        c1.putElement(c2.getTag(), c2);
        root.putElement(c1.getTag(), c1);
        p(root.dump());

        root =
                xo("test2").putElement("key1", "value1")
                           .putElement("child1", xo("child1").setText("yyy"))
                           .putElement("child1", xo("child1").putElement("child2", xo("child2").putElement("child3", xo("child3").putElement("key3", "value3").setText("xxxxx"))));

        p(root.dump());
    }

    void p(final String str) {
        System.out.println(str);
    }

    XmlObject xo(final String name) {
        return new XmlObject(name);
    }
}
