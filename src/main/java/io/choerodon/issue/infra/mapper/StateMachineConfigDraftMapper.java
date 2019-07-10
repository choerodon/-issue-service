package io.choerodon.issue.infra.mapper;

import io.choerodon.issue.infra.dto.StateMachineConfigDraftDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineConfigDraftMapper extends Mapper<StateMachineConfigDraftDTO> {

    StateMachineConfigDraftDTO queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    List<StateMachineConfigDraftDTO> queryWithCodeInfo(@Param("organizationId") Long organizationId, @Param("transformId") Long transformId, @Param("type") String type);
}
