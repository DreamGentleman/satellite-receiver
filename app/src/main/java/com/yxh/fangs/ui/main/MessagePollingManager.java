package com.yxh.fangs.ui.main;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessagePollingManager {

    private final MessageDispatcher dispatcher;
    private Disposable pollingDisposable;

    public MessagePollingManager(MessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void start() {
        pollingDisposable = Observable
                .interval(10, 30, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribe(tick -> dispatcher.fetchAndHandleMessages(false),
                        throwable -> { /* ignore */ });
    }

    public void stop() {
        if (pollingDisposable != null) {
            pollingDisposable.dispose();
        }
    }
}
