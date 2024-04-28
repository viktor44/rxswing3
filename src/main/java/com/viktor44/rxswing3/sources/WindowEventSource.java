package com.viktor44.rxswing3.sources;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class WindowEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromWindowEventsOf(Window)
	 */
	public static Observable<WindowEvent> fromWindowEventsOf(final Window window) {
		return Observable.create(new ObservableOnSubscribe<WindowEvent>() {

			@Override
			public void subscribe(ObservableEmitter<WindowEvent> subscriber) throws Exception {
				final WindowListener windowListener = new WindowListener() {
					@Override
					public void windowOpened(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowClosing(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowClosed(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowIconified(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowDeiconified(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowActivated(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}

					@Override
					public void windowDeactivated(WindowEvent windowEvent) {
						subscriber.onNext(windowEvent);
					}
				};

				window.addWindowListener(windowListener);

				subscriber.setDisposable(Disposable.fromAction(() -> {

					window.removeWindowListener(windowListener);

				}));

			}
		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}
}
