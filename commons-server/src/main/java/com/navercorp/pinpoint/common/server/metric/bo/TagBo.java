/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.metric.bo;

import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
public class TagBo {
    private String tagName;
    private String tagValue;
    public TagBo(String tagName, String tagValue) {
        this.tagName = Objects.requireNonNull(tagName);
        this.tagValue = Objects.requireNonNull(tagValue);
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getTagValue() {
        return tagValue;
    }

    public void setTagValue(String tagValue) {
        this.tagValue = tagValue;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(tagName);
        sb.append(":").append(tagValue);
        return sb.toString();
        // for easier row key decoding
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TagBo)) return false;

        TagBo tagBo = (TagBo) o;

        return tagName.equals(tagBo.getTagName()) && tagValue.equals(tagBo.getTagValue());
    }

    @Override
    public int hashCode() {
        int result = tagName != null ? tagName.hashCode() : 0;
        result = 31 * result + (tagValue != null ? tagValue.hashCode() : 0);
        return result;
    }
}
