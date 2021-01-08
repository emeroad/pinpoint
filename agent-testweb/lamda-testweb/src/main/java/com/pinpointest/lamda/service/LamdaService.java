package com.pinpointest.lamda.service;

import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class LamdaService {

    public Function<String, String> upperCase() {
        UpperCase upperCase = new UpperCase();
        return s -> upperCase.apply(s);
    }

    public String apply(Function<String, String> function, String s) {
        return function.apply(s);
    }

}
