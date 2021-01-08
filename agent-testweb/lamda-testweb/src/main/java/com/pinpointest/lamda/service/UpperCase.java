package com.pinpointest.lamda.service;

import java.util.function.Function;

public class UpperCase implements Function<String, String> {
    @Override
    public String apply(String o) {
        return o.toUpperCase();
    }
}
