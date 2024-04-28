package com.viktor44.rxswing3.sources;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;

import org.junit.Test;

import com.viktor44.rxswing3.sources.AbstractButtonSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class AbstractButtonSourceTest {
	
    @Test
    public void testObservingActionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<ActionEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final ActionEvent event = new ActionEvent(this, 1, "command");

                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testAction() {
                        fireActionPerformed(event);
                    }
                }

                TestButton button = new TestButton();
                Disposable sub = AbstractButtonSource.fromActionOf(button).subscribe(action,
                        error, complete);

                verify(action, never()).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();

                button.testAction();
                verify(action, times(1)).accept(any());

                button.testAction();
                verify(action, times(2)).accept(any());

                sub.dispose();
                button.testAction();
                verify(action, times(2)).accept(any());
                verify(error, never()).accept(any());
                verify(complete, never()).run();
            }

        }).awaitTerminal();
    }
}
