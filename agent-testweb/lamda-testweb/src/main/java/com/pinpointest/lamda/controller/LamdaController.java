package com.pinpointest.lamda.controller;

import com.pinpointest.lamda.service.LamdaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Function;

@RestController
public class LamdaController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LamdaService lamdaService;

    @GetMapping(value = "/lamda/test1")
    public String test1() {
        Function<String, String> f = lamdaService.upperCase();
        return f.apply("hello world");
    }

    @Autowired
    private Function<String, String> uppercase;

    @GetMapping(value = "/lamda/test2")
    public String test2() {
        return uppercase.apply("hello world");
    }


    @GetMapping(value = "/lamda/test3")
    public String test3() {
        return lamdaService.apply(uppercase, "hello world");
    }

}
