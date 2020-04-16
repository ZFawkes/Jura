package dev.fawkes.jura;

import dev.fawkes.jura.app.AppInit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;

/**
 * main()
 */
@SpringBootApplication
public class JuraApplication {

    private final AppInit appInit;

    public JuraApplication(AppInit appInit) {
        this.appInit = appInit;
    }

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

    @EventListener(ApplicationStartedEvent.class)
    public void run() throws Exception {
        this.appInit.init().ready();
    }
}