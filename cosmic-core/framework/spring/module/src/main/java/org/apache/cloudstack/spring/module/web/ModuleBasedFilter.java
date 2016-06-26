package org.apache.cloudstack.spring.module.web;

import org.apache.cloudstack.spring.module.factory.CloudStackSpringContext;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

public abstract class ModuleBasedFilter implements Filter {

    boolean enabled = false;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        final String module = filterConfig.getInitParameter("module");
        final CloudStackSpringContext context = (CloudStackSpringContext) filterConfig.getServletContext().getAttribute(CloudStackSpringContext.CLOUDSTACK_CONTEXT_SERVLET_KEY);

        if (context == null) {
            return;
        }

        final ApplicationContext applicationContext = context.getApplicationContextForWeb(module);
        if (applicationContext != null) {
            final AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
            if (factory != null) {
                factory.autowireBean(this);
                enabled = true;
            }
        }
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
