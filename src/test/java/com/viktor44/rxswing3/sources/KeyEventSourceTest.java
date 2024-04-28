package com.viktor44.rxswing3.sources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JPanel;

import org.junit.Test;
import org.mockito.InOrder;

import com.viktor44.rxswing3.sources.KeyEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class KeyEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testObservingKeyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run()  throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<KeyEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final KeyEvent event = mock(KeyEvent.class);

                Disposable sub = KeyEventSource.fromKeyEventsOf(comp)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                fireKeyEvent(event);
                verify(action, times(1)).accept(any());

                fireKeyEvent(event);
                verify(action, times(2)).accept(any());

                sub.dispose();
                fireKeyEvent(event);
                verify(action, times(2)).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    @Test
    public void testObservingPressedKeys() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<Set<Integer>> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                Disposable sub = KeyEventSource.currentlyPressedKeysOf(comp)
                        .subscribe(action, error, complete);

                InOrder inOrder = inOrder(action);
                inOrder.verify(action).accept(Collections.emptySet());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
                inOrder.verify(action, times(1)).accept(new HashSet<>(Arrays.asList(1)));
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                fireKeyEvent(keyEvent(2, KeyEvent.KEY_PRESSED));
                fireKeyEvent(keyEvent(KeyEvent.VK_UNDEFINED, KeyEvent.KEY_TYPED));
                inOrder.verify(action, times(1)).accept(new HashSet<>(Arrays.asList(1, 2)));

                fireKeyEvent(keyEvent(2, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(new HashSet<>(Arrays.asList(1)));

                fireKeyEvent(keyEvent(3, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(new HashSet<>(Arrays.asList(1)));

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_RELEASED));
                inOrder.verify(action, times(1)).accept(Collections.emptySet());

                sub.dispose();

                fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
                inOrder.verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    private KeyEvent keyEvent(int keyCode, int id) {
        return new KeyEvent(comp, id, -1L, 0, keyCode, ' ');
    }

    private void fireKeyEvent(KeyEvent event) {
        for (KeyListener listener : comp.getKeyListeners()) {
            listener.keyTyped(event);
        }
    }
}
