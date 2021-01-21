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

import com.navercorp.pinpoint.common.server.metric.model.Tag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hyunjoon Cho
 */
public class SystemMetricUtils {

    public static List<Tag> parseTags(List<String> tagStringList) {
        List<Tag> tagList = new ArrayList<>();
        for (String tagString : tagStringList) {
            tagList.add(parseTag(tagString));
        }
        return tagList;
    }

    public static List<Tag> parseTags(String tagStrings) {
        List<Tag> tagList = new ArrayList<>();

        String[] tagStrArray = parseMultiValueFieldList(tagStrings);
        for (String tagString : tagStrArray) {
            tagList.add(parseTag(tagString));
        }

        return tagList;
    }

    public static Tag parseTag(String tagString) {
        String[] tag = tagString.split(":");
        return new Tag(tag[0], tag[1]);
    }

    private static String[] parseMultiValueFieldList(String string) {
        return string.substring(1, string.length() - 1).replace("\"", "").split(",");
    }
}
