/**
 * Copyright 2016-2022 Bloomreach
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

import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.SerializableCallable;
import org.onehippo.forge.document.commenting.cms.impl.DefaultDocumentCommentingFieldPlugin;

public class CustomPrioritizingDocumentCommentingFieldPlugin extends DefaultDocumentCommentingFieldPlugin {

    private static final long serialVersionUID = 1L;

    public CustomPrioritizingDocumentCommentingFieldPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
    }

    @Override
    protected AbstractDialog createDialogInstance(final CommentItem commentItem, final SerializableCallable<Object> onOkCallback) {
        return new CustomPrioritizingDocumentCommentingEditorDialog(getCaptionModel(), getCommentingContext(), getCommentPersistenceManager(),
                commentItem, onOkCallback);
    }

}
