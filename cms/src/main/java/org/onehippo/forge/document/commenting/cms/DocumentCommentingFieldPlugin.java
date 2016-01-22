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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.hippoecm.addon.workflow.ConfirmDialog;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.DialogAction;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.editor.editor.EditorForm;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.event.IObserver;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentCommentingFieldPlugin extends RenderPlugin<Node>implements IObserver {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(DocumentCommentingFieldPlugin.class);

    private static final ResourceReference ADD_ICON_REF = new PackageResourceReference(
            DocumentCommentingFieldPlugin.class, "add-small-16.png");

    private static final ResourceReference EDIT_ICON_REF = new PackageResourceReference(
            DocumentCommentingFieldPlugin.class, "edit-small-16.png");

    private static final ResourceReference DELETE_ICON_REF = new PackageResourceReference(
            DocumentCommentingFieldPlugin.class, "delete-small-16.png");

    private JcrNodeModel documentModel;

    private CommentPersistenceManager commentPersistenceManager = new JcrCommentPersistenceManager();

    private List<CommentItem> currentCommentItems = new LinkedList<>();

    private final DialogAction addDialogAction;

    public DocumentCommentingFieldPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        setOutputMarkupId(true);

        documentModel = (JcrNodeModel) getModel();

        add(new Label("doc-commenting-caption", getCaptionModel()));

        MarkupContainer commentsContainer = new WebMarkupContainer("doc-comments-container");

        addDialogAction = new DialogAction(
                createDialogFactory(commentPersistenceManager, new CommentItem(), new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        refreshCommentItems();
                        return refreshDocumentEditorWithSelectedCompounds();
                    }
                }), getDialogService());

        AjaxLink addLink = new AjaxLink("add") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                addDialogAction.execute();
            }
        };

        final Image addImage = new Image("add-image") {
            private static final long serialVersionUID = 1L;
        };

        addImage.setImageResourceReference(ADD_ICON_REF, null);
        addLink.add(addImage);
        addLink.setVisible(isEditMode());
        commentsContainer.add(addLink);

        refreshCommentItems();

        commentsContainer.add(createRefreshingView());

        add(commentsContainer);
    }

    public List<CommentItem> getCurrentCommentItems() {
        return currentCommentItems;
    }

    private void refreshCommentItems() {

        try {
            String subjectId = documentModel.getNode().getParent().getIdentifier();
            List<CommentItem> commentItems = commentPersistenceManager.getCommentItemsBySubjectId(subjectId);
            currentCommentItems.clear();
            currentCommentItems.addAll(commentItems);
        } catch (RepositoryException e) {
            log.error("Failed to refresh current comment items.", e);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new PackageResourceReference(DocumentCommentingFieldPlugin.class,
                DocumentCommentingFieldPlugin.class.getSimpleName() + ".css")));
    }

    /**
     * Returns the current context document model.
     * @return document model
     */
    protected JcrNodeModel getDocumentModel() {
        return documentModel;
    }

    protected IModel<String> getCaptionModel() {
        final String defaultCaption = new StringResourceModel("doc-commenting.caption", this, null,
                PluginConstants.DEFAULT_FIELD_CAPTION).getString();
        String caption = getPluginConfig().getString("caption", defaultCaption);
        String captionKey = caption;
        return new StringResourceModel(captionKey, this, null, caption);
    }

    private RefreshingView<? extends Serializable> createRefreshingView() {

        return new RefreshingView<Serializable>("view") {

            private static final long serialVersionUID = 1L;

            private IDataProvider<CommentItem> dataProvider = new SimpleListDataProvider<CommentItem>(
                    currentCommentItems);

            @Override
            protected Iterator getItemModels() {

                final Iterator<? extends CommentItem> baseIt = dataProvider.iterator(0, currentCommentItems.size());

                return new Iterator<IModel<CommentItem>>() {
                    public boolean hasNext() {
                        return baseIt.hasNext();
                    }

                    public IModel<CommentItem> next() {
                        return dataProvider.model(baseIt.next());
                    }

                    public void remove() {
                        baseIt.remove();
                    }
                };
            }

            @Override
            protected void populateItem(Item item) {
                final CommentItem comment = (CommentItem) item.getModelObject();

                final String curUserId = UserSession.get().getJcrSession().getUserID();

                item.add(new Label("docitem-head-text", new Model<String>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        String datePattern = "yyyy-MM-dd HH:mm:ss";
                        return comment.getAuthor() + " - " + DateFormatUtils.format(comment.getCreated(), datePattern);
                    }
                }).setEscapeModelStrings(false));

                item.add(new Label("docitem-body-text", new Model<String>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return comment.getContent();
                    }
                }).setEscapeModelStrings(false));

                if (item.getIndex() == currentCommentItems.size() - 1) {
                    item.add(new AttributeAppender("class", new Model("last"), " "));
                }

                final DialogAction editDialogAction = new DialogAction(
                        createDialogFactory(commentPersistenceManager, comment, new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        refreshCommentItems();
                        return refreshDocumentEditorWithSelectedCompounds();
                    }
                }), getDialogService());

                AjaxLink editLink = new AjaxLink("edit") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        editDialogAction.execute();
                    }
                };

                final Image editImage = new Image("edit-image") {
                    private static final long serialVersionUID = 1L;
                };

                editImage.setImageResourceReference(EDIT_ICON_REF, null);
                editLink.add(editImage);
                editLink.setVisible(isEditMode() && StringUtils.equals(comment.getAuthor(), curUserId));
                item.add(editLink);

                AjaxLink deleteLink = new AjaxLink("delete") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(final AjaxRequestTarget target) {
                        try {
                            final ConfirmDialog confirmDlg = new ConfirmDialog(new Model<String>("Confirmation"),
                                    new Model<String>("Are you sure to delete the comment?")) {
                                @Override
                                public void invokeWorkflow() throws Exception {
                                    commentPersistenceManager.deleteCommentItem(comment);
                                    currentCommentItems.remove(comment);
                                    target.add(DocumentCommentingFieldPlugin.this);
                                }
                            };
                            getDialogService().show(confirmDlg);
                        } catch (CommentingException e) {
                            log.error("Failed to delete comment.", e);
                        }
                    }
                };

                final Image deleteImage = new Image("delete-image") {
                    private static final long serialVersionUID = 1L;
                };

                deleteImage.setImageResourceReference(DELETE_ICON_REF, null);
                deleteLink.add(deleteImage);
                deleteLink.setVisible(isEditMode() && StringUtils.equals(comment.getAuthor(), curUserId));
                item.add(deleteLink);
            }
        };
    }

    protected boolean isEditMode() {
        return IEditor.Mode.EDIT.equals(IEditor.Mode.fromString(getPluginConfig().getString("mode", "view")));
    }

    protected boolean isCompareMode() {
        return IEditor.Mode.COMPARE.equals(IEditor.Mode.fromString(getPluginConfig().getString("mode", "view")));
    }

    protected IDialogService getDialogService() {
        return getPluginContext().getService(IDialogService.class.getName(), IDialogService.class);
    }

    protected AbstractDialog createDialogInstance(final CommentPersistenceManager commentPersistenceManager,
            final CommentItem commentItem, final Callable<Object> onOkCallback) {
        return new DocumentCommentingEditorDialog(getCaptionModel(), getPluginConfig(), getPluginContext(),
                documentModel, commentPersistenceManager, commentItem, onOkCallback);
    }

    protected IDialogFactory createDialogFactory(final CommentPersistenceManager commentPersistenceManager,
            final CommentItem commentItem, final Callable<Object> onOkCallback) {
        return new IDialogFactory() {
            private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog() {
                return createDialogInstance(commentPersistenceManager, commentItem, onOkCallback);
            }
        };
    }

    private Object refreshDocumentEditorWithSelectedCompounds() {
        // find the EditorForm and invoke #onModelChagned() in order to refresh the other editor form fields.
        MarkupContainer container = ((MarkupContainer) this).getParent();
        for (; container != null; container = container.getParent()) {
            if (container instanceof EditorForm) {
                ((EditorForm) container).onModelChanged();
                break;
            }
        }

        return null;
    }

}
