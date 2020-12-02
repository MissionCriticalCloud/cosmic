package com.cloud.agent;

import java.security.Security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = "com.cloud.agent",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "(?!com.cloud.agent.service).*")
        })

public class AgentApplication {
    static {
        Security.setProperty("jdk.tls.disabledAlgorithms", "SSLv3, TLSv1, TLSv1.1");
        Security.setProperty("jdk.tls.ephemeralDHKeySize", "2048");
    }

    public static void main(final String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
