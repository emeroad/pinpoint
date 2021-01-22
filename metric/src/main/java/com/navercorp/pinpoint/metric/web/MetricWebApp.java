package com.navercorp.pinpoint.metric.web;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ImportResource;

//@SpringBootConfiguration
//@EnableAutoConfiguration
@ImportResource({ "classpath:/pinot-web/applicationContext-web-pinot.xml"})
public class MetricWebApp {
}
