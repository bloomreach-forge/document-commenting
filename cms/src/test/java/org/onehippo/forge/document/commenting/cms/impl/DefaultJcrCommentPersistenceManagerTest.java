package org.onehippo.forge.document.commenting.cms.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.CommentingContext;
import org.onehippo.forge.document.commenting.cms.api.CommentingException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultJcrCommentPersistenceManagerTest {

    @Mock private Session session;
    @Mock private Node commentNode;
    @Mock private PropertyIterator propertyIterator;

    /** Subclass that bypasses UserSession.get() */
    static class TestableManager extends DefaultJcrCommentPersistenceManager {
        private final Session mockSession;
        TestableManager(Session s) { this.mockSession = s; }
        @Override protected Session getSession() { return mockSession; }
    }

    private TestableManager manager;

    @BeforeEach
    void setUp() {
        manager = new TestableManager(session);
    }

    // --- constants ---

    @Test
    void constants_haveExpectedValues() {
        assertEquals("doccommentdata", DefaultJcrCommentPersistenceManager.DEFAULT_COMMENTS_LOCATION);
        assertEquals("doccommenting:commentdatacontainer", DefaultJcrCommentPersistenceManager.NT_COMMENTS_CONTAINER);
        assertEquals("doccommenting:commentdata", DefaultJcrCommentPersistenceManager.NT_COMMENT);
        assertEquals("doccommenting:subjectid", DefaultJcrCommentPersistenceManager.PROP_SUBJECTID);
        assertEquals("doccommenting:author", DefaultJcrCommentPersistenceManager.PROP_AUTHOR);
        assertEquals("doccommenting:created", DefaultJcrCommentPersistenceManager.PROP_CREATED);
        assertEquals("doccommenting:lastModified", DefaultJcrCommentPersistenceManager.PROP_LAST_MODIFIED);
        assertEquals("doccommenting:content", DefaultJcrCommentPersistenceManager.PROP_CONTENT);
    }

    // --- getCommentBodyText / trivial methods ---

    @Test
    void getCommentBodyText_returnsContent() throws CommentingException {
        CommentItem item = new CommentItem();
        item.setContent("Hello world");
        assertEquals("Hello world", manager.getCommentBodyText(null, item));
    }

    @Test
    void getCommentHeadTooltip_returnsNull() throws CommentingException {
        assertNull(manager.getCommentHeadTooltip(null, new CommentItem()));
    }

    @Test
    void getCommentBodyTooltip_returnsNull() throws CommentingException {
        assertNull(manager.getCommentBodyTooltip(null, new CommentItem()));
    }

    // --- mapCommentItem: standard properties ---

    @Test
    void mapCommentItem_mapsStandardProperties() throws RepositoryException {
        Calendar created = Calendar.getInstance();
        Calendar lastModified = Calendar.getInstance();

        stubStringProperty(commentNode, DefaultJcrCommentPersistenceManager.PROP_SUBJECTID, "subject-uuid");
        stubStringProperty(commentNode, DefaultJcrCommentPersistenceManager.PROP_AUTHOR, "testuser");
        stubStringProperty(commentNode, DefaultJcrCommentPersistenceManager.PROP_CONTENT, "Test comment");
        stubCalendarProperty(commentNode, DefaultJcrCommentPersistenceManager.PROP_CREATED, created);
        stubCalendarProperty(commentNode, DefaultJcrCommentPersistenceManager.PROP_LAST_MODIFIED, lastModified);
        when(commentNode.getIdentifier()).thenReturn("comment-id-123");
        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(false);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertEquals("comment-id-123", item.getId());
        assertEquals("subject-uuid", item.getSubjectId());
        assertEquals("testuser", item.getAuthor());
        assertEquals("Test comment", item.getContent());
        assertSame(created, item.getCreated());
        assertSame(lastModified, item.getLastModified());
    }

    // --- mapCommentItem: extra single-value properties ---

    @Test
    void mapCommentItem_extraStringProperty_addedToAttributes() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property extraProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        when(extraProp.getName()).thenReturn("custom:tag");
        when(extraProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(false);
        when(extraProp.isMultiple()).thenReturn(false);
        when(extraProp.getType()).thenReturn(PropertyType.STRING);
        when(extraProp.getString()).thenReturn("forge");

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(extraProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertEquals("forge", item.getAttribute("custom:tag"));
    }

    @Test
    void mapCommentItem_extraBooleanProperty_addedToAttributes() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property extraProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        when(extraProp.getName()).thenReturn("custom:approved");
        when(extraProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(false);
        when(extraProp.isMultiple()).thenReturn(false);
        when(extraProp.getType()).thenReturn(PropertyType.BOOLEAN);
        when(extraProp.getBoolean()).thenReturn(true);

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(extraProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertEquals(true, item.getAttribute("custom:approved"));
    }

    @Test
    void mapCommentItem_extraLongProperty_addedToAttributes() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property extraProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        when(extraProp.getName()).thenReturn("custom:score");
        when(extraProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(false);
        when(extraProp.isMultiple()).thenReturn(false);
        when(extraProp.getType()).thenReturn(PropertyType.LONG);
        when(extraProp.getLong()).thenReturn(42L);

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(extraProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertEquals(42L, item.getAttribute("custom:score"));
    }

    @Test
    void mapCommentItem_protectedProperty_skipped() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property protectedProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        when(protectedProp.getName()).thenReturn("jcr:primaryType");
        when(protectedProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(true);

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(protectedProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertNull(item.getAttribute("jcr:primaryType"));
    }

    // --- bindCommentNode ---

    @Test
    void bindCommentNode_whenNoCreatedProperty_setsCreatedAndLastModified() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("sub-id");
        item.setAuthor("author");
        item.setContent("content");

        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(false);

        manager.bindCommentNode(commentNode, item);

        verify(commentNode).setProperty(eq(DefaultJcrCommentPersistenceManager.PROP_SUBJECTID), eq("sub-id"));
        verify(commentNode).setProperty(eq(DefaultJcrCommentPersistenceManager.PROP_AUTHOR), eq("author"));
        verify(commentNode).setProperty(eq(DefaultJcrCommentPersistenceManager.PROP_CONTENT), eq("content"));
        // Both created and lastModified set on first creation
        verify(commentNode, times(2)).setProperty(
                argThat(name -> name.equals(DefaultJcrCommentPersistenceManager.PROP_CREATED)
                        || name.equals(DefaultJcrCommentPersistenceManager.PROP_LAST_MODIFIED)),
                any(Calendar.class));
    }

    @Test
    void bindCommentNode_whenCreatedPropertyExists_setsOnlyLastModified() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("sub-id");
        item.setAuthor("author");
        item.setContent("content");

        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(true);

        manager.bindCommentNode(commentNode, item);

        // Only lastModified should be set (not created again)
        verify(commentNode).setProperty(
                eq(DefaultJcrCommentPersistenceManager.PROP_LAST_MODIFIED), any(Calendar.class));
        verify(commentNode, never()).setProperty(
                eq(DefaultJcrCommentPersistenceManager.PROP_CREATED), any(Calendar.class));
    }

    @Test
    void bindCommentNode_withStringAttribute_setsProperty() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("s");
        item.setAuthor("a");
        item.setContent("c");
        item.setAttribute("custom:tag", "my-tag");

        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(false);
        when(commentNode.hasProperty("custom:tag")).thenReturn(false);

        manager.bindCommentNode(commentNode, item);

        verify(commentNode).setProperty("custom:tag", "my-tag");
    }

    // --- mapCommentItem: multi-value string property ---

    @Test
    void mapCommentItem_multiValueStringProperty_addedToAttributes() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property multiProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        when(multiProp.getName()).thenReturn("custom:tags");
        when(multiProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(false);
        when(multiProp.isMultiple()).thenReturn(true);
        when(multiProp.getType()).thenReturn(PropertyType.STRING);
        when(v1.getString()).thenReturn("tag1");
        when(v2.getString()).thenReturn("tag2");
        when(multiProp.getValues()).thenReturn(new Value[]{v1, v2});

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(multiProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertArrayEquals(new String[]{"tag1", "tag2"}, (String[]) item.getAttribute("custom:tags"));
    }

    @Test
    void mapCommentItem_multiValueLongProperty_addedToAttributes() throws RepositoryException {
        stubStandardProperties(commentNode);

        Property multiProp = mock(Property.class);
        PropertyDefinition def = mock(PropertyDefinition.class);
        Value v1 = mock(Value.class);
        when(multiProp.getName()).thenReturn("custom:counts");
        when(multiProp.getDefinition()).thenReturn(def);
        when(def.isProtected()).thenReturn(false);
        when(multiProp.isMultiple()).thenReturn(true);
        when(multiProp.getType()).thenReturn(PropertyType.LONG);
        when(v1.getLong()).thenReturn(99L);
        when(multiProp.getValues()).thenReturn(new Value[]{v1});

        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(true, false);
        when(propertyIterator.nextProperty()).thenReturn(multiProp);

        CommentItem item = new CommentItem();
        manager.mapCommentItem(item, commentNode);

        assertArrayEquals(new long[]{99L}, (long[]) item.getAttribute("custom:counts"));
    }

    // --- bindCommentNode: extra attribute types ---

    @Test
    void bindCommentNode_withBooleanAttribute_setsProperty() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("s"); item.setAuthor("a"); item.setContent("c");
        item.setAttribute("custom:active", Boolean.TRUE);
        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(false);
        lenient().when(commentNode.hasProperty("custom:active")).thenReturn(false);

        manager.bindCommentNode(commentNode, item);
        verify(commentNode).setProperty("custom:active", true);
    }

    @Test
    void bindCommentNode_withLongAttribute_setsProperty() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("s"); item.setAuthor("a"); item.setContent("c");
        item.setAttribute("custom:count", 7L);
        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(false);
        lenient().when(commentNode.hasProperty("custom:count")).thenReturn(false);

        manager.bindCommentNode(commentNode, item);
        verify(commentNode).setProperty("custom:count", 7L);
    }

    @Test
    void bindCommentNode_withNullAttribute_skipsProperty() throws RepositoryException {
        CommentItem item = new CommentItem();
        item.setSubjectId("s"); item.setAuthor("a"); item.setContent("c");
        item.setAttribute("custom:nullprop", null);
        when(commentNode.hasProperty(DefaultJcrCommentPersistenceManager.PROP_CREATED)).thenReturn(false);

        manager.bindCommentNode(commentNode, item);
        verify(commentNode, never()).setProperty(eq("custom:nullprop"), anyString());
    }

    // --- getCommentItemById ---

    @Test
    void getCommentItemById_whenNodeFound_returnsCommentItem() throws Exception {
        when(session.getNodeByIdentifier("id-abc")).thenReturn(commentNode);
        stubStandardProperties(commentNode);
        when(commentNode.getIdentifier()).thenReturn("id-abc");
        when(commentNode.getProperties()).thenReturn(propertyIterator);
        when(propertyIterator.hasNext()).thenReturn(false);

        CommentItem result = manager.getCommentItemById(null, "id-abc");

        assertNotNull(result);
        assertEquals("id-abc", result.getId());
    }

    @Test
    void getCommentItemById_whenRepositoryException_throwsCommentingException() throws Exception {
        when(session.getNodeByIdentifier("bad-id")).thenThrow(new RepositoryException("not found"));

        assertThrows(CommentingException.class, () -> manager.getCommentItemById(null, "bad-id"));
    }

    // --- helpers ---

    private static void stubStandardProperties(Node node) throws RepositoryException {
        stubStringProperty(node, DefaultJcrCommentPersistenceManager.PROP_SUBJECTID, "sub");
        stubStringProperty(node, DefaultJcrCommentPersistenceManager.PROP_AUTHOR, "auth");
        stubStringProperty(node, DefaultJcrCommentPersistenceManager.PROP_CONTENT, "cont");
        stubCalendarProperty(node, DefaultJcrCommentPersistenceManager.PROP_CREATED, Calendar.getInstance());
        stubCalendarProperty(node, DefaultJcrCommentPersistenceManager.PROP_LAST_MODIFIED, Calendar.getInstance());
        when(node.getIdentifier()).thenReturn("node-id");
    }

    private static void stubStringProperty(Node node, String name, String value) throws RepositoryException {
        Property prop = mock(Property.class);
        when(node.hasProperty(name)).thenReturn(true);
        when(node.getProperty(name)).thenReturn(prop);
        when(prop.getString()).thenReturn(value);
    }

    private static void stubCalendarProperty(Node node, String name, Calendar value) throws RepositoryException {
        Property prop = mock(Property.class);
        lenient().when(node.hasProperty(name)).thenReturn(true);
        lenient().when(node.getProperty(name)).thenReturn(prop);
        lenient().when(prop.getDate()).thenReturn(value);
    }
}
