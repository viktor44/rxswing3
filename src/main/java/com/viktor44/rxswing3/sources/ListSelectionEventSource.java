package com.viktor44.rxswing3.sources;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class ListSelectionEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromListSelectionEvents(ListSelectionModel)
	 */
	public static Observable<ListSelectionEvent> fromListSelectionEventsOf(final ListSelectionModel listSelectionModel) {
		return Observable.create(new ObservableOnSubscribe<ListSelectionEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<ListSelectionEvent> subscriber) {
				final ListSelectionListener listener = new ListSelectionListener() {
					@Override
					public void valueChanged(final ListSelectionEvent event) {
						subscriber.onNext(event);
					}

				};
				listSelectionModel.addListSelectionListener(listener);
				subscriber.setDisposable(Disposable.fromAction(() ->  {
				
						listSelectionModel.removeListSelectionListener(listener);
				
				}));
			}
		}).subscribeOn(SwingScheduler.getInstance())
				.unsubscribeOn(SwingScheduler.getInstance());
	}
}
