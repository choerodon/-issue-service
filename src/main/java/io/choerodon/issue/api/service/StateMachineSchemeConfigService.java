package io.choerodon.issue.api.service;

import io.choerodon.issue.api.dto.StateMachineSchemeConfigDTO;
import io.choerodon.issue.api.dto.StateMachineSchemeDTO;
import io.choerodon.issue.api.dto.payload.StateMachineSchemeChangeItem;
import io.choerodon.issue.domain.StateMachineSchemeConfigDraft;
import io.choerodon.mybatis.service.BaseService;

import java.util.List;

/**
 * @author shinan.chen
 * @Date 2018/8/2
 */
public interface StateMachineSchemeConfigService extends BaseService<StateMachineSchemeConfigDraft> {

    /**
     * 根据状态机id删除配置
     *
     * @param organizationId 组织id
     * @param stateMachineId 状态机id
     * @return
     */
    StateMachineSchemeDTO delete(Long organizationId, Long schemeId, Long stateMachineId);

    /**
     * 删除状态机方案及配置
     *
     * @param organizationId
     * @param schemeId
     */
    void deleteBySchemeId(Long organizationId, Long schemeId);

    /**
     * 创建方案
     *
     * @param organizationId
     * @param schemeId
     * @param schemeDTOs
     * @return
     */
    StateMachineSchemeDTO create(Long organizationId, Long schemeId, Long stateMachineId, List<StateMachineSchemeConfigDTO> schemeDTOs);

    /**
     * 创建默认配置
     *
     * @param organizationId
     * @param schemeId
     * @param stateMachineId
     */
    void createDefaultConfig(Long organizationId, Long schemeId, Long stateMachineId);

    /**
     * 更新默认配置
     *
     * @param organizationId
     * @param schemeId
     * @param stateMachineId
     */
    void updateDefaultConfig(Long organizationId, Long schemeId, Long stateMachineId);

    /**
     * 获取默认配置
     *
     * @return
     */
    StateMachineSchemeConfigDTO selectDefault(Boolean isDraft, Long organizationId, Long schemeId);

    /**
     * 通过状态机方案id和问题类型id查询出状态机id
     *
     * @param schemeId
     * @param issueTypeId
     * @return
     */
    Long queryStateMachineIdBySchemeIdAndIssueTypeId(Boolean isDraft, Long organizationId, Long schemeId, Long issueTypeId);

    /**
     * 通过状态机方案id和状态机id查询出问题类型id
     *
     * @param isDraft
     * @param organizationId
     * @param schemeId
     * @param stateMachineId
     * @return
     */
    List<Long> queryIssueTypeIdBySchemeIdAndStateMachineId(Boolean isDraft, Long organizationId, Long schemeId, Long stateMachineId);

    /**
     * 根据方案查询配置
     *
     * @param organizationId
     * @param schemeId
     * @return
     */
    List<StateMachineSchemeConfigDTO> queryBySchemeId(Boolean isDraft, Long organizationId, Long schemeId);

    /**
     * 查询状态机关联的方案
     *
     * @return
     */
    List<Long> querySchemeIdsByStateMachineId(Boolean isDraft, Long organizationId, Long stateMachineId);

    /**
     * 发布状态机方案
     *
     * @param organizationId
     * @param schemeId
     * @return
     */
    StateMachineSchemeDTO deploy(Long organizationId, Long schemeId);

    /**
     * 发布状态机方案校验
     *
     * @param organizationId
     * @param schemeId
     * @return
     */
    List<StateMachineSchemeChangeItem> checkDeploy(Long organizationId, Long schemeId);

    /**
     * 删除状态机方案草稿配置
     *
     * @param organizationId
     * @param schemeId
     * @return
     */
    StateMachineSchemeDTO deleteDraft(Long organizationId, Long schemeId);

    /**
     * 若草稿没有数据，则修复草稿的数据
     *
     * @param organizationId
     * @param schemeId
     */
    void fixDraft(Long organizationId, Long schemeId);

    /**
     * 复制草稿配置给发布配置
     *
     * @param organizationId
     * @param schemeId
     */
    void copyDraftToDeploy(Long organizationId, Long schemeId);

}
