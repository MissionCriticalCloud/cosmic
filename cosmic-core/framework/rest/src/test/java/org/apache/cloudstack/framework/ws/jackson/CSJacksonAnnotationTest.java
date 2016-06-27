package org.apache.cloudstack.framework.ws.jackson;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CSJacksonAnnotationTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    @Ignore
    public void test() {
        final ObjectMapper mapper = new ObjectMapper();
        final JaxbAnnotationModule jaxbModule = new JaxbAnnotationModule();
        jaxbModule.setPriority(Priority.SECONDARY);
        mapper.registerModule(jaxbModule);
        mapper.registerModule(new CSJacksonAnnotationModule());

        final StringWriter writer = new StringWriter();

        final TestVO vo = new TestVO(1000, "name");
        vo.names = new ArrayList<>();
        vo.names.add("name1");
        vo.names.add("name2");
        vo.values = new HashMap<>();
        vo.values.put("key1", 1000l);
        vo.values.put("key2", 2000l);
        vo.vo2.name = "testvoname2";
        vo.pods = "abcde";

        try {
            mapper.writeValue(writer, vo);
        } catch (final JsonGenerationException e) {
            e.printStackTrace();
        } catch (final JsonMappingException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        System.out.print(writer.getBuffer().toString());
    }

    @XmlRootElement(name = "xml-test2")
    public class Test2VO {
        public String name;
    }

    @XmlRootElement(name = "abc")
    public class TestVO {
        public int id;

        public Map<String, Long> values;

        public String name;

        public List<String> names;

        public String pods;

        @XmlElement(name = "test2")
        public Test2VO vo2 = new Test2VO();

        public TestVO(final int id, final String name) {
            this.id = id;
            this.name = name;
        }

        @Url(clazz = TestVO.class, method = "getName")
        public String getName() {
            return name;
        }

        @Url(clazz = TestVO.class, method = "getNames", type = List.class)
        public List<String> getNames() {
            return names;
        }
    }
}
