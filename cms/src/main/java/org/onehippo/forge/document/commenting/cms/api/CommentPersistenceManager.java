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

public interface CommentPersistenceManager extends Serializable {

    public String createCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException;

    public CommentItem getCommentItemById(CommentingContext commentingContext, String commentId) throws CommentingException;

    public List<CommentItem> getCommentItemsBySubjectId(CommentingContext commentingContext, String subjectId) throws CommentingException;

    public void updateCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException;

    public void deleteCommentItem(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException;

    public String getCommentHeadText(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException;

    public String getCommentBodyText(CommentingContext commentingContext, CommentItem commentItem) throws CommentingException;

}
