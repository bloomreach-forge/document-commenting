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
import java.util.Calendar;
import java.util.Map;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

public class CommentingPersistUtils {

    private final static Object mutex = new Object();

    public final static String DEFAULT_COMMENTS_LOCATION = "doccommentdata";

    public final static String NT_COMMENTS_CONTAINER = "doccommenting:commentdatacontainer";

    public final static String NT_COMMENT = "doccommenting:commentdata";

    public final static String COMMENT_SUBJECTID = "doccommenting:subjectid";

    public final static String COMMENT_AUTHOR = "doccommenting:author";

    public final static String COMMENT_CREATED = "doccommenting:created";

    public final static String COMMENT_CONTENT = "doccommenting:content";

    public CommentingPersistUtils() {
    }

    public static Node persistCommentData(final Session session, final String subjectId, final String content,
            final Map<String, Object> extraDataMap) throws RepositoryException {
        Node commentNode = null;

        try {
            Node rootNode = session.getRootNode();
            Node docCommentsData;

            if (!rootNode.hasNode(DEFAULT_COMMENTS_LOCATION)) {
                synchronized (mutex) {
                    docCommentsData = rootNode.addNode(DEFAULT_COMMENTS_LOCATION, NT_COMMENTS_CONTAINER);
                    addInitialStructure(docCommentsData);
                }
            } else {
                docCommentsData = rootNode.getNode(DEFAULT_COMMENTS_LOCATION);
            }

            Node randomNode = createRandomNode(docCommentsData);
            commentNode = randomNode.addNode("tick_" + System.currentTimeMillis(), NT_COMMENT);
            commentNode.addMixin("mix:referenceable");

            commentNode.setProperty(COMMENT_SUBJECTID, StringUtils.defaultIfBlank(subjectId, ""));
            commentNode.setProperty(COMMENT_AUTHOR, StringUtils.defaultIfBlank(session.getUserID(), ""));
            commentNode.setProperty(COMMENT_CREATED, Calendar.getInstance());
            commentNode.setProperty(COMMENT_CONTENT, StringUtils.defaultIfBlank(content, ""));

            if (extraDataMap != null && !extraDataMap.isEmpty()) {
                String propName;
                Object propValue;

                for (Map.Entry<String, Object> entry : extraDataMap.entrySet()) {
                    propName = StringUtils.trim(entry.getKey());
                    propValue = entry.getValue();

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

            session.save();
        } catch (RepositoryException e) {
            session.refresh(false);
            throw e;
        }

        return commentNode;
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
