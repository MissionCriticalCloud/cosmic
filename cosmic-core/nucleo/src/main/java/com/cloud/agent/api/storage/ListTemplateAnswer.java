//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.storage.template.TemplateProp;

import java.util.Map;

public class ListTemplateAnswer extends Answer {
    private String secUrl;
    private Map<String, TemplateProp> templateInfos;

    public ListTemplateAnswer() {
        super();
    }

    public ListTemplateAnswer(final String secUrl, final Map<String, TemplateProp> templateInfos) {
        super(null, true, "success");
        this.setSecUrl(secUrl);
        this.templateInfos = templateInfos;
    }

    public Map<String, TemplateProp> getTemplateInfo() {
        return templateInfos;
    }

    public void setTemplateInfo(final Map<String, TemplateProp> templateInfos) {
        this.templateInfos = templateInfos;
    }

    public String getSecUrl() {
        return secUrl;
    }

    public void setSecUrl(final String secUrl) {
        this.secUrl = secUrl;
    }
}
