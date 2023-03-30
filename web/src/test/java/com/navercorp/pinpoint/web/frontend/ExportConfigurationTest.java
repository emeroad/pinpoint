package com.navercorp.pinpoint.web.frontend;

import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.frontend.export.ConfigPropertiesExporter;
import com.navercorp.pinpoint.web.frontend.export.FrontendConfigExporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ContextConfiguration(classes = {
        ConfigProperties.class,
        ExportConfiguration.class,
})
@ExtendWith(SpringExtension.class)
class ExportConfigurationTest {

    @Autowired
    List<FrontendConfigExporter> exporters;

    @Test
    void lookup_empty() {
        Assertions.assertEquals(1, exporters.size());

        FrontendConfigExporter exporter = exporters.get(0);

        Assertions.assertSame(ConfigPropertiesExporter.class.getName(), exporter.getClass().getName());

    }
}

