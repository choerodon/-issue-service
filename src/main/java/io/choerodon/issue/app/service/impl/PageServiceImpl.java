package io.choerodon.issue.app.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.issue.app.service.PageService;
import io.choerodon.issue.api.vo.PageSearchVO;
import io.choerodon.issue.api.vo.PageVO;
import io.choerodon.issue.infra.dto.PageDTO;
import io.choerodon.issue.infra.mapper.PageMapper;
import io.choerodon.issue.infra.utils.PageUtil;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author shinan.chen
 * @since 2019/4/1
 */
@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private PageMapper pageMapper;

    private ModelMapper modelMapper = new ModelMapper();

    @PostConstruct
    public void init() {
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    @Override
    public PageInfo<PageVO> pageQuery(Long organizationId, PageRequest pageRequest, PageSearchVO searchDTO) {
        PageInfo<PageDTO> page = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(),
                PageUtil.sortToSql(pageRequest.getSort())).doSelectPageInfo(() -> pageMapper.fulltextSearch(organizationId, searchDTO));
        return PageUtil.buildPageInfoWithPageInfoList(page,
                modelMapper.map(page.getList(), new TypeToken<List<PageVO>>() {
                }.getType()));
    }
}
