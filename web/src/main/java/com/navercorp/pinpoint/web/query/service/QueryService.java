package com.navercorp.pinpoint.web.query.service;

import com.navercorp.pinpoint.web.query.BindType;

public interface QueryService {
    BindType getBindType();

    String bind(String metaData, String bind);
}
