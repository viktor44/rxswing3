package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.viktor44.rxswing3.SwingObservable;
import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;

public class ComponentEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromComponentEvents
	 */
	public static Observable<ComponentEvent> fromComponentEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<ComponentEvent>() {

			@Override
			public void subscribe(ObservableEmitter<ComponentEvent> subscriber) throws Exception {
				final ComponentListener listener = new ComponentListener() {
					@Override
					public void componentHidden(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentMoved(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentResized(ComponentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void componentShown(ComponentEvent event) {
						subscriber.onNext(event);
					}
				};
				component.addComponentListener(listener);
				subscriber.setDisposable(Disposable.fromAction(() -> {
					component.removeComponentListener(listener);
				}));

			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see SwingObservable#fromResizing
	 */
	public static Observable<Dimension> fromResizing(final Component component) {
		return fromComponentEventsOf(component).filter(Predicates.RESIZED).map(event -> event.getComponent().getSize());
	}

	/**
	 * Predicates that help with filtering observables for specific component events.
	 */
	public enum Predicates implements Predicate<java.awt.event.ComponentEvent> {
		RESIZED(ComponentEvent.COMPONENT_RESIZED), HIDDEN(ComponentEvent.COMPONENT_HIDDEN), MOVED(ComponentEvent.COMPONENT_MOVED), SHOWN(
				ComponentEvent.COMPONENT_SHOWN);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}


		@Override
		public boolean test(ComponentEvent event) throws Exception {
			return event.getID() == id;
		}
	}
}
