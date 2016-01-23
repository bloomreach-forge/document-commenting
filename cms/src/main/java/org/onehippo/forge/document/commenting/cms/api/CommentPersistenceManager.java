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
import java.util.List;

/**
 * Comment data persistence manager service interface, providing CRUD operations.
 * <P>
 * An implementation may provide the service based on JCR repository.
 * Another implementation may possibly provide the service based on NoSQL data store for instance.
 * </P>
 */
public interface CommentPersistenceManager extends Serializable {

    /**
     * Create comment data item based on given {@code commentItem}.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @return the identifier of the newly created comment data item
     * @throws CommentingException if any exception occurs while creating a comment data item
     */
    public String createCommentItem(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Retrieves a comment data item by {@code commentId}.
     * @param commentingContext commenting context instance
     * @param commentId the identifier of the comment data item
     * @return {@link CommentItem} instance
     * @throws CommentingException if any exception occurs while retrieving a comment data item
     */
    public CommentItem getCommentItemById(CommentingContext commentingContext, String commentId)
            throws CommentingException;

    /**
     * Retrieves command data by {@code subjectId}, {@code offset} and {@code limit}.
     * @param commentingContext commenting context instance
     * @param subjectId the identifier of the subject data (e.g, document handle UUID).
     * @param offset first item index of data
     * @param limit max query item count limit
     * @return list of comment data items
     * @throws CommentingException if any exception occurs while retrieving comment data
     */
    public List<CommentItem> getLatestCommentItemsBySubjectId(CommentingContext commentingContext, String subjectId,
            long offset, long limit) throws CommentingException;

    /**
     * Updates comment data item by {@code commentItem}.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @throws CommentingException if any exception occurs while updating a comment data item
     */
    public void updateCommentItem(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Deletes comment data item based on information given by {@code commentItem}.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @throws CommentingException if any exception occurs while deleting a comment data item
     */
    public void deleteCommentItem(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Gets comment header text to display in user interface from {@code commentItem}.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @return comment header text to display in user interface
     * @throws CommentingException if any exception occurs while creating a comment data item
     */
    public String getCommentHeadText(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Gets comment body text to display in user interface from {@code commentItem}.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @return comment body text to display in user interface
     * @throws CommentingException if any exception occurs while creating a comment data item
     */
    public String getCommentBodyText(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Gets comment header tooltip text to display in user interface from {@code commentItem}.
     * Null if tooltip is not needed to display.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @return comment header tooltip text to display in user interface. Null if tooltip is not needed to display.
     * @throws CommentingException if any exception occurs while creating a comment data item
     */
    public String getCommentHeadTooltip(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

    /**
     * Gets comment body tooltip text to display in user interface from {@code commentItem}.
     * Null if tooltip is not needed to display.
     * @param commentingContext commenting context instance
     * @param commentItem comment data item object
     * @return comment body tooltip text to display in user interface. Null if tooltip is not needed to display.
     * @throws CommentingException if any exception occurs while creating a comment data item
     */
    public String getCommentBodyTooltip(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException;

}
