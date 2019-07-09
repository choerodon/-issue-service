package io.choerodon.issue.api.service;

import io.choerodon.issue.api.dto.IssueTypeDTO;
import io.choerodon.issue.api.dto.IssueTypeWithStateMachineIdDTO;
import io.choerodon.issue.api.dto.ProjectConfigDetailDTO;
import io.choerodon.issue.api.dto.StatusDTO;
import io.choerodon.issue.domain.ProjectConfig;
import io.choerodon.issue.api.dto.payload.TransformDTO;

import java.util.List;
import java.util.Map;

/**
 * @author shinan.chen
 * @Date 2018/9/4
 */
public interface ProjectConfigService {

    /**
     * 创建项目方案配置
     *
     * @param projectId
     * @param schemeId
     * @param schemeType
     * @param applyType
     * @return
     */
    ProjectConfig create(Long projectId, Long schemeId, String schemeType, String applyType);

    /**
     * 获取项目配置方案信息
     *
     * @param projectId
     * @return
     */
    ProjectConfigDetailDTO queryById(Long projectId);

    /**
     * 根据项目id找到方案返回问题类型列表
     *
     * @param projectId
     * @param applyType
     * @return
     */
    List<IssueTypeDTO> queryIssueTypesByProjectId(Long projectId, String applyType);

    /**
     * 根据项目id找到方案返回问题类型列表，带问题类型对应的状态机id
     *
     * @param projectId
     * @param applyType
     * @return
     */
    List<IssueTypeWithStateMachineIdDTO> queryIssueTypesWithStateMachineIdByProjectId(Long projectId, String applyType);

    /**
     * 根据项目id找到方案返回当前状态可以转换的列表
     *
     * @param projectId
     * @param currentStatusId
     * @param issueId
     * @param
     * @param applyType
     * @return
     */
    List<TransformDTO> queryTransformsByProjectId(Long projectId, Long currentStatusId, Long issueId, Long issueTypeId, String applyType);

    /**
     * 查询项目下某个问题类型的所有状态
     *
     * @param projectId
     * @param issueTypeId
     * @param applyType
     * @return
     */
    List<StatusDTO> queryStatusByIssueTypeId(Long projectId, Long issueTypeId, String applyType);

    /**
     * 查询项目下的所有状态
     *
     * @param projectId
     * @param applyType
     * @return
     */
    List<StatusDTO> queryStatusByProjectId(Long projectId, String applyType);

    /**
     * 根据项目id找到方案返回问题类型对应的状态机
     *
     * @param projectId
     * @param applyType
     * @param issueTypeId
     * @return
     */
    Long queryStateMachineId(Long projectId, String applyType, Long issueTypeId);

    /**
     * 【敏捷】新增状态
     *
     * @param projectId
     * @param statusDTO
     * @return
     */
    StatusDTO createStatusForAgile(Long projectId, String applyType, StatusDTO statusDTO);

    /**
     * 【敏捷】校验是否能新增状态
     *
     * @param projectId
     * @return
     */
    Map<String, Object> checkCreateStatusForAgile(Long projectId, String applyType);

    /**
     * 【敏捷】校验是否能删除状态
     *
     * @param projectId
     * @return
     */
    void removeStatusForAgile(Long projectId, Long statusId, String applyType);

    /**
     * @param projectId
     * @param statusId
     * @return
     */
    Boolean checkRemoveStatusForAgile(Long projectId, Long statusId, String applyType);

    /**
     * 查询状态机关联的项目id列表
     *
     * @param organizationId
     * @param stateMachineId
     * @return
     */
    Map<String, List<Long>> queryProjectIdsMap(Long organizationId, Long stateMachineId);

    Long queryWorkFlowFirstStatus(Long projectId, String applyType, Long issueTypeId, Long organizationId);

    Map<Long, Map<Long, List<TransformDTO>>> queryTransformsMapByProjectId(Long projectId, String applyType);
}
