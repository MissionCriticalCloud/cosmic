//

//

package com.cloud.agent.api.storage;

import com.cloud.agent.api.Answer;
import com.cloud.storage.template.TemplateProp;

import java.util.Map;

public class ListVolumeAnswer extends Answer {
    private String secUrl;
    private Map<Long, TemplateProp> templateInfos;

    public ListVolumeAnswer() {
        super();
    }

    public ListVolumeAnswer(final String secUrl, final Map<Long, TemplateProp> templateInfos) {
        super(null, true, "success");
        this.setSecUrl(secUrl);
        this.templateInfos = templateInfos;
    }

    public Map<Long, TemplateProp> getTemplateInfo() {
        return templateInfos;
    }

    public void setTemplateInfo(final Map<Long, TemplateProp> templateInfos) {
        this.templateInfos = templateInfos;
    }

    public String getSecUrl() {
        return secUrl;
    }

    public void setSecUrl(final String secUrl) {
        this.secUrl = secUrl;
    }
}
