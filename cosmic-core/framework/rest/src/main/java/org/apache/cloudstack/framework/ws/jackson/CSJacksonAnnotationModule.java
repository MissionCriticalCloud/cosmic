package org.apache.cloudstack.framework.ws.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

/**
 * This module extends SimpleModle so that our annotations can be processed.
 */
public class CSJacksonAnnotationModule extends Module {

    @Override
    public String getModuleName() {
        return "CloudStackSupplementalModule";
    }

    @Override
    public Version version() {
        return new Version(1, 0, 0, "", "org.apache.cloudstack", "cloudstack-framework-rest");
    }

    @Override
    public void setupModule(final SetupContext ctx) {
        ctx.appendAnnotationIntrospector(new CSJacksonAnnotationIntrospector());
    }
}
