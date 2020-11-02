/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.web.metric.util;

import com.navercorp.pinpoint.common.server.metric.bo.TagBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricUtils {

    public static List<TagBo> parseTagBos(List<String> tags) {
        List<TagBo> tagBoList = new ArrayList<>();
        for (String tag : tags) {
            String[] tagSplit = tag.split(":");
            tagBoList.add(new TagBo(tagSplit[0], tagSplit[1]));
        }
        return tagBoList;
    }

    public static List<TagBo> parseTagBos(String tagNames, String tagValues) {
        List<TagBo> tagBoList = new ArrayList<>();

        String[] tagName = parseMultiValueFieldList(tagNames);
        String[] tagValue = parseMultiValueFieldList(tagValues);

        for (int j = 0; j < tagName.length; j++) {
            tagBoList.add(new TagBo(tagName[j], tagValue[j]));
        }

        return tagBoList;
    }

    public static String[] parseMultiValueFieldList(String string) {
        return string.substring(1, string.length() - 1).replace("\"", "").split(",");
    }
}
