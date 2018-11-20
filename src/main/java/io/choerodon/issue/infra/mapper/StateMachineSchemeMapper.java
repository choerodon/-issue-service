package io.choerodon.issue.infra.mapper;

import io.choerodon.issue.domain.StateMachineScheme;
import io.choerodon.mybatis.common.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author peng.jiang@hand-china.com
 */
@Component
public interface StateMachineSchemeMapper extends BaseMapper<StateMachineScheme> {

    /**
     * 分页查询状态方案
     *
     * @param scheme 状态机方案
     * @param param 模糊查询参数
     * @return 方案列表
     */
    List<StateMachineScheme> fulltextSearch(@Param("scheme") StateMachineScheme scheme, @Param("param") String param);

    /**
     * 根据id列表查询
     *
     * @return 方案列表
     */
    List<StateMachineScheme> queryByIds(@Param("organizationId") Long organizationId, @Param("schemeIds") List<Long> schemeIds);
}
