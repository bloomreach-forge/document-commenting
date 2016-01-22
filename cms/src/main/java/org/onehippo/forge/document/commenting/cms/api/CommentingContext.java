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

import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;

public class CommentingContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private IPluginContext pluginContext;

    private IPluginConfig pluginConfig;

    private JcrNodeModel subjectDocumentModel;

    public CommentingContext() {
    }

    public CommentingContext(IPluginContext pluginContext, IPluginConfig pluginConfig, JcrNodeModel subjectDocumentModel) {
        this.pluginContext = pluginContext;
        this.pluginConfig = pluginConfig;
        this.subjectDocumentModel = subjectDocumentModel;
    }

    public IPluginContext getPluginContext() {
        return pluginContext;
    }

    public void setPluginContext(IPluginContext pluginContext) {
        this.pluginContext = pluginContext;
    }

    public IPluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public void setPluginConfig(IPluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    public JcrNodeModel getSubjectDocumentModel() {
        return subjectDocumentModel;
    }

    public void setSubjectDocumentModel(JcrNodeModel subjectDocumentModel) {
        this.subjectDocumentModel = subjectDocumentModel;
    }

}
