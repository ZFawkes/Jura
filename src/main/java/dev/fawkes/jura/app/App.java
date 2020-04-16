package dev.fawkes.jura.app;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class App {

    @Bean
    public AtomicBoolean getAppReady() {
        return new AtomicBoolean(false);
    }


}
