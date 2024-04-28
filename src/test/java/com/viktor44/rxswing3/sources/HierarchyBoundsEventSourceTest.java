package com.viktor44.rxswing3.sources;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
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
import com.viktor44.rxswing3.sources.HierarchyEventSource;
import com.viktor44.rxswing3.sources.HierarchyEventSource.Predicates;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

@RunWith(Parameterized.class)
public class HierarchyBoundsEventSourceTest {

    private JPanel rootPanel;
    private JPanel parentPanel;
    private Consumer<HierarchyEvent> action;
    private Consumer<Throwable> error;
    private Action complete;
    private final Function<Component, Observable<HierarchyEvent>> observableFactory;
    private JPanel childPanel;
    
    public HierarchyBoundsEventSourceTest( Function<Component, Observable<HierarchyEvent>> observableFactory ) {
        this.observableFactory = observableFactory;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][]{ { observablefromEventSource() }, 
                                              { observablefromSwingObservable() } });
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        rootPanel = new JPanel();
        
        parentPanel = new JPanel();
        rootPanel.add(parentPanel);
        
        childPanel = new JPanel();
///        childPanel = Mockito.spy(new JPanel());
        parentPanel.add(childPanel);
        
        action = mock(Consumer.class);
        error = mock(Consumer.class);
        complete = mock(Action.class);
    }
    
    @Test
    public void testObservingAnscestorResizedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Throwable {
                Disposable subscription = observableFactory.apply(childPanel)
                                                             .filter(Predicates.ANCESTOR_RESIZED)
                                                             .subscribe(action, error, complete);

                parentPanel.setSize(10, 10);
                parentPanel.setLocation(10, 10); // verifies that ancestor moved events are ignored.

                Mockito.verify(action).accept(Mockito.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();

                // Verifies that the underlying listener has been removed.
                subscription.dispose();
///                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingAnscestorMovedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run() throws Throwable {
                Disposable subscription = observableFactory.apply(childPanel)
                                                             .filter(Predicates.ANCESTOR_MOVED)
                                                             .subscribe(action, error, complete);

                parentPanel.setSize(10, 10); // verifies that ancestor resized events are ignored.
                parentPanel.setLocation(10, 10);

                Mockito.verify(action).accept(Mockito.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();

                // Verifies that the underlying listener has been removed.
                subscription.dispose();
///                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingAllHierarchyBoundsEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {
            @Override
            public void run()  throws Throwable {
                Disposable subscription = observableFactory.apply(childPanel)
                                                             .subscribe(action, error, complete);
                
                InOrder inOrder = inOrder(action);

                parentPanel.setSize(10, 10);
                parentPanel.setLocation(10, 10);

                inOrder.verify(action).accept(Mockito.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
                inOrder.verify(action).accept(Mockito.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
                inOrder.verifyNoMoreInteractions();
                Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).run();

                // Verifies that the underlying listener has been removed.
                subscription.dispose();
///                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }

    private ArgumentMatcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int id, final Container changed, final Container changedParent) {
        return new ArgumentMatcher<HierarchyEvent>() {
            @Override
            public boolean matches(HierarchyEvent event) {
                if (source != event.getComponent())
                    return false;

                if (changed != event.getChanged())
                    return false;

                if (changedParent != event.getChangedParent())
                    return false;

                return id == event.getID();
            }
        };
    }
    
    private static Function<Component, Observable<HierarchyEvent>> observablefromEventSource()
    {
        return new Function<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> apply(Component component) {
                return HierarchyEventSource.fromHierarchyBoundsEventsOf(component);
            }
        };
    }
    
    private static Function<Component, Observable<HierarchyEvent>> observablefromSwingObservable()
    {
        return new Function<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> apply(Component component) {
                return SwingObservable.fromHierachyBoundsEvents(component);
            }
        };
    }
}
