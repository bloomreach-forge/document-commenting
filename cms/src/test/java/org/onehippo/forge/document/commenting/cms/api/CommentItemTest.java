/**
 * Copyright 2016-2016 Hippo B.V. (http://www.onehippo.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.document.commenting.cms.api;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Calendar;

import org.junit.Test;

public class CommentItemTest {

    @Test
    public void testCloneAndEqualsAndHashCode() throws Exception {
        CommentItem comment1 = new CommentItem();
        comment1.setId("123");
        comment1.setSubjectId("456");
        comment1.setAuthor("author");
        comment1.setCreated(Calendar.getInstance());
        comment1.setLastModified(Calendar.getInstance());
        comment1.setContent("Hello, World!");
        comment1.setAttribute("attr1", "value1");
        comment1.setAttribute("attr2", "value2");

        CommentItem comment2 = (CommentItem) comment1.clone();
        assertEquals(comment1, comment2);
        assertEquals(comment1.hashCode(), comment2.hashCode());
        assertEquals(comment1.getId(), comment2.getId());
        assertEquals(comment1.getSubjectId(), comment2.getSubjectId());
        assertEquals(comment1.getAuthor(), comment2.getAuthor());
        assertEquals(comment1.getCreated(), comment2.getCreated());
        assertEquals(comment1.getLastModified(), comment2.getLastModified());
        assertEquals(comment1.getContent(), comment2.getContent());
        assertEquals(comment1.getAttribute("attr1"), comment1.getAttribute("attr1"));
        assertEquals(comment1.getAttribute("attr2"), comment1.getAttribute("attr2"));

        comment2.setContent(comment2.getContent() + " " + comment2.getContent());
        assertEquals("Hello, World! Hello, World!", comment2.getContent());
        assertFalse(comment1.equals(comment2));
        assertFalse(comment1.hashCode() == comment2.hashCode());
    }

}
