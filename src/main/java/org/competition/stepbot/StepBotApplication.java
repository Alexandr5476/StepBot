package org.competition.stepbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StepBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(StepBotApplication.class, args);
    }

}
