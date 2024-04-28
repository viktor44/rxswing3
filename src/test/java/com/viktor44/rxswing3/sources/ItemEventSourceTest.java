package com.viktor44.rxswing3.sources;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.ItemEvent;

import javax.swing.AbstractButton;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.viktor44.rxswing3.SwingObservable;
import com.viktor44.rxswing3.sources.ItemEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class ItemEventSourceTest
{
    @Test
    public void testObservingItemEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = ItemEventSource.fromItemEventsOf(button).subscribe(action,
                        error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, times(1)).accept(argThat(itemEventMatcher(SELECTED)));

                button.testSelection();
                verify(action, times(2)).accept(argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).accept(argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, times(2)).accept(argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).accept(argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredBySelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = SwingObservable.fromItemSelectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, never()).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, times(1)).accept(argThat(itemEventMatcher(SELECTED)));
                verify(action, never()).accept(argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredByDeSelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<ItemEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = SwingObservable.fromItemDeselectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                button.testSelection();
                verify(action, never()).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));


                sub.dispose();
                button.testSelection();
                verify(action, never()).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }
        }).awaitTerminal();
    }
    
    private ArgumentMatcher<ItemEvent> itemEventMatcher(final int eventType)
    {
        return new ArgumentMatcher<ItemEvent>() {
            @Override
            public boolean matches(ItemEvent event) {
                return event.getStateChange() == eventType;
            }
        };
    }
}
