package com.viktor44.rxswing3.sources;



import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import org.junit.Assert;
import org.junit.Test;

import com.viktor44.rxswing3.sources.ListSelectionEventSource;

import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.observers.TestObserver;

public class ListSelectionEventSourceTest {

    @Test
    public void jtableRowSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception {
            	TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

                JTable table = createJTable();
                ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                table.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(0));

                table.getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(1));
            }
        }).awaitTerminal();
    }

    @Test
    public void jtableColumnSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() {
                TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

                JTable table = createJTable();
                table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

                ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getColumnModel().getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getColumnModel().getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(0));

                table.getColumnModel().getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getColumnModel().getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(1));

            }
        }).awaitTerminal();
    }

    @Test
    public void jlistSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception {
                TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

                JList<String> jList = new JList<String>(new String[]{"a", "b", "c", "d", "e", "f"});
                jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                ListSelectionEventSource
                        .fromListSelectionEventsOf(jList.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                jList.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                jList.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(0));

                jList.getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                jList.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.values().get(1));
            }
        }).awaitTerminal();
    }

    @Test
    public void jtableRowSelectionUnsubscribeRemovesRowSelectionListener() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Exception{
                TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

                JTable table = createJTable();
                int numberOfListenersBefore = getNumberOfRowListSelectionListeners(table);

                ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                testSubscriber.dispose();

                Assert.assertTrue(testSubscriber.isDisposed());

                table.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                Assert.assertEquals(numberOfListenersBefore, getNumberOfRowListSelectionListeners(table));
            }
        }).awaitTerminal();
    }

    private static int getNumberOfRowListSelectionListeners(final JTable table) {
        return ((DefaultListSelectionModel) table.getSelectionModel()).getListSelectionListeners().length;
    }

    private static JTable createJTable() {
        return new JTable(new Object[][]{
                {"A1", "B1", "C1"},
                {"A2", "B2", "C2"},
                {"A3", "B3", "C3"},
        },
                new String[]{
                        "A", "B", "C"
                });
    }

    private static void assertListSelectionEventEquals(ListSelectionEvent expected, ListSelectionEvent actual) {
        if (expected == null) {
            throw new IllegalArgumentException("missing expected");
        }

        if (actual == null) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual);
        }
        if (!expected.getSource().equals(actual.getSource())) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different source.");
        }
        if (expected.getFirstIndex() != actual.getFirstIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different first index.");
        }
        if (expected.getLastIndex() != actual.getLastIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different last index.");
        }
        if (expected.getValueIsAdjusting() != actual.getValueIsAdjusting()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different ValueIsAdjusting.");
        }
    }
}