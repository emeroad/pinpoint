/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.spring.webflux;

import com.navercorp.pinpoint.bootstrap.config.DumpType;
import com.navercorp.pinpoint.bootstrap.config.HttpDumpConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class SpringWebFluxPluginConfig {
    private final boolean enable;
    private final boolean param;
    private final HttpDumpConfig httpDumpConfig;
    private final boolean clientEnable;

    private final boolean uriStatEnable;
    private final boolean uriStatUseUserInput;
    private final boolean uriStatCollectMethod;
    public SpringWebFluxPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        this.enable = config.readBoolean("profiler.spring.webflux.enable", true);

        // Client
        this.clientEnable = config.readBoolean("profiler.spring.webflux.client.enable", false);
        this.param = config.readBoolean("profiler.spring.webflux.client.param", true);
        boolean cookie = config.readBoolean("profiler.spring.webflux.client.cookie", false);
        DumpType cookieDumpType = DumpType.of(config.readString("profiler.spring.webflux.client.cookie.dumptype"));
        int cookieSamplingRate = config.readInt("profiler.spring.webflux.client.cookie.sampling.rate", 1);
        int cookieDumpSize = config.readInt("profiler.spring.webflux.client.cookie.dumpsize", 1024);
        this.httpDumpConfig = HttpDumpConfig.get(cookie, cookieDumpType, cookieSamplingRate, cookieDumpSize, false, cookieDumpType, 1, 1024);
        this.uriStatEnable = config.readBoolean("profiler.uri.stat.spring.webflux.enable", false);
        this.uriStatUseUserInput = config.readBoolean("profiler.uri.stat.spring.webflux.useuserinput", false);
        this.uriStatCollectMethod = config.readBoolean("profiler.uri.stat.collect.http.method", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isParam() {
        return param;
    }

    public HttpDumpConfig getHttpDumpConfig() {
        return httpDumpConfig;
    }

    public boolean isClientEnable() {
        return clientEnable;
    }

    public boolean isUriStatEnable() {
        return uriStatEnable;
    }

    public boolean isUriStatCollectMethod() {
        return uriStatCollectMethod;
    }

    public boolean isUriStatUseUserInput() {
        return uriStatUseUserInput;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpringWebFluxPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", param=").append(param);
        sb.append(", httpDumpConfig=").append(httpDumpConfig);
        sb.append(", uriStatEnable=").append(uriStatEnable);
        sb.append(", uriStatUseUserInput=").append(uriStatUseUserInput);
        sb.append('}');
        return sb.toString();
    }
}
