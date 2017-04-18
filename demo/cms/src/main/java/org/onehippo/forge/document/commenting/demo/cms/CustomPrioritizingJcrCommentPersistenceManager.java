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
package org.onehippo.forge.document.commenting.demo.cms;

import org.apache.commons.lang.StringUtils;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.CommentingContext;
import org.onehippo.forge.document.commenting.cms.api.CommentingException;
import org.onehippo.forge.document.commenting.cms.impl.DefaultJcrCommentPersistenceManager;

public class CustomPrioritizingJcrCommentPersistenceManager extends DefaultJcrCommentPersistenceManager {

    private static final long serialVersionUID = 1L;

    public String getCommentBodyTooltip(CommentingContext commentingContext, CommentItem commentItem)
            throws CommentingException {
        String priority = (String) commentItem
                .getAttribute(CustomPrioritizingDocumentCommentingEditorDialog.PRIORITY_PROP_NAME);
        return "Priority: " + StringUtils.defaultIfBlank(priority, "Unknown");
    }

}
