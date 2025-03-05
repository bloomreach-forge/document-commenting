/**
 * Copyright 2016-2025 Bloomreach (http://www.bloomreach.com)
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
package org.onehippo.forge.document.commenting.cms.impl;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.ckeditor.AutoSaveBehavior;
import org.hippoecm.frontend.plugins.ckeditor.CKEditorPanel;
import org.hippoecm.frontend.plugins.ckeditor.CKEditorPanelAutoSaveExtension;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.onehippo.ckeditor.CKEditorConfig;
import org.onehippo.forge.document.commenting.cms.api.CommentItem;
import org.onehippo.forge.document.commenting.cms.api.CommentPersistenceManager;
import org.onehippo.forge.document.commenting.cms.api.CommentingContext;
import org.onehippo.forge.document.commenting.cms.api.SerializableCallable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.hippoecm.frontend.plugins.ckeditor.AbstractCKEditorPlugin.CONFIG_CKEDITOR_CONFIG_APPENDED_JSON;
import static org.hippoecm.frontend.plugins.ckeditor.AbstractCKEditorPlugin.CONFIG_CKEDITOR_CONFIG_LICENSE_KEY;
import static org.hippoecm.frontend.plugins.ckeditor.AbstractCKEditorPlugin.CONFIG_CKEDITOR_CONFIG_OVERLAYED_JSON;

public class DefaultDocumentCommentingEditorDialog extends AbstractDialog<Node> {

    private static final long serialVersionUID = 1L;
    private static final String CK_EDITOR_LICENSE_PROPERTY = "/hippo:namespaces/hippostd/html/editor:templates/_default_/" + CONFIG_CKEDITOR_CONFIG_LICENSE_KEY;

    private static Logger log = LoggerFactory.getLogger(DefaultDocumentCommentingEditorDialog.class);
    private static final String EDITOR_CONFIG_JSON = "editor.config.json";
    public static final String DEFAULT_EDITOR_CONFIG = "{"
            // do not html encode but utf-8 encode hence entities = false
            + "  entities: false,"
            // &gt; must not be replaced with > hence basicEntities = true
            + "  basicEntities: true,"
            + "  autoUpdateElement: false,"
            + "  contentsCss: ['ckeditor/hippocontents.css'],"
            + "  plugins: 'basicstyles,button,clipboard,enterkey,entities,floatingspace,floatpanel,htmlwriter,list,listblock,menu,menubutton,panel,panelbutton,removeformat,richcombo,stylescombo,tab,toolbar,undo',"
            + "  removePlugins: 'divarea,liststyle,tabletools,tableresize',"
            + "  extraPlugins: 'wysiwygarea',"
            + "  disableNativeSpellChecker: false,"
            + "  title: false,"
            + "  toolbar: ["
            + "    { name: 'basicstyles', items: [ 'Bold', 'Italic' ] },"
            + "    { name: 'clipboard', items: [ 'Undo', 'Redo' ] },"
            + "    { name: 'paragraph', items: [ 'NumberedList', 'BulletedList' ] }"
            + "  ]"
            + "}";
    private final IModel<String> titleModel;
    private final CommentingContext commentingContext;
    private final CommentPersistenceManager commentPersistenceManager;
    /**
     * Original source of the comment item.
     */
    private final CommentItem originalCommentItem;
    /**
     * The comment item instance currently being edited in this dialog, clone from {@code originalCommentItem}.
     */
    private final CommentItem currentCommentItem;
    private final SerializableCallable<Object> onOkCallback;
    private final IValueMap dialogSize;
    private CKEditorPanel contentEditor;
    private boolean autoSaveExtensionProcessPending;

    public DefaultDocumentCommentingEditorDialog(IModel<String> titleModel, CommentingContext commentingContext,
            CommentPersistenceManager commentPersistenceManager, final CommentItem originalCommentItem,
            SerializableCallable<Object> onOkCallback) {

        super(commentingContext.getSubjectDocumentModel());
        setOutputMarkupId(true);
        this.titleModel = titleModel;
        this.commentingContext = commentingContext;
        this.commentPersistenceManager = commentPersistenceManager;
        this.originalCommentItem = originalCommentItem;
        this.currentCommentItem = (CommentItem) originalCommentItem.clone();
        this.onOkCallback = onOkCallback;

        final IPluginConfig pluginConfig = getCommentingContext().getPluginConfig();
        final String dialogSizeParam = pluginConfig.getString(PluginConstants.PARAM_DIALOG_SIZE,
                PluginConstants.DEFAULT_DIALOG_SIZE);
        dialogSize = new ValueMap(dialogSizeParam).makeImmutable();
        if (getModel().getObject() == null) {
            setOkVisible(false);
            setOkEnabled(false);
        }
        final String ckEditorConfig = getCkEditorConfig(pluginConfig);
        contentEditor = createEditPanel("content", ckEditorConfig);
        add(contentEditor);
    }

    private String getCkEditorConfig(final IPluginConfig pluginConfig ) {
        final String defaultJson = pluginConfig.getString(EDITOR_CONFIG_JSON, DEFAULT_EDITOR_CONFIG);
        final String overlayedJson = pluginConfig.getString(CONFIG_CKEDITOR_CONFIG_OVERLAYED_JSON);
        final String appendedJson = pluginConfig.getString(CONFIG_CKEDITOR_CONFIG_APPENDED_JSON);
        final ObjectNode config;
        try {
            config = CKEditorConfig.combineConfig(defaultJson, overlayedJson, appendedJson);

            final String licenseKey = readLicenseKey();
            if(StringUtils.isNotBlank(licenseKey)) {
                CKEditorConfig.setLicenseKey(config, licenseKey);
            }

            return config.toString();
        } catch (RepositoryException | IOException e) {
            log.warn("Unable to process CK editor config", e);
            return defaultJson;
        }
    }

    private String readLicenseKey() throws RepositoryException {
        final HippoSession jcrSession = UserSession.get().getJcrSession();
        if(jcrSession.propertyExists(CK_EDITOR_LICENSE_PROPERTY)) {
            return jcrSession.getProperty(CK_EDITOR_LICENSE_PROPERTY).getString();
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DefaultDocumentCommentingEditorDialog.class,
                DefaultDocumentCommentingEditorDialog.class.getSimpleName() + ".css")));
    }

    @Override
    protected void onOk() {
        super.onOk();
        boolean created = false;
        boolean updated = false;

        try {
            if (StringUtils.isBlank(getCurrentCommentItem().getId())) {
                if (isValidCommentInputForCreate(getCurrentCommentItem())) {
                    getCommentPersistenceManager().createCommentItem(getCommentingContext(), getCurrentCommentItem());
                    created = true;
                }
            } else {
                if (isValidCommentInputForUpdate(getCurrentCommentItem()) && isCurrentCommentItemUpdated()) {
                    getCommentPersistenceManager().updateCommentItem(getCommentingContext(), getCurrentCommentItem());
                    updated = true;
                }
            }

            if ((created || updated) && (getOnOkCallback() != null)) {
                getOnOkCallback().call();
            }
        } catch (Exception e) {
            log.error("Failed to persist comment data.", e);
        }
    }

    protected boolean isValidCommentInputForCreate(final CommentItem commentItem) {
        return StringUtils.isNotBlank(commentItem.getContent());
    }

    protected boolean isValidCommentInputForUpdate(final CommentItem commentItem) {
        return true;
    }

    protected boolean isCurrentCommentItemUpdated() {
        return !getCurrentCommentItem().equals(getOriginalCommentItem());
    }

    @Override
    public IModel<String> getTitle() {
        return titleModel;
    }

    @Override
    public IValueMap getProperties() {
        return dialogSize;
    }

    protected CKEditorPanel createEditPanel(final String id, final String editorConfigJson) {
        CKEditorPanel editPanel = new CKEditorPanel(id, editorConfigJson, createEditModel());
        addAutoSaveExtension(editPanel);
        return editPanel;
    }

    protected IModel<String> createEditModel() {
        return new IModel<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void detach() {
            }

            @Override
            public String getObject() {
                return getCurrentCommentItem().getContent();
            }

            @Override
            public void setObject(String object) {
                if (autoSaveExtensionProcessPending) {
                    getCurrentCommentItem().setContent(object);
                }
            }
        };
    }

    protected CommentingContext getCommentingContext() {
        return commentingContext;
    }

    protected CommentPersistenceManager getCommentPersistenceManager() {
        return commentPersistenceManager;
    }

    protected CommentItem getOriginalCommentItem() {
        return originalCommentItem;
    }

    protected CommentItem getCurrentCommentItem() {
        return currentCommentItem;
    }

    protected SerializableCallable<Object> getOnOkCallback() {
        return onOkCallback;
    }

    private void addAutoSaveExtension(final CKEditorPanel editPanel) {
        final AutoSaveBehavior autoSaveBehavior = new AutoSaveBehavior(editPanel.getEditorModel()) {
            @Override
            protected void respond(final AjaxRequestTarget target) {
                try {
                    autoSaveExtensionProcessPending = true;
                    super.respond(target);
                } finally {
                    autoSaveExtensionProcessPending = false;
                }
            }
        };

        final CKEditorPanelAutoSaveExtension autoSaveExtension = new CKEditorPanelAutoSaveExtension(autoSaveBehavior);
        editPanel.addExtension(autoSaveExtension);
    }

    private String createEditorConfiguration(final String editorConfigJson) {
        try {
            JSONObject editorConfig = new JSONObject(editorConfigJson);
            logEditorConfiguration("Commenting CKEditor config", editorConfig);
            return editorConfig.toString();
        } catch (JSONException e) {
            log.warn("Error while creating CKEditor configuration, using default configuration as-is:\n" + editorConfigJson, e);
            return editorConfigJson;
        }
    }

    private void logEditorConfiguration(String name, JSONObject config) throws JSONException {
        if (log.isDebugEnabled()) {
            log.debug(name + "\n" + config.toString(2));
        }
    }
}
