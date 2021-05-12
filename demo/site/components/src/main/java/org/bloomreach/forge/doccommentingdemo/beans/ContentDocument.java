package org.bloomreach.forge.doccommentingdemo.beans;
import java.util.Calendar;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

@HippoEssentialsGenerated(internalName = "doccommentingdemo:contentdocument")
@Node(jcrType = "doccommentingdemo:contentdocument")
public class ContentDocument extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "doccommentingdemo:introduction")
    public String getIntroduction() {
        return getSingleProperty("doccommentingdemo:introduction");
    }

    @HippoEssentialsGenerated(internalName = "doccommentingdemo:title")
    public String getTitle() {
        return getSingleProperty("doccommentingdemo:title");
    }

    @HippoEssentialsGenerated(internalName = "doccommentingdemo:content")
    public HippoHtml getContent() {
        return getHippoHtml("doccommentingdemo:content");
    }

    @HippoEssentialsGenerated(internalName = "doccommentingdemo:publicationdate")
    public Calendar getPublicationDate() {
        return getSingleProperty("doccommentingdemo:publicationdate");
    }
}
