package io.choerodon.issue.api.service.impl;


import io.choerodon.issue.api.dto.LookupTypeDTO;
import io.choerodon.issue.api.service.LookupTypeService;
import io.choerodon.issue.infra.mapper.LookupTypeMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/09/27.
 * Email: fuqianghuang01@gmail.com
 */
@Service
public class LookupTypeServiceImpl implements LookupTypeService {

    @Autowired
    private LookupTypeMapper lookupTypeMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<LookupTypeDTO> listLookupType(Long organizationId) {
        return modelMapper.map(lookupTypeMapper.selectAll(), new TypeToken<List<LookupTypeDTO>>() {
        }.getType());
    }

}
