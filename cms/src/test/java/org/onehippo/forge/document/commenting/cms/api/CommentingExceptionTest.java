package org.onehippo.forge.document.commenting.cms.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommentingExceptionTest {

    @Test
    void defaultConstructor_canBeThrown() {
        assertThrows(CommentingException.class, () -> { throw new CommentingException(); });
    }

    @Test
    void messageConstructor_setsMessage() {
        assertEquals("error msg", new CommentingException("error msg").getMessage());
    }

    @Test
    void causeConstructor_setsCause() {
        RuntimeException cause = new RuntimeException("root");
        assertSame(cause, new CommentingException(cause).getCause());
    }

    @Test
    void messageAndCauseConstructor_setsBoth() {
        RuntimeException cause = new RuntimeException("cause");
        CommentingException ex = new CommentingException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        assertInstanceOf(RuntimeException.class, new CommentingException("x"));
    }
}
