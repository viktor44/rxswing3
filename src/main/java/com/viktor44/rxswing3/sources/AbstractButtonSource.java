package com.viktor44.rxswing3.sources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;

public class AbstractButtonSource {

    /**
     * @see com.viktor44.rxswing3.SwingObservable#fromButtonAction
     */
    public static Observable<ActionEvent> fromActionOf(final AbstractButton button) {
    	
        return Observable.create(new ObservableOnSubscribe<ActionEvent>() {
          

			@Override
			public void subscribe(final ObservableEmitter<ActionEvent> subscriber) throws Exception {
				 final ActionListener listener = new ActionListener() {
	                    @Override
	                    public void actionPerformed(ActionEvent e) {
	                        subscriber.onNext(e);
	                    }
	                };
	                button.addActionListener(listener);
	                subscriber.setDisposable(Disposable.fromAction(() -> {
	                	button.removeActionListener(listener);
	                }));
	                
	               
				
			}
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
