package dev.fawkes.jura;

import dev.fawkes.jura.app.AppInit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

/**
 * main()
 */
@SpringBootApplication
public class JuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(JuraApplication.class, args);
        // Keep app alive.
        new Thread("Keep Alive") {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
            }
        }.start();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() throws Exception {
        new AppInit().init().start();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}