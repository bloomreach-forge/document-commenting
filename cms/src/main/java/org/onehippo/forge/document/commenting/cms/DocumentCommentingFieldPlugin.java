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
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

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

    private static final ResourceReference DELETE_ICON_REF =
            new PackageResourceReference(DocumentCommentingFieldPlugin.class, "delete-small-16.png");

    private JcrNodeModel documentModel;

    public DocumentCommentingFieldPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        setOutputMarkupId(true);

        documentModel = (JcrNodeModel) getModel();

        add(new Label("doc-commenting-caption", getCaptionModel()));

        MarkupContainer commentsContainer = new WebMarkupContainer("doc-comments-container");

        final List<CommentItem> comments = new ArrayList<>();
        comments.add(new CommentItem("admin", "test content 1"));
        comments.add(new CommentItem("editor", "test content 2"));
        comments.add(new CommentItem("author", "test content 3"));
        commentsContainer.add(createRefreshingView(comments));

        add(commentsContainer);
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

                item.add(new Label("link-text", new Model<String>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String getObject() {
                        return "" + comment.getAuthor() + ", " + comment.getContent();
                    }
                }));

                if (item.getIndex() == commentItems.size() - 1) {
                    item.add(new AttributeAppender("class", new Model("last"), " "));
                }

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

    public static class CommentItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private String author;
        private String content;

        public CommentItem() {
        }

        public CommentItem(String author, String content) {
            this.author = author;
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
