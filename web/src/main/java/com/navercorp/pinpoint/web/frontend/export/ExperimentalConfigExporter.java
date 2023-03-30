package com.navercorp.pinpoint.web.frontend.export;

import com.navercorp.pinpoint.web.frontend.config.ExperimentalConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnBean(ExperimentalConfig.class)
public class ExperimentalConfigExporter implements FrontendConfigExporter {

    private final ExperimentalConfig experimentalConfig;

    public ExperimentalConfigExporter(ExperimentalConfig experimentalConfig) {
        this.experimentalConfig = Objects.requireNonNull(experimentalConfig, "experimentalConfig");
    }

    @Override
    public void export(Map<String, Object> export) {
        export.putAll(experimentalConfig.getProperties());
    }
}
