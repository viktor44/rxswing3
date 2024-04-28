package com.viktor44.rxswing3.sources;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPanel;

import org.junit.Test;

import com.viktor44.rxswing3.sources.FocusEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class FocusEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testObservingFocusEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                
            	@SuppressWarnings("unchecked")
                Consumer<FocusEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final FocusEvent event = mock(FocusEvent.class);

                Disposable sub = FocusEventSource.fromFocusEventsOf(comp)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                fireFocusEvent(event);
                verify(action, times(1)).accept(any());

                fireFocusEvent(event);
                verify(action, times(2)).accept(any());

                sub.dispose();
                fireFocusEvent(event);
                verify(action, times(2)).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    private void fireFocusEvent(FocusEvent event) {
        for (FocusListener listener : comp.getFocusListeners()) {
            listener.focusGained(event);
        }
    }
}
