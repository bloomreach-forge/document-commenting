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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

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
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.DialogAction;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.event.IObserver;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentCommentingFieldPlugin extends RenderPlugin<Node> implements IObserver {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(DocumentCommentingFieldPlugin.class);

    private static final ResourceReference ADD_ICON_REF =
            new PackageResourceReference(DocumentCommentingFieldPlugin.class, "add-small-16.png");

    private static final ResourceReference EDIT_ICON_REF =
            new PackageResourceReference(DocumentCommentingFieldPlugin.class, "edit-small-16.png");

    private static final ResourceReference DELETE_ICON_REF =
            new PackageResourceReference(DocumentCommentingFieldPlugin.class, "delete-small-16.png");

    private JcrNodeModel documentModel;

    public DocumentCommentingFieldPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        setOutputMarkupId(true);

        documentModel = (JcrNodeModel) getModel();

        add(new Label("doc-commenting-caption", getCaptionModel()));

        MarkupContainer commentsContainer = new WebMarkupContainer("doc-comments-container");

        AjaxLink addLink = new AjaxLink("add") {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                new DialogAction(createDialogFactory(), getDialogService()).execute();
            }
        };

        final Image addImage = new Image("add-image") {
            private static final long serialVersionUID = 1L;
        };

        addImage.setImageResourceReference(ADD_ICON_REF, null);
        addLink.add(addImage);
        addLink.setVisible(isEditMode());
        commentsContainer.add(addLink);

        final List<CommentItem> comments = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        comments.add(createCommentItem("admin", now, "test <i>content</i> 1"));
        comments.add(createCommentItem("editor", now, "test <i>content</i> 2"));
        comments.add(createCommentItem("author", now, "test <i>content</i> 3"));
        commentsContainer.add(createRefreshingView(comments));

        add(commentsContainer);
    }

    private CommentItem createCommentItem(String author, Calendar created, String content) {
        CommentItem item = new CommentItem();
        item.setAuthor(author);
        item.setCreated(created);
        item.setContent(content);
        return item;
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
                                                              PluginConstants.DEFAULT_FIELD_CAPTION)
            .getString();
        String caption = getPluginConfig().getString("caption", defaultCaption);
        String captionKey = caption;
        return new StringResourceModel(captionKey, this, null, caption);
    }

    private RefreshingView<? extends Serializable> createRefreshingView(final List<CommentItem> commentItems) {

        return new RefreshingView<Serializable>("view") {

            private static final long serialVersionUID = 1L;

            private IDataProvider<CommentItem> dataProvider =
                new SimpleListDataProvider<CommentItem>(commentItems);

            @Override
            protected Iterator getItemModels() {

                final Iterator<? extends CommentItem> baseIt = dataProvider.iterator(0, commentItems.size());

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

                if (item.getIndex() == commentItems.size() - 1) {
                    item.add(new AttributeAppender("class", new Model("last"), " "));
                }

                AjaxLink editLink = new AjaxLink("edit") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        new DialogAction(createDialogFactory(), getDialogService()).execute();
                    }
                };

                final Image editImage = new Image("edit-image") {
                    private static final long serialVersionUID = 1L;
                };

                editImage.setImageResourceReference(EDIT_ICON_REF, null);
                editLink.add(editImage);
                editLink.setVisible(isEditMode());
                item.add(editLink);

                AjaxLink deleteLink = new AjaxLink("delete") {

                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        boolean removed = commentItems.remove(comment);

                        if (removed) {
                            target.add(DocumentCommentingFieldPlugin.this);
                        }
                    }
                };

                final Image deleteImage = new Image("delete-image") {
                    private static final long serialVersionUID = 1L;
                };

                deleteImage.setImageResourceReference(DELETE_ICON_REF, null);
                deleteLink.add(deleteImage);
                deleteLink.setVisible(isEditMode());
                item.add(deleteLink);
            }
        };
    }

    protected boolean isEditMode() {
        return IEditor.Mode.EDIT.equals(IEditor.Mode.fromString(getPluginConfig().getString("mode", "view")));
    }

    protected boolean isCompareMode() {
        return IEditor.Mode.COMPARE.equals(IEditor.Mode.fromString(getPluginConfig()
            .getString("mode", "view")));
    }

    protected IDialogService getDialogService() {
        return getPluginContext().getService(IDialogService.class.getName(), IDialogService.class);
    }

    protected AbstractDialog createDialogInstance() {
        return new DocumentCommentingEditorDialog(getCaptionModel(), getPluginConfig(), getPluginContext(), getModel());
    }

    protected IDialogFactory createDialogFactory() {
        return new IDialogFactory() {
            private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog() {
                return createDialogInstance();
            }
        };
    }
}
