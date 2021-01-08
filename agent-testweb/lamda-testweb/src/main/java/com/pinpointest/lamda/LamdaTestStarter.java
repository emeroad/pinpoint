package com.pinpointest.lamda;

import com.pinpointest.lamda.service.UpperCase;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Function;

@SpringBootApplication
public class LamdaTestStarter {

    public static void main(String[] args) {
        SpringApplication.run(LamdaTestStarter.class, args);
    }

    @Bean
    public Function<String, String> test1() {
        UpperCase upperCase = new UpperCase();
        return s -> upperCase.apply(s);
    }
}
