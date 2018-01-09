package com.mlpinit.models;

import rx.Observable;
import rx.schedulers.SwingScheduler;
import rx.subjects.PublishSubject;

public class MinesweeperTimer {

    private PublishSubject<Integer> elapsedTimeSubject;
    public Observable<Integer> elapsedTimeObservable;

    private Long startTime;
    private boolean timerIsStarted;

    public MinesweeperTimer() {
        elapsedTimeSubject = PublishSubject.create();
        elapsedTimeObservable = elapsedTimeSubject.share()
                .subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
        this.startTime = null;
    }

    public void stopTimer() {
        this.timerIsStarted = false;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        timerIsStarted = true;
        new Thread(() -> {
            while (timerIsStarted) {
                try {
                    Thread.sleep(500);
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    elapsedTimeSubject.onNext((int) (elapsedTime / 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
