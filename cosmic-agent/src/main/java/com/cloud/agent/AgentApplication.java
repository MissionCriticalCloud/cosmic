package com.cloud.agent;

import com.cloud.agent.service.AgentShell;

import javax.naming.ConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = "com.cloud.agent",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "(?!com.cloud.agent.service).*")
        })
public class AgentApplication implements CommandLineRunner {
    private static final Logger s_logger = LoggerFactory.getLogger(AgentApplication.class);

    @Autowired
    AgentShell agentShell;

    public static void main(final String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Override
    public void run(final String... strings) throws Exception {
        s_logger.info("Starting Cosmic Agent...");

        try {
            agentShell.init(strings);
            agentShell.start();
        } catch (final ConfigurationException e) {
            s_logger.error(e.getMessage());
        }
    }
}
