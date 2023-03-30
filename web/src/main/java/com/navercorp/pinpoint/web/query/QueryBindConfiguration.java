package com.navercorp.pinpoint.web.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "com.navercorp.pinpoint.web.query.controller",
        "com.navercorp.pinpoint.web.query.service",
})
public class QueryBindConfiguration {

    private final Logger logger = LogManager.getLogger(QueryBindConfiguration.class);

    public QueryBindConfiguration() {
        logger.info("Install {}", QueryBindConfiguration.class.getSimpleName());
    }
}
