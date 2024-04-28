package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class PropertyChangeEventSource {

    public static Observable<PropertyChangeEvent> fromPropertyChangeEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<PropertyChangeEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<PropertyChangeEvent> subscriber) {
                final PropertyChangeListener listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addPropertyChangeListener(listener);
                subscriber.setDisposable(Disposable.fromAction(() ->  {
                        component.removePropertyChangeListener(listener);
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
