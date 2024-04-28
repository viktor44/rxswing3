package com.viktor44.rxswing3.sources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;

import org.junit.Test;

import com.viktor44.rxswing3.sources.WindowEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class WindowEventSourceTest {

    @Test
    public void testObservingWindowEvents() throws Throwable {
        if (GraphicsEnvironment.isHeadless())
            return;
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Throwable {
                JFrame owner = new JFrame();
                Window window = new Window(owner);

                @SuppressWarnings("unchecked")
                Consumer<WindowEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final WindowEvent event = mock(WindowEvent.class);

                Disposable sub = WindowEventSource.fromWindowEventsOf(window)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                fireWindowEvent(window, event);
                verify(action, times(1)).accept(any());

                fireWindowEvent(window, event);
                verify(action, times(2)).accept(any());

                sub.dispose();
                fireWindowEvent(window, event);
                verify(action, times(2)).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }

    private void fireWindowEvent(Window window, WindowEvent event) {
        for (WindowListener listener : window.getWindowListeners()) {
            listener.windowClosed(event);
        }
    }
}

