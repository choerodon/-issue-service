package io.choerodon.issue.infra.mapper;

import io.choerodon.issue.api.dto.IssueTypeSchemeSearchDTO;
import io.choerodon.issue.domain.IssueTypeScheme;
import io.choerodon.issue.domain.IssueTypeSchemeWithInfo;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author shinan.chen
 * @date 2018/8/10
 */
@Component
public interface IssueTypeSchemeMapper extends Mapper<IssueTypeScheme> {
    List<Long> selectIssueTypeSchemeIds(@Param("organizationId") Long organizationId, @Param("issueTypeSchemeSearchDTO") IssueTypeSchemeSearchDTO issueTypeSchemeSearchDTO);

    List<IssueTypeSchemeWithInfo> queryIssueTypeSchemeList(@Param("organizationId") Long organizationId, @Param("issueTypeSchemeIds") List<Long> issueTypeSchemeIds);
}
