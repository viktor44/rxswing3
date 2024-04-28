package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.viktor44.rxswing3.SwingScheduler;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.BiFunction;

public class KeyEventSource {

    /**
     * @see com.viktor44.rxswing3.SwingObservable#fromKeyEvents(Component)
     */
    public static Observable<KeyEvent> fromKeyEventsOf(final Component component) {
        return Observable.create(new ObservableOnSubscribe<KeyEvent>() {
            @Override
            public void subscribe(final ObservableEmitter<KeyEvent> subscriber) {
                final KeyListener listener = new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void keyReleased(KeyEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void keyTyped(KeyEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addKeyListener(listener);

                subscriber.setDisposable(Disposable.fromAction(() ->  {
                  
                        component.removeKeyListener(listener);
                    
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see com.viktor44.rxswing3.SwingObservable#fromPressedKeys(Component)
     */
    public static Observable<Set<Integer>> currentlyPressedKeysOf(Component component) {
        class CollectKeys implements BiFunction<Set<Integer>, KeyEvent, Set<Integer>>{
           

			@Override
			public Set<Integer> apply(Set<Integer> t1, KeyEvent event) throws Exception {
				  Set<Integer> afterEvent = new HashSet<Integer>(t1);
	                switch (event.getID()) {
	                    case KeyEvent.KEY_PRESSED:
	                        afterEvent.add(event.getKeyCode());
	                        break;
	                        
	                    case KeyEvent.KEY_RELEASED:
	                        afterEvent.remove(event.getKeyCode());
	                        break;
	                      
	                    default: // nothing to do
	                }
	                return afterEvent;
			}
        }
        
        Observable<KeyEvent> filteredKeyEvents = fromKeyEventsOf(component).filter(event -> event.getID() == KeyEvent.KEY_PRESSED || event.getID() == KeyEvent.KEY_RELEASED);
        
        return filteredKeyEvents.scan(Collections.<Integer>emptySet(), new CollectKeys());
    }
    
}
