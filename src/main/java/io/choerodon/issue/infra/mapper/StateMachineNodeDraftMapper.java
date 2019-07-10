package io.choerodon.issue.infra.mapper;

import io.choerodon.issue.infra.dto.StateMachineNodeDraftDTO;
import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
public interface StateMachineNodeDraftMapper extends Mapper<StateMachineNodeDraftDTO> {

    StateMachineNodeDraftDTO getNodeById(@Param("nodeId") Long nodeId);

    List<StateMachineNodeDraftDTO> selectByStateMachineId(@Param("stateMachineId") Long stateMachineId);

    Long checkStateDelete(@Param("organizationId") Long organizationId, @Param("statusId") Long statusId);

    StateMachineNodeDraftDTO queryById(@Param("organizationId") Long organizationId, @Param("id") Long id);

    /**
     * 获取最大的postionY
     *
     * @param stateMachineId
     * @return
     */
    StateMachineNodeDraftDTO selectMaxPositionY(@Param("stateMachineId") Long stateMachineId);

    /**
     * 单独写更新，版本号不变，否则前端处理复杂
     */
    int updateAllStatusTransformId(@Param("organizationId") Long organizationId, @Param("id") Long id, @Param("allStatusTransformId") Long allStatusTransformId);
}
