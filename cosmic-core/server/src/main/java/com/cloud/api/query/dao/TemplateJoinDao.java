package com.cloud.api.query.dao;

import com.cloud.api.query.vo.TemplateJoinVO;
import com.cloud.template.VirtualMachineTemplate;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDao;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.api.ResponseObject.ResponseView;
import org.apache.cloudstack.api.response.TemplateResponse;

import java.util.List;

public interface TemplateJoinDao extends GenericDao<TemplateJoinVO, Long> {

    TemplateResponse newTemplateResponse(ResponseView view, TemplateJoinVO tmpl);

    TemplateResponse newIsoResponse(TemplateJoinVO tmpl);

    TemplateResponse newUpdateResponse(TemplateJoinVO tmpl);

    TemplateResponse setTemplateResponse(ResponseView view, TemplateResponse tmplData, TemplateJoinVO tmpl);

    List<TemplateJoinVO> newTemplateView(VirtualMachineTemplate tmpl);

    List<TemplateJoinVO> newTemplateView(VirtualMachineTemplate tmpl, long zoneId, boolean readyOnly);

    List<TemplateJoinVO> searchByTemplateZonePair(Boolean showRemoved, String... pairs);

    List<TemplateJoinVO> listActiveTemplates(long storeId);

    Pair<List<TemplateJoinVO>, Integer> searchIncludingRemovedAndCount(final SearchCriteria<TemplateJoinVO> sc, final Filter filter);
}
