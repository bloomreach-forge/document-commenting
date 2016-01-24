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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Comment data holder object.
 */
public class CommentItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Comment data item identifier.
     */
    private String id;

    /**
     * The subject data identifier with this comment data item is associated. e.g, document handle UUID.
     */
    private String subjectId;

    /**
     * Comment author user ID.
     */
    private String author;

    /**
     * Creation date time of this comment data item.
     */
    private Calendar created;

    /**
     * Last modified date time of this comment data item.
     */
    private Calendar lastModified;

    /**
     * Comment content data.
     */
    private String content;

    /**
     * Extra attributes map of this comment data.
     */
    private Map<String, Object> attributes = new LinkedHashMap<>();

    public CommentItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(Calendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet(attributes.keySet());
    }
}
