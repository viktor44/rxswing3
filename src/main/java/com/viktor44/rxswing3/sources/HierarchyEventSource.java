package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Predicate;

public class HierarchyEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromHierachyEvents
	 */
	public static Observable<HierarchyEvent> fromHierarchyEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<HierarchyEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<HierarchyEvent> subscriber) {
				final HierarchyListener hiearchyListener = new HierarchyListener() {
					@Override
					public void hierarchyChanged(HierarchyEvent e) {
						subscriber.onNext(e);
					}
				};
				component.addHierarchyListener(hiearchyListener);
				subscriber.setDisposable(Disposable.fromAction(() -> {

					component.removeHierarchyListener(hiearchyListener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromHierachyBoundsEvents
	 */
	public static Observable<HierarchyEvent> fromHierarchyBoundsEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<HierarchyEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<HierarchyEvent> subscriber) {
				final HierarchyBoundsListener hiearchyBoundsListener = new HierarchyBoundsListener() {
					@Override
					public void ancestorMoved(HierarchyEvent e) {
						subscriber.onNext(e);
					}

					@Override
					public void ancestorResized(HierarchyEvent e) {
						subscriber.onNext(e);
					}
				};
				component.addHierarchyBoundsListener(hiearchyBoundsListener);
				subscriber.setDisposable(Disposable.fromAction(() -> {

					component.removeHierarchyBoundsListener(hiearchyBoundsListener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	public static enum Predicates implements Predicate<HierarchyEvent> {
		ANCESTOR_RESIZED(HierarchyEvent.ANCESTOR_RESIZED), ANCESTOR_MOVED(HierarchyEvent.ANCESTOR_MOVED);

		private final int id;

		private Predicates(int id) {
			this.id = id;
		}

		@Override
		public boolean test(HierarchyEvent event) {
			return event.getID() == id;
		}
	}
}
