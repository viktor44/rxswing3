package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;

public class FocusEventSource {

    /**
     * @see com.viktor44.rxswing3.SwingObservable#fromFocusEvents
     */
    public static Observable<FocusEvent> fromFocusEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<FocusEvent>() {
            @Override
            public void subscribe(ObservableEmitter<FocusEvent> subscriber) {
                final FocusListener listener = new FocusListener() {

                    @Override
                    public void focusGained(FocusEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void focusLost(FocusEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addFocusListener(listener);
                subscriber.setDisposable(Disposable.fromAction(() ->  {
                   
                        component.removeFocusListener(listener);
                    
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * Predicates that help with filtering observables for specific focus events.
     */
    public enum Predicates implements Predicate<FocusEvent> {
        FOCUS_GAINED(FocusEvent.FOCUS_GAINED),
        FOCUS_LOST(FocusEvent.FOCUS_LOST);

        private final int id;

        private Predicates(int id) {
            this.id = id;
        }

        @Override
        public boolean test(FocusEvent event) {
            return event.getID() == id;
        }
    }
}
