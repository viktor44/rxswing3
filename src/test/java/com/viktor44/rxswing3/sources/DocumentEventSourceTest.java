package com.viktor44.rxswing3.sources;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;

import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.viktor44.rxswing3.SwingObservable;
import com.viktor44.rxswing3.sources.DocumentEventSource;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;

public class DocumentEventSourceTest {

    @Test
    public void testObservingDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<DocumentEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final JEditorPane pane = new JEditorPane();
                // Document must by StyledDocument to test changeUpdate
                pane.setContentType("text/html");
                final Document doc = (HTMLDocument) pane.getDocument();

                final Disposable subscription = DocumentEventSource.fromDocumentEventsOf(doc)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Mockito.any());
                verify(error, never()).accept(Mockito.any());
                verify(complete, never()).run();

                // test insertUpdate
                insertStringToDocument(doc, 0, "test text");
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
                verifyNoMoreInteractions(action, error, complete);

                // test removeUpdate
                removeFromDocument(doc, 0, 5);
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.REMOVE)));
                verifyNoMoreInteractions(action, error, complete);

                // test changeUpdate
                Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
                ((HTMLDocument) doc).setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.CHANGE)));
                verifyNoMoreInteractions(action, error, complete);

                // test unsubscribe
                subscription.dispose();
                insertStringToDocument(doc, 0, "this should be ignored");
                verifyNoMoreInteractions(action, error, complete);
            }

        }).awaitTerminal();
    }

    @Test
    public void testObservingFilteredDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action() {

            @Override
            public void run() throws Throwable {
                @SuppressWarnings("unchecked")
                Consumer<DocumentEvent> action = mock(Consumer.class);
                @SuppressWarnings("unchecked")
                Consumer<Throwable> error = mock(Consumer.class);
                Action complete = mock(Action.class);

                final Document doc = new JEditorPane().getDocument();

                // filter only INSERT, others will be ignored
                final Set<DocumentEvent.EventType> filteredTypes = new HashSet<>(Arrays.asList(DocumentEvent.EventType.INSERT));
                final Disposable subscription = SwingObservable.fromDocumentEvents(doc, filteredTypes)
                        .subscribe(action, error, complete);

                verify(action, never()).accept(Mockito.any());
                verify(error, never()).accept(Mockito.any());
                verify(complete, never()).run();

                // test insertUpdate
                insertStringToDocument(doc, 0, "test text");
                verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
                verifyNoMoreInteractions(action, error, complete);

                // test removeUpdate
                removeFromDocument(doc, 0, 5);
                // removeUpdate should be ignored
                verifyNoMoreInteractions(action, error, complete);

                // test unsubscribe
                subscription.dispose();
                insertStringToDocument(doc, 0, "this should be ignored");
                verifyNoMoreInteractions(action, error, complete);
            }

        }).awaitTerminal();
    }

    private static ArgumentMatcher<DocumentEvent> documentEventMatcher(final DocumentEvent.EventType eventType) {
        return new ArgumentMatcher<DocumentEvent>() {
            @Override
            public boolean matches(DocumentEvent argument) {
                return argument.getType().equals(eventType);
            }
        };
    }

    private static void insertStringToDocument(Document doc, int offset, String text) {
        try {
            doc.insertString(offset, text, null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void removeFromDocument(Document doc, int offset, int length) {
        try {
            doc.remove(offset, length);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

}
