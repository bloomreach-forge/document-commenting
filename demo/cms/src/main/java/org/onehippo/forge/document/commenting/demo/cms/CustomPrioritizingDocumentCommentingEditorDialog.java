/**
 * Copyright 2016-2021 Bloomreach
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

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.CommentPersistenceManager;
import org.onehippo.forge.document.commenting.cms.api.CommentingContext;
import org.onehippo.forge.document.commenting.cms.api.SerializableCallable;
import org.onehippo.forge.document.commenting.cms.impl.DefaultDocumentCommentingEditorDialog;

public class CustomPrioritizingDocumentCommentingEditorDialog extends DefaultDocumentCommentingEditorDialog {

    private static final long serialVersionUID = 1L;

    static final String PRIORITY_PROP_NAME = "doccommentingdemo:priority";

    public CustomPrioritizingDocumentCommentingEditorDialog(IModel<String> titleModel, CommentingContext commentingContext,
            CommentPersistenceManager commentPersistenceManager, CommentItem currentCommentItem,
            SerializableCallable<Object> onOkCallback) {

        super(titleModel, commentingContext, commentPersistenceManager, currentCommentItem, onOkCallback);

        WebMarkupContainer prioritySelectorContainer = new WebMarkupContainer("priority-selector");

        final List<String> priorityList = Arrays.asList("Normal", "Blocker", "Top", "High", "Low", "Trivial");
        final DropDownChoice<String> priorityChoice =
                new DropDownChoice<String>("priority", new Model<String>() {
                    @Override
                    public String getObject() {
                        return (String) getCurrentCommentItem().getAttribute(PRIORITY_PROP_NAME);
                    }

                    @Override
                    public void setObject(String object) {
                        getCurrentCommentItem().setAttribute(PRIORITY_PROP_NAME, object);
                    }
                }, priorityList);

        prioritySelectorContainer.add(priorityChoice);

        add(prioritySelectorContainer);
    }
}
