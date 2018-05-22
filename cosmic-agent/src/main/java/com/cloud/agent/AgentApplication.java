package com.cloud.agent;

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
    public static void main(final String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
