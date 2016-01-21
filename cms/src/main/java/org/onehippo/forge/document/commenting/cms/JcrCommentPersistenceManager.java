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
package org.onehippo.forge.document.commenting.cms;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrCommentPersistenceManager implements CommentPersistenceManager {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(JcrCommentPersistenceManager.class);

    private final static Object mutex = new Object();

    public final static String DEFAULT_COMMENTS_LOCATION = "doccommentdata";

    public final static String NT_COMMENTS_CONTAINER = "doccommenting:commentdatacontainer";

    public final static String NT_COMMENT = "doccommenting:commentdata";

    public final static String COMMENT_SUBJECTID = "doccommenting:subjectid";

    public final static String COMMENT_AUTHOR = "doccommenting:author";

    public final static String COMMENT_CREATED = "doccommenting:created";

    public final static String COMMENT_LAST_MODIFIED = "doccommenting:lastModified";

    public final static String COMMENT_CONTENT = "doccommenting:content";

    private static final String DEFAULT_COMMENTS_QUERY = "//element(*,doccommenting:commentdata)[@doccommenting:subjectid=''{0}''] order by @doccommenting:created descending";

    private static final long DEFAULT_MAX_QUERY_LIMIT = 100;

    private long queryLimit = DEFAULT_MAX_QUERY_LIMIT;

    public void createCommentItem(CommentItem commentItem) throws CommentingException {
        try {
            Node docCommentsDataNode = getDocCommentsDataNode(getSession());

            Node randomNode = createRandomNode(docCommentsDataNode);
            Node commentNode = randomNode.addNode("comment_" + System.currentTimeMillis(), NT_COMMENT);

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

    public List<CommentItem> getCommentItemsBySubjectId(String subjectId) throws CommentingException {
        List<CommentItem> commentItems = new LinkedList<>();

        try {
            String statement = MessageFormat.format(DEFAULT_COMMENTS_QUERY, subjectId);
            Query query = getSession().getWorkspace().getQueryManager().createQuery(statement, Query.XPATH);
            query.setLimit(getQueryLimit());

            QueryResult result = query.execute();

            for (NodeIterator nodeIt = result.getNodes(); nodeIt.hasNext();) {
                Node node = nodeIt.nextNode();
                CommentItem item = new CommentItem();
                item.setId(node.getIdentifier());
                item.setSubjectId(JcrUtils.getStringProperty(node, "doccommenting:subjectid", ""));
                item.setAuthor(JcrUtils.getStringProperty(node, "doccommenting:author", ""));
                item.setCreated(JcrUtils.getDateProperty(node, "doccommenting:created", null));
                item.setContent(JcrUtils.getStringProperty(node, "doccommenting:content", ""));
                commentItems.add(item);
            }
        } catch (RepositoryException e) {
            throw new CommentingException(e);
        }

        return commentItems;
    }

    public void updateCommentItem(CommentItem commentItem) throws CommentingException {
        if (StringUtils.isBlank(commentItem.getId())) {
            throw new IllegalArgumentException("No identifier in commentItem.");
        }

        try {
            Node commentNode = getSession().getNodeByIdentifier(commentItem.getId());

            if (!commentNode.isNodeType("mix:referenceable")) {
                commentNode.addMixin("mix:referenceable");
            }

            commentItem.setLastModified(Calendar.getInstance());
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

    public void deleteCommentItem(CommentItem commentItem) throws CommentingException {
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

    public long getQueryLimit() {
        return queryLimit;
    }

    public void setQueryLimit(long queryLimit) {
        this.queryLimit = queryLimit;
    }

    private Session getSession() {
        return UserSession.get().getJcrSession();
    }

    private static void bindCommentNode(final Node commentNode, final CommentItem commentItem)
            throws RepositoryException {
        commentNode.setProperty(COMMENT_SUBJECTID, StringUtils.defaultIfBlank(commentItem.getSubjectId(), ""));
        commentNode.setProperty(COMMENT_AUTHOR, StringUtils.defaultIfBlank(commentItem.getAuthor(), ""));
        commentNode.setProperty(COMMENT_CREATED,
                commentItem.getCreated() != null ? commentItem.getCreated() : Calendar.getInstance());

        if (commentItem.getLastModified() != null) {
            commentNode.setProperty(COMMENT_LAST_MODIFIED, commentItem.getLastModified());
        }

        commentNode.setProperty(COMMENT_CONTENT, StringUtils.defaultIfBlank(commentItem.getContent(), ""));

        for (String propName : commentItem.getAttributeNames()) {
            Object propValue = commentItem.getAttribute(propName);

            if (StringUtils.isNotBlank(propName) && propValue != null) {
                if (propValue instanceof String) {
                    commentNode.setProperty(propName, (String) propValue);
                } else if (propValue instanceof Calendar) {
                    commentNode.setProperty(propName, (Calendar) propValue);
                } else if (propValue instanceof Boolean) {
                    commentNode.setProperty(propName, (Boolean) propValue);
                } else if (propValue instanceof Long) {
                    commentNode.setProperty(propName, (Long) propValue);
                } else if (propValue instanceof BigDecimal) {
                    commentNode.setProperty(propName, (BigDecimal) propValue);
                }
            }
        }
    }

    private static Node getDocCommentsDataNode(final Session session) throws RepositoryException {
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

    private static void addInitialStructure(Node commentData) throws RepositoryException {
        char a = 'a';

        for (int i = 0; i < 26; i++) {
            Node letter = commentData.addNode(Character.toString((char) (a + i)), NT_COMMENTS_CONTAINER);

            for (int j = 0; j < 26; j++) {
                letter.addNode(Character.toString((char) (a + j)), NT_COMMENTS_CONTAINER);
            }
        }
    }

    private static Node createRandomNode(Node docCommentsData) throws RepositoryException {
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
