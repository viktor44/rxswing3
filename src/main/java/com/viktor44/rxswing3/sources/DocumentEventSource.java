package com.viktor44.rxswing3.sources;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class DocumentEventSource {

	/**
	 * @see com.viktor44.rxswing3.SwingObservable#fromDocumentEvents(Document)
	 */
	public static Observable<DocumentEvent> fromDocumentEventsOf(final Document document) {
		return Observable.create(new ObservableOnSubscribe<DocumentEvent>() {
			@Override
			public void subscribe(ObservableEmitter<DocumentEvent> subscriber) throws Exception {

				final DocumentListener listener = new DocumentListener() {
					@Override
					public void insertUpdate(DocumentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void removeUpdate(DocumentEvent event) {
						subscriber.onNext(event);
					}

					@Override
					public void changedUpdate(DocumentEvent event) {
						subscriber.onNext(event);
					}
				};
				document.addDocumentListener(listener);
				subscriber.setDisposable(Disposable.fromAction(() -> {
					document.removeDocumentListener(listener);
				}));

			}

		}).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
	}
}
