package com.viktor44.rxswing3.sources;

import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;

public class ContainerEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromContainerEvents
	 */
	public static Observable<ContainerEvent> fromContainerEventsOf(final Container container) {
		return Observable.create(new ObservableOnSubscribe<ContainerEvent>() {

			@Override
			public void subscribe(ObservableEmitter<ContainerEvent> subscriber) throws Exception {
				final ContainerListener listener = new ContainerListener() {
					@Override
					public void componentRemoved(ContainerEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentAdded(ContainerEvent event) {
						subscriber.onNext(event);
					}
				};
				container.addContainerListener(listener);
				subscriber.setDisposable(Disposable.fromAction(() -> {

					container.removeContainerListener(listener);

				}));

			}
		}).subscribeOn(SwingScheduler.getInstance()).observeOn(SwingScheduler.getInstance());
	}

	public static enum Predicates implements Predicate<ContainerEvent> {
		COMPONENT_ADDED(ContainerEvent.COMPONENT_ADDED), COMPONENT_REMOVED(ContainerEvent.COMPONENT_REMOVED);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}

		@Override
		public boolean test(ContainerEvent event) {
			return event.getID() == id;
		}
	}
}
