package com.viktor44.rxswing3.sources;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class ItemEventSource {

	public static Observable<ItemEvent> fromItemEventsOf(final ItemSelectable itemSelectable) {
		return Observable.create(new ObservableOnSubscribe<ItemEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<ItemEvent> subscriber) {
				final ItemListener listener = new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent event) {
						subscriber.onNext(event);
					}
				};
				itemSelectable.addItemListener(listener);
				subscriber.setDisposable(Disposable.fromAction(() -> {
					itemSelectable.removeItemListener(listener);
				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}
}
