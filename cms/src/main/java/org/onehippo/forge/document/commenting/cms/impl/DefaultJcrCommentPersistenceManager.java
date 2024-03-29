/**
 * Copyright 2016-2022 Bloomreach (<a href="http://www.bloomreach.com">http://www.bloomreach.com</a>)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.document.commenting.cms.impl;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.util.JcrUtils;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.CommentPersistenceManager;
import org.onehippo.forge.document.commenting.cms.api.CommentingContext;
import org.onehippo.forge.document.commenting.cms.api.CommentingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CommentPersistenceManager} implementation which stores data into JCR repository.
 */
public class DefaultJcrCommentPersistenceManager implements CommentPersistenceManager {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DefaultJcrCommentPersistenceManager.class);
    private static final Object mutex = new Object();
    public static final String DEFAULT_COMMENTS_LOCATION = "doccommentdata";
    public static final String NT_COMMENTS_CONTAINER = "doccommenting:commentdatacontainer";
    public static final String NT_COMMENT = "doccommenting:commentdata";
    public static final String PROP_SUBJECTID = "doccommenting:subjectid";
    public static final String PROP_AUTHOR = "doccommenting:author";
    public static final String PROP_CREATED = "doccommenting:created";
    public static final String PROP_LAST_MODIFIED = "doccommenting:lastModified";
    public static final String PROP_CONTENT = "doccommenting:content";
    private static final Set<String> BUILTIN_PROP_NAMES =
            new HashSet<>(Arrays.asList(PROP_SUBJECTID, PROP_AUTHOR, PROP_CREATED, PROP_LAST_MODIFIED, PROP_CONTENT));
    private static final String DEFAULT_COMMENTS_QUERY =
            "//element(*,doccommenting:commentdata)[@doccommenting:subjectid=''{0}''] order by @doccommenting:created descending";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String dateFormat;

    public CommentItem getCommentItemById(CommentingContext commentingContext, String commentId) throws CommentingException {
        CommentItem commentItem = null;

        try {
            Node commentNode = getSession().getNodeByIdentifier(commentId);
            commentItem = new CommentItem();
            mapCommentItem(commentItem, commentNode);
        } catch (RepositoryException e) {
            throw new CommentingException(e);
        }

        return commentItem;
    }

    public List<CommentItem> getLatestCommentItemsBySubjectId(CommentingContext commentingContext, String subjectId,
            long offset, long limit) throws CommentingException {
        List<CommentItem> commentItems = new LinkedList<>();

        if (limit > 0) {
            try {
                Query query = createLatestCommentItemsReadQuery(commentingContext, subjectId, offset, limit);
                QueryResult result = query.execute();
                Node commentNode;

                for (NodeIterator nodeIt = result.getNodes(); nodeIt.hasNext();) {
                    commentNode = nodeIt.nextNode();
                    CommentItem commentItem = new CommentItem();
                    mapCommentItem(commentItem, commentNode);
                    commentItems.add(commentItem);
                }
            } catch (RepositoryException e) {
                throw new CommentingException(e);
            }
        }

        return commentItems;
    }

    public String createCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException {
        String commentId = null;

        try {
            commentItem.setSubjectId(commentingContext.getSubjectDocumentModel().getNode().getParent().getIdentifier());
            commentItem.setAuthor(getSession().getUserID());
            Node docCommentsDataNode = getDocCommentsDataNode(getSession());
            Node randomNode = createRandomNode(docCommentsDataNode);
            Node commentNode = randomNode.addNode("comment_" + System.currentTimeMillis(), NT_COMMENT);

            if (!commentNode.isNodeType("mix:referenceable")) {
                commentNode.addMixin("mix:referenceable");
            }

            bindCommentNode(commentNode, commentItem);
            getSession().save();
            commentId = commentNode.getIdentifier();
        } catch (RepositoryException e1) {
            try {
                getSession().refresh(false);
            } catch (RepositoryException e2) {
                log.error("Failed to refresh.", e2);
            }

            throw new CommentingException(e1);
        }

        return commentId;
    }

    public void updateCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException {
        if (StringUtils.isBlank(commentItem.getId())) {
            throw new IllegalArgumentException("No identifier in commentItem.");
        }

        try {
            commentItem.setSubjectId(commentingContext.getSubjectDocumentModel().getNode().getParent().getIdentifier());
            commentItem.setAuthor(getSession().getUserID());
            Node commentNode = getSession().getNodeByIdentifier(commentItem.getId());

            if (!commentNode.isNodeType("mix:referenceable")) {
                commentNode.addMixin("mix:referenceable");
            }

            bindCommentNode(commentNode, commentItem);
            getSession().save();
        } catch (RepositoryException e1) {
            try {
                getSession().refresh(false);
            } catch (RepositoryException e2) {
                log.error("Failed to refresh.", e2);
            }

            throw new CommentingException(e1);
        }
    }

    public void deleteCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException {
        try {
            Node commentNode = getSession().getNodeByIdentifier(commentItem.getId());
            commentNode.remove();
            getSession().save();
        } catch (ItemNotFoundException e) {
            throw new CommentingException(e);
        } catch (RepositoryException e1) {
            try {
                getSession().refresh(false);
            } catch (RepositoryException e2) {
                log.error("Failed to refresh.", e2);
            }

            throw new CommentingException(e1);
        }
    }

    public String getCommentHeadText(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException {
        StringBuilder sb = new StringBuilder(40);
        sb.append(getAuthorName(commentItem)).append(" - ")
                .append(DateFormatUtils.format(commentItem.getCreated(), getDateFormat(commentingContext)));
        return sb.toString();
    }

    public String getCommentBodyText(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException {
        return commentItem.getContent();
    }

    public String getCommentHeadTooltip(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException {
        return null;
    }

    public String getCommentBodyTooltip(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException {
        return null;
    }

    protected Session getSession() {
        return UserSession.get().getJcrSession();
    }

    protected Query createLatestCommentItemsReadQuery(CommentingContext commentingContext, String subjectId,
            long offset, long limit) throws RepositoryException {
        IPluginConfig config = commentingContext.getPluginConfig().getPluginConfig("cluster.options");
        String queryTemplate = config.getString("jcr.comment.persistence.query", DEFAULT_COMMENTS_QUERY);
        String statement = MessageFormat.format(queryTemplate, subjectId);
        Query query = getSession().getWorkspace().getQueryManager().createQuery(statement, Query.XPATH);

        if (offset > 0L) {
            query.setOffset(offset);
        }

        if (limit > 0L) {
            query.setLimit(limit);
        }

        return query;
    }

    protected void mapCommentItem(final CommentItem commentItem, final Node commentNode) throws RepositoryException {
        commentItem.setId(commentNode.getIdentifier());
        commentItem.setSubjectId(JcrUtils.getStringProperty(commentNode, PROP_SUBJECTID, ""));
        commentItem.setAuthor(JcrUtils.getStringProperty(commentNode, PROP_AUTHOR, ""));
        commentItem.setCreated(JcrUtils.getDateProperty(commentNode, PROP_CREATED, null));
        commentItem.setLastModified(JcrUtils.getDateProperty(commentNode, PROP_LAST_MODIFIED, null));
        commentItem.setContent(JcrUtils.getStringProperty(commentNode, PROP_CONTENT, ""));

        Property prop;
        String propName;
        int propType;

        for (PropertyIterator propIt = commentNode.getProperties(); propIt.hasNext(); ) {
            prop = propIt.nextProperty();
            propName = prop.getName();

            if (!BUILTIN_PROP_NAMES.contains(propName) && !prop.getDefinition().isProtected()) {
                propType = prop.getType();

                if (!prop.isMultiple()) {
                    if (propType == PropertyType.STRING) {
                        commentItem.setAttribute(propName, prop.getString());
                    } else if (propType == PropertyType.BOOLEAN) {
                        commentItem.setAttribute(propName, prop.getBoolean());
                    } else if (propType == PropertyType.LONG) {
                        commentItem.setAttribute(propName, prop.getLong());
                    } else if (propType == PropertyType.DOUBLE) {
                        commentItem.setAttribute(propName, prop.getDouble());
                    } else if (propType == PropertyType.DATE) {
                        commentItem.setAttribute(propName, prop.getDate());
                    }
                } else {
                    Value [] values = prop.getValues();

                    if (propType == PropertyType.STRING) {
                        String [] stringValues = new String[values.length];
                        for (int i = 0; i < values.length; i++) {
                            stringValues[i] = values[i].getString();
                        }
                        commentItem.setAttribute(propName, stringValues);
                    } else if (propType == PropertyType.BOOLEAN) {
                        boolean [] booleanValues = new boolean[values.length];
                        for (int i = 0; i < values.length; i++) {
                            booleanValues[i] = values[i].getBoolean();
                        }
                        commentItem.setAttribute(propName, booleanValues);
                    } else if (propType == PropertyType.LONG) {
                        long [] longValues = new long[values.length];
                        for (int i = 0; i < values.length; i++) {
                            longValues[i] = values[i].getLong();
                        }
                        commentItem.setAttribute(propName, longValues);
                    } else if (propType == PropertyType.DOUBLE) {
                        double [] doubleValues = new double[values.length];
                        for (int i = 0; i < values.length; i++) {
                            doubleValues[i] = values[i].getDouble();
                        }
                        commentItem.setAttribute(propName, doubleValues);
                    } else if (propType == PropertyType.DATE) {
                        Calendar [] dateValues = new Calendar[values.length];
                        for (int i = 0; i < values.length; i++) {
                            dateValues[i] = values[i].getDate();
                        }
                        commentItem.setAttribute(propName, dateValues);
                    }
                }
            }
        }
    }

    protected void bindCommentNode(final Node commentNode, final CommentItem commentItem) throws RepositoryException {
        commentNode.setProperty(PROP_SUBJECTID, StringUtils.defaultIfBlank(commentItem.getSubjectId(), ""));
        commentNode.setProperty(PROP_AUTHOR, StringUtils.defaultIfBlank(commentItem.getAuthor(), ""));

        Calendar now = Calendar.getInstance();

        if (!commentNode.hasProperty(PROP_CREATED)) {
            commentNode.setProperty(PROP_CREATED, now);
            commentNode.setProperty(PROP_LAST_MODIFIED, now);
        } else {
            commentNode.setProperty(PROP_LAST_MODIFIED, now);
        }

        commentNode.setProperty(PROP_CONTENT, StringUtils.defaultIfBlank(commentItem.getContent(), ""));

        Property existingProp;
        Object value;

        for (String propName : commentItem.getAttributeNames()) {
            existingProp = null;

            if (commentNode.hasProperty(propName)) {
                existingProp = commentNode.getProperty(propName);
            }

            if (existingProp != null && existingProp.getDefinition().isProtected()) {
                continue;
            }

            value = commentItem.getAttribute(propName);

            if (value == null) {
                continue;
            }

            if (!value.getClass().isArray()) {
                if (value instanceof String) {
                    commentNode.setProperty(propName, (String) value);
                } else if (value instanceof Boolean) {
                    commentNode.setProperty(propName, (Boolean) value);
                } else if (value instanceof Long) {
                    commentNode.setProperty(propName, (Long) value);
                } else if (value instanceof Double) {
                    commentNode.setProperty(propName, (Double) value);
                } else if (value instanceof Calendar) {
                    commentNode.setProperty(propName, (Calendar) value);
                }
            } else {
                if (value instanceof String[]) {
                    String [] stringValues = (String []) value;
                    Value [] values = new Value[stringValues.length];
                    for (int i = 0; i < stringValues.length; i++) {
                        values[i] = commentNode.getSession().getValueFactory().createValue(stringValues[i]);
                    }
                    commentNode.setProperty(propName, values);
                } else if (value instanceof boolean[]) {
                    boolean [] booleanValues = (boolean []) value;
                    Value [] values = new Value[booleanValues.length];
                    for (int i = 0; i < booleanValues.length; i++) {
                        values[i] = commentNode.getSession().getValueFactory().createValue(booleanValues[i]);
                    }
                    commentNode.setProperty(propName, values);
                } else if (value instanceof long[]) {
                    long [] longValues = (long []) value;
                    Value [] values = new Value[longValues.length];
                    for (int i = 0; i < longValues.length; i++) {
                        values[i] = commentNode.getSession().getValueFactory().createValue(longValues[i]);
                    }
                    commentNode.setProperty(propName, values);
                } else if (value instanceof double[]) {
                    double [] doubleValues = (double []) value;
                    Value [] values = new Value[doubleValues.length];
                    for (int i = 0; i < doubleValues.length; i++) {
                        values[i] = commentNode.getSession().getValueFactory().createValue(doubleValues[i]);
                    }
                    commentNode.setProperty(propName, values);
                } else if (value instanceof Calendar[]) {
                    Calendar [] dateValues = (Calendar []) value;
                    Value [] values = new Value[dateValues.length];
                    for (int i = 0; i < dateValues.length; i++) {
                        values[i] = commentNode.getSession().getValueFactory().createValue(dateValues[i]);
                    }
                    commentNode.setProperty(propName, values);
                }
            }
        }
    }

    protected String getDateFormat(final CommentingContext commentingContext) {
        if (dateFormat == null) {
            IPluginConfig config = commentingContext.getPluginConfig().getPluginConfig("cluster.options");
            dateFormat = config.getString("jcr.comment.persistence.date.format", DEFAULT_DATE_FORMAT);
        }

        return dateFormat;
    }

    protected String getAuthorName(final CommentItem commentItem) {
        return commentItem.getAuthor();
    }

    private Node getDocCommentsDataNode(final Session session) throws RepositoryException {
        Node rootNode = session.getRootNode();
        Node docCommentsDataNode;

        if (!rootNode.hasNode(DEFAULT_COMMENTS_LOCATION)) {
            synchronized (mutex) {
                docCommentsDataNode = rootNode.addNode(DEFAULT_COMMENTS_LOCATION, NT_COMMENTS_CONTAINER);
                addInitialStructure(docCommentsDataNode);
            }
        } else {
            docCommentsDataNode = rootNode.getNode(DEFAULT_COMMENTS_LOCATION);
        }

        return docCommentsDataNode;
    }

    private void addInitialStructure(Node commentData) throws RepositoryException {
        char a = 'a';

        for (int i = 0; i < 26; i++) {
            Node letter = commentData.addNode(Character.toString((char) (a + i)), NT_COMMENTS_CONTAINER);

            for (int j = 0; j < 26; j++) {
                letter.addNode(Character.toString((char) (a + j)), NT_COMMENTS_CONTAINER);
            }
        }
    }

    private Node createRandomNode(Node docCommentsData) throws RepositoryException {
        Node result = docCommentsData;
        char a = 'a';
        Random rand = new Random();
        boolean needCheck = true;

        for (int i = 0; i < 4; i++) {
            int r = rand.nextInt(26);

            if (needCheck && result.hasNode(Character.toString((char) (a + r)))) {
                result = result.getNode(Character.toString((char) (a + r)));
            } else {
                needCheck = false;
                result = result.addNode(Character.toString((char) (a + r)), NT_COMMENTS_CONTAINER);
            }
        }

        return result;
    }

}
