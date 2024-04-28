package com.viktor44.rxswing3.sources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.awt.Component;
import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.viktor44.rxswing3.SwingObservable;
import com.viktor44.rxswing3.sources.PropertyChangeEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class PropertyChangeEventSourceTest {
	@Test
	public void testObservingPropertyEvents() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action() {

			@Override
			public void run() throws Throwable {
				@SuppressWarnings("unchecked")
				Consumer<PropertyChangeEvent> action = mock(Consumer.class);
				@SuppressWarnings("unchecked")
				Consumer<Throwable> error = mock(Consumer.class);
				Action complete = mock(Action.class);

				Component component = new JPanel();

				Disposable subscription = PropertyChangeEventSource.fromPropertyChangeEventsOf(component).subscribe(action, error, complete);

				verify(action, never()).accept(Mockito.any());
				verify(error, never()).accept(Mockito.any());
				verify(complete, never()).run();

				component.setEnabled(false);
				verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
				verifyNoMoreInteractions(action, error, complete);

				// check that an event is only fired if the value really changes
				component.setEnabled(false);
				verifyNoMoreInteractions(action, error, complete);

				component.setEnabled(true);
				verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
				verifyNoMoreInteractions(action, error, complete);

				// check some arbitrary property
				component.firePropertyChange("width", 200, 300);
				verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("width", 200l, 300l)));
				verifyNoMoreInteractions(action, error, complete);

				// verify no events sent after unsubscribing
				subscription.dispose();
				component.setEnabled(false);
				verifyNoMoreInteractions(action, error, complete);
			}

		}).awaitTerminal();
	}

	@Test
	public void testObservingFilteredPropertyEvents() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action() {

			@Override
			public void run() throws Throwable {
				@SuppressWarnings("unchecked")
				Consumer<PropertyChangeEvent> action = mock(Consumer.class);
				@SuppressWarnings("unchecked")
				Consumer<Throwable> error = mock(Consumer.class);
				Action complete = mock(Action.class);

				Component component = new JPanel();

				Disposable subscription = SwingObservable.fromPropertyChangeEvents(component, "enabled").subscribe(action, error, complete);

				verify(action, never()).accept(Mockito.any());
				verify(error, never()).accept(Mockito.any());
				verify(complete, never()).run();

				// trigger a bunch of property change events and verify that only the enbled ones are observed
				component.setEnabled(false);
				component.setEnabled(false);
				component.setEnabled(true);
				component.firePropertyChange("width", 200, 300);
				component.firePropertyChange("height", 400, 200);
				component.firePropertyChange("depth", 100, 300);
				verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
				verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
				verifyNoMoreInteractions(action, error, complete);

				subscription.dispose();
			}

		}).awaitTerminal();
	}

	private static ArgumentMatcher<PropertyChangeEvent> propertyChangeEventMatcher(final String propertyName, final Object oldValue, final Object newValue) {
		return new ArgumentMatcher<PropertyChangeEvent>() {
			@Override
			public boolean matches(PropertyChangeEvent pcEvent) {
				if (!propertyName.equals(pcEvent.getPropertyName())) {
					return false;
				}

				if (!oldValue.equals(pcEvent.getOldValue())) {
					return false;
				}

				return newValue.equals(pcEvent.getNewValue());
			}
		};
	}
}
