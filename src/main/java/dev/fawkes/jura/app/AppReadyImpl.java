package dev.fawkes.jura.app;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.fawkes.jura.dev.DevReadyTask;

import org.springframework.stereotype.Component;

@Component
class AppReadyImpl implements AppReady {

    private final DevReadyTask devReadyTask;
    private final AtomicBoolean appReady;

    AppReadyImpl(DevReadyTask devReadyTask, AtomicBoolean appReady) {
        this.appReady = appReady;
        this.devReadyTask = devReadyTask;
    }

    public void ready() {
        this.appReady.set(true);
        this.devReadyTask.doTask();
    }
}
