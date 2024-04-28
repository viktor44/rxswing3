package com.viktor44.rxswing3.sources;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPanel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.viktor44.rxswing3.SwingObservable;
import com.viktor44.rxswing3.sources.ContainerEventSource;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

@RunWith(Parameterized.class)
public class ContainerEventSourceTest {
    
    private final Function<Container, Observable<ContainerEvent>> observableFactory;
    
    private JPanel panel;
    private Consumer<ContainerEvent> action;
    private Consumer<Throwable> error;
    private Action complete;

    public ContainerEventSourceTest(Function<Container, Observable<ContainerEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{ { observableFromContainerEventSource() },
                                             { observableFromSwingObservable() } });
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        panel = new JPanel();
///        panel = Mockito.spy(new JPanel());
        action = Mockito.mock(Consumer.class);
        error = Mockito.mock(Consumer.class);
        complete = Mockito.mock(Action.class);
    }
    
    @Test
    public void testObservingContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Throwable {
                Disposable subscription = observableFactory.apply(panel)
                                                             .subscribe(action, error, complete);
                
                JPanel child = new JPanel();
                panel.add(child);
                panel.removeAll();
                
                InOrder inOrder = Mockito.inOrder(action);
                
                inOrder.verify(action).accept(Mockito.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
                inOrder.verify(action).accept(Mockito.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_REMOVED)));
                inOrder.verifyNoMoreInteractions();
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();
                
                // Verifies that the underlying listener has been removed.
                subscription.dispose();
///                Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
                Assert.assertEquals(0, panel.getHierarchyListeners().length);
                
                // Verifies that after unsubscribing events are not emitted.
                panel.add(child);
                Mockito.verifyNoMoreInteractions(action, error, complete);
           }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingFilteredContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Throwable{
                Disposable subscription = observableFactory.apply(panel)
                                                             .filter(com.viktor44.rxswing3.sources.ContainerEventSource.Predicates.COMPONENT_ADDED)
                                                             .subscribe(action, error, complete);
                
                JPanel child = new JPanel();
                panel.add(child);
                panel.remove(child); // sanity check to verify that the filtering works.
                
                Mockito.verify(action).accept(Mockito.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();
                
                // Verifies that the underlying listener has been removed.
                subscription.dispose();
///                Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
                Assert.assertEquals(0, panel.getHierarchyListeners().length);
                
                // Verifies that after unsubscribing events are not emitted.
                panel.add(child);
                Mockito.verifyNoMoreInteractions(action, error, complete);
           }
        }).awaitTerminal();
    }

    private static ArgumentMatcher<ContainerEvent> containerEventMatcher(final Container container, final Component child, final int id) {
        return new ArgumentMatcher<ContainerEvent>() {
            @Override
            public boolean matches(ContainerEvent event) {
                if (container != event.getContainer())
                    return false;
                
                if (container != event.getSource())
                    return false;
                
                if (child != event.getChild())
                    return false;
                
                return event.getID() == id;
            }
        };
    }

    private static Function<Container, Observable<ContainerEvent>> observableFromContainerEventSource()
    {
        return new Function<Container, Observable<ContainerEvent>>(){
            @Override
            public Observable<ContainerEvent> apply(Container container) {
                return ContainerEventSource.fromContainerEventsOf(container);
            }
        }; 
    }
    
    private static Function<Container, Observable<ContainerEvent>> observableFromSwingObservable()
    {
        return new Function<Container, Observable<ContainerEvent>>(){
            @Override
            public Observable<ContainerEvent> apply(Container container) {
                return SwingObservable.fromContainerEvents(container);
            }
        }; 
    }
}
