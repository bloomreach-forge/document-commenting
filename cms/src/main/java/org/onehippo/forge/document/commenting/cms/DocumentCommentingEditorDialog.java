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

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentCommentingEditorDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(DocumentCommentingEditorDialog.class);

    private final IModel<String> titleModel;

    private final IPluginConfig pluginConfig;

    private final IPluginContext pluginContext;

    private final IValueMap dialogSize;

    public DocumentCommentingEditorDialog(IModel<String> titleModel, IPluginConfig pluginConfig, IPluginContext pluginContext, IModel model) {
        super(model);

        setOutputMarkupId(true);

        this.titleModel = titleModel;

        this.pluginConfig = pluginConfig;
        this.pluginContext = pluginContext;

        final String dialogSizeParam = getPluginConfig().getString(PluginConstants.PARAM_DIALOG_SIZE, PluginConstants.DEFAULT_DIALOG_SIZE);
        dialogSize = new ValueMap(dialogSizeParam).makeImmutable();

        if (getModel().getObject() == null) {
            setOkVisible(false);
            setOkEnabled(false);
        }

        final TextArea<String> content = new TextArea<String>("content", Model.of(""));
        content.setRequired(false);
        add(content);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DocumentCommentingEditorDialog.class, DocumentCommentingEditorDialog.class.getSimpleName() + ".css")));
    }

    @Override
    protected void onOk() {
    }

    @Override
    public IModel<String> getTitle() {
        return titleModel;
    }

    @Override
    public IValueMap getProperties() {
        return dialogSize;
    }

    protected IPluginConfig getPluginConfig() {
        return pluginConfig;
    }

    protected IPluginContext getPluginContext() {
        return pluginContext;
    }

}

