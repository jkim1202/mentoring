package org.example.mentoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MentoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MentoringApplication.class, args);
    }

}
