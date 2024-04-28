package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class MouseEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromMouseEvents
	 */
	public static Observable<MouseEvent> fromMouseEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<MouseEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<MouseEvent> subscriber) {
				final MouseListener listener = new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void mousePressed(MouseEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void mouseReleased(MouseEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void mouseEntered(MouseEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void mouseExited(MouseEvent event) {
						subscriber.onNext(event);
					}
				};
				component.addMouseListener(listener);

				subscriber.setDisposable(Disposable.fromAction(() -> {

					component.removeMouseListener(listener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromMouseMotionEvents
	 */
	public static Observable<MouseEvent> fromMouseMotionEventsOf(final Component component) {
		return Observable.create(new ObservableOnSubscribe<MouseEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<MouseEvent> subscriber) {
				final MouseMotionListener listener = new MouseMotionListener() {
					@Override
					public void mouseDragged(MouseEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void mouseMoved(MouseEvent event) {
						subscriber.onNext(event);
					}
				};
				component.addMouseMotionListener(listener);

				subscriber.setDisposable(Disposable.fromAction(() -> {

					component.removeMouseMotionListener(listener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	public static Observable<MouseWheelEvent> fromMouseWheelEvents(final Component component) {
		return Observable.create(new ObservableOnSubscribe<MouseWheelEvent>() {
			@Override
			public void subscribe(final ObservableEmitter<MouseWheelEvent> subscriber) {
				final MouseWheelListener listener = new MouseWheelListener() {
					@Override
					public void mouseWheelMoved(MouseWheelEvent event) {
						subscriber.onNext(event);
					}
				};
				component.addMouseWheelListener(listener);

				subscriber.setDisposable(Disposable.fromAction(() -> {

					component.removeMouseWheelListener(listener);

				}));
			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromRelativeMouseMotion
	 */
	public static Observable<Point> fromRelativeMouseMotion(final Component component) {
		final Observable<MouseEvent> events = fromMouseMotionEventsOf(component);
		return Observable.zip(events, events.skip(1), (ev1, ev2) -> new Point(ev2.getX() - ev1.getX(), ev2.getY() - ev1.getY()))
				.subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}

}
