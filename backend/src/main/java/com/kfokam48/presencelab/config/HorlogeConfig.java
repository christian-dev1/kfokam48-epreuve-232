package com.kfokam48.presencelab.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Horloge injectable : les règles dépendant du temps (RG1 : expiration à 15 min)
 * se testent en fixant l'heure, sans attendre.
 */
@Configuration
public class HorlogeConfig {

    @Bean
    public Clock horloge() {
        return Clock.systemUTC();
    }
}
