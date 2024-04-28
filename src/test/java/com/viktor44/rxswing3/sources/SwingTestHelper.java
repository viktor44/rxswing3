package com.viktor44.rxswing3.sources;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Scheduler.Worker;
import io.reactivex.rxjava3.functions.Action;

/* package-private */final class SwingTestHelper { // only for test

    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile Throwable error;

    private SwingTestHelper() {
    }

    public static SwingTestHelper create() {
        return new SwingTestHelper();
    }

    public SwingTestHelper runInEventDispatchThread(final Action action) {
        Worker inner = SwingScheduler.getInstance().createWorker();
        inner.schedule(new Runnable() {

            @Override
            public void run() {
                try {
                    action.run();
                } catch (Throwable e) {
                    error = e;
                }
                latch.countDown();
            }
        });
        return this;
    }

    public void awaitTerminal() throws Throwable {
        latch.await();
        if (error != null) {
            throw error;
        }
    }

    public void awaitTerminal(long timeout, TimeUnit unit) throws Throwable {
        latch.await(timeout, unit);
        if (error != null) {
            throw error;
        }
    }

}
