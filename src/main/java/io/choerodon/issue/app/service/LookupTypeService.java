package io.choerodon.issue.app.service;


import io.choerodon.issue.api.vo.LookupTypeVO;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
public interface LookupTypeService {

    List<LookupTypeVO> listLookupType(Long organizationId);
}