package com.cloud.servlet;

import com.cloud.utils.LogUtils;
import com.cloud.utils.SerialVersionUID;
import com.cloud.utils.component.ComponentContext;
import com.cloud.utils.db.TransactionLegacy;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class CloudStartupServlet extends HttpServlet {
    public static final Logger s_logger = LoggerFactory.getLogger(CloudStartupServlet.class.getName());
    static final long serialVersionUID = SerialVersionUID.CloudStartupServlet;

    Timer _timer = new Timer();

    @Override
    public void init(final ServletConfig config) throws ServletException {
        LogUtils.initLog4j("log4j-cloud.xml");
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, config.getServletContext());

        // wait when condition is ready for initialization
        _timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (ComponentContext.getApplicationContext() != null) {
                    _timer.cancel();

                    final TransactionLegacy txn = TransactionLegacy.open(TransactionLegacy.CLOUD_DB);
                    try {
                        ComponentContext.initComponentsLifeCycle();
                    } finally {
                        txn.close();
                    }
                }
            }
        }, 0, 1000);
    }
}
