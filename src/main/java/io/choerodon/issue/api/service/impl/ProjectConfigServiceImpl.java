package io.choerodon.issue.api.service.impl;


import com.alibaba.fastjson.JSON;
import io.choerodon.asgard.saga.annotation.Saga;
import io.choerodon.asgard.saga.dto.StartInstanceDTO;
import io.choerodon.asgard.saga.feign.SagaClient;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.issue.api.dto.*;
import io.choerodon.issue.api.dto.payload.StatusPayload;
import io.choerodon.issue.api.service.*;
import io.choerodon.issue.domain.IssueType;
import io.choerodon.issue.domain.ProjectConfig;
import io.choerodon.issue.api.dto.payload.TransformDTO;
import io.choerodon.issue.api.dto.payload.TransformInfo;
import io.choerodon.issue.infra.enums.SchemeApplyType;
import io.choerodon.issue.infra.enums.SchemeType;
import io.choerodon.issue.infra.exception.RemoveStatusException;
import io.choerodon.issue.infra.mapper.IssueTypeMapper;
import io.choerodon.issue.infra.mapper.IssueTypeSchemeConfigMapper;
import io.choerodon.issue.infra.mapper.IssueTypeSchemeMapper;
import io.choerodon.issue.infra.mapper.ProjectConfigMapper;
import io.choerodon.issue.infra.utils.EnumUtil;
import io.choerodon.issue.infra.utils.ProjectUtil;
import io.choerodon.issue.statemachine.fegin.InstanceFeignClient;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author shinan.chen
 * @Date 2018/10/24
 */
@Component
@RefreshScope
public class ProjectConfigServiceImpl implements ProjectConfigService {

    private static final String AGILE_SERVICE = "agile-service";
    private static final String FLAG = "flag";
    private static final String MESSAGE = "message";
    private static final String STATEMACHINEID = "stateMachineId";
    private static final String ERROR_ISSUE_STATE_MACHINE_NOT_FOUND = "error.issueStateMachine.notFound";
    private static final String ERROR_ISSUE_STATUS_NOT_FOUND = "error.createIssue.issueStatusNotFound";
    private static final String ERROR_APPLYTYPE_ILLEGAL = "error.applyType.illegal";
    private static final String ERROR_STATEMACHINESCHEMEID_NULL = "error.stateMachineSchemeId.null";

    @Autowired
    private ProjectConfigMapper projectConfigMapper;
    @Autowired
    private IssueTypeMapper issueTypeMapper;
    @Autowired
    private IssueTypeSchemeMapper issueTypeSchemeMapper;
    @Autowired
    private IssueTypeSchemeConfigMapper issueTypeSchemeConfigMapper;
    @Autowired
    private StateMachineSchemeConfigService stateMachineSchemeConfigService;
    @Autowired
    private IssueTypeSchemeService issueTypeSchemeService;
    @Autowired
    private StateMachineSchemeService stateMachineSchemeService;
    @Autowired
    private ProjectUtil projectUtil;
    @Autowired
    private ProjectConfigService projectConfigService;
    @Autowired
    private InstanceFeignClient instanceFeignClient;
    @Autowired
    private SagaClient sagaClient;
    @Autowired
    private StatusService statusService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private StateMachineTransformService transformService;

    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public ProjectConfig create(Long projectId, Long schemeId, String schemeType, String applyType) {
        if (!EnumUtil.contain(SchemeType.class, schemeType)) {
            throw new CommonException("error.schemeType.illegal");
        }
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        ProjectConfig projectConfig = new ProjectConfig(projectId, schemeId, schemeType, applyType);
        //保证幂等性
        List<ProjectConfig> configs = projectConfigMapper.select(projectConfig);
        if (!configs.isEmpty()) {
            return configs.get(0);
        }
        int result = projectConfigMapper.insert(projectConfig);
        if (result != 1) {
            throw new CommonException("error.projectConfig.create");
        }

        //若是关联状态机方案，设置状态机方案、状态机为活跃
        if (schemeType.equals(SchemeType.STATE_MACHINE)) {
            stateMachineSchemeService.activeSchemeWithRefProjectConfig(schemeId);
        }
        return projectConfig;
    }

    @Override
    public ProjectConfigDetailDTO queryById(Long projectId) {
        Long organizationId = projectUtil.getOrganizationId(projectId);
        List<ProjectConfig> projectConfigs = projectConfigMapper.queryByProjectId(projectId);
        Map<String, List<ProjectConfig>> configMap = projectConfigs.stream().collect(Collectors.groupingBy(ProjectConfig::getSchemeType));
        ProjectConfigDetailDTO projectConfigDetailDTO = new ProjectConfigDetailDTO();
        projectConfigDetailDTO.setProjectId(projectId);
        //获取问题类型方案
        List<ProjectConfig> issueTypeSchemeConfigs = configMap.get(SchemeType.ISSUE_TYPE);
        if (issueTypeSchemeConfigs != null && !issueTypeSchemeConfigs.isEmpty()) {
            Map<String, IssueTypeSchemeDTO> issueTypeSchemeMap = new HashMap<>(issueTypeSchemeConfigs.size());
            for (ProjectConfig projectConfig : issueTypeSchemeConfigs) {
                IssueTypeSchemeDTO issueTypeSchemeDTO = issueTypeSchemeService.queryById(organizationId, projectConfig.getSchemeId());
                issueTypeSchemeMap.put(projectConfig.getApplyType(), issueTypeSchemeDTO);
            }
            projectConfigDetailDTO.setIssueTypeSchemeMap(issueTypeSchemeMap);
        }
        //获取状态机方案
        List<ProjectConfig> stateMachineSchemeConfigs = configMap.get(SchemeType.STATE_MACHINE);
        if (stateMachineSchemeConfigs != null && !stateMachineSchemeConfigs.isEmpty()) {
            Map<String, StateMachineSchemeDTO> stateMachineSchemeMap = new HashMap<>(stateMachineSchemeConfigs.size());
            for (ProjectConfig projectConfig : stateMachineSchemeConfigs) {
                StateMachineSchemeDTO stateMachineSchemeDTO = stateMachineSchemeService.querySchemeWithConfigById(false, organizationId, projectConfig.getSchemeId());
                stateMachineSchemeMap.put(projectConfig.getApplyType(), stateMachineSchemeDTO);
            }
            projectConfigDetailDTO.setStateMachineSchemeMap(stateMachineSchemeMap);
        }
        return projectConfigDetailDTO;
    }

    @Override
    public List<IssueTypeDTO> queryIssueTypesByProjectId(Long projectId, String applyType) {
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        Long organizationId = projectUtil.getOrganizationId(projectId);
        ProjectConfig projectConfig = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.ISSUE_TYPE, applyType);
        //获取问题类型方案
        if (projectConfig.getSchemeId() != null) {
            //根据方案配置表获取 问题类型
            List<IssueType> issueTypes = issueTypeMapper.queryBySchemeId(organizationId, projectConfig.getSchemeId());
            return modelMapper.map(issueTypes, new TypeToken<List<IssueTypeDTO>>() {
            }.getType());
        } else {
            throw new CommonException("error.queryIssueTypesByProjectId.issueTypeSchemeId.null");
        }
    }

    @Override
    public List<IssueTypeWithStateMachineIdDTO> queryIssueTypesWithStateMachineIdByProjectId(Long projectId, String applyType) {
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        Long organizationId = projectUtil.getOrganizationId(projectId);
        Long issueTypeSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.ISSUE_TYPE, applyType).getSchemeId();
        Long stateMachineSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType).getSchemeId();
        if (issueTypeSchemeId == null) {
            throw new CommonException("error.issueTypeSchemeId.null");
        }
        if (stateMachineSchemeId == null) {
            throw new CommonException(ERROR_STATEMACHINESCHEMEID_NULL);
        }
        //根据方案配置表获取 问题类型
        List<IssueType> issueTypes = issueTypeMapper.queryBySchemeId(organizationId, issueTypeSchemeId);
        //根据方案配置表获取 状态机与问题类型的对应关系
        List<StateMachineSchemeConfigDTO> configs = stateMachineSchemeConfigService.queryBySchemeId(false, organizationId, stateMachineSchemeId);
        Map<Long, Long> map = configs.stream().collect(Collectors.toMap(StateMachineSchemeConfigDTO::getIssueTypeId, StateMachineSchemeConfigDTO::getStateMachineId));
        Long defaultStateMachineId = stateMachineSchemeConfigService.selectDefault(false, organizationId, stateMachineSchemeId).getStateMachineId();
        List<IssueTypeWithStateMachineIdDTO> issueTypeWithStateMachineIds = modelMapper.map(issueTypes, new TypeToken<List<IssueTypeWithStateMachineIdDTO>>() {
        }.getType());
        issueTypeWithStateMachineIds.forEach(x -> {
            Long stateMachineId = map.get(x.getId());
            if (stateMachineId != null) {
                x.setStateMachineId(stateMachineId);
            } else {
                x.setStateMachineId(defaultStateMachineId);
            }
        });
        return issueTypeWithStateMachineIds;
    }

    @Override
    public List<StatusDTO> queryStatusByIssueTypeId(Long projectId, Long issueTypeId, String applyType) {
        Long organizationId = projectUtil.getOrganizationId(projectId);
        Long stateMachineSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType).getSchemeId();
        if (stateMachineSchemeId == null) {
            throw new CommonException(ERROR_STATEMACHINESCHEMEID_NULL);
        }
        //获取状态机
        Long stateMachineId = stateMachineSchemeConfigService.queryStateMachineIdBySchemeIdAndIssueTypeId(false, organizationId, stateMachineSchemeId, issueTypeId);
        return statusService.queryByStateMachineIds(organizationId, Collections.singletonList(stateMachineId));
    }

    @Override
    public List<StatusDTO> queryStatusByProjectId(Long projectId, String applyType) {
        Long organizationId = projectUtil.getOrganizationId(projectId);
        Long stateMachineSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType).getSchemeId();
        if (stateMachineSchemeId == null) {
            throw new CommonException(ERROR_STATEMACHINESCHEMEID_NULL);
        }
        //获取状态机ids
        List<Long> stateMachineIds = stateMachineSchemeConfigService.queryBySchemeId(false, organizationId, stateMachineSchemeId)
                .stream().map(StateMachineSchemeConfigDTO::getStateMachineId).collect(Collectors.toList());
        return statusService.queryByStateMachineIds(organizationId, stateMachineIds);
    }

    @Override
    public List<TransformDTO> queryTransformsByProjectId(Long projectId, Long currentStatusId, Long issueId, Long issueTypeId, String applyType) {
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        Long organizationId = projectUtil.getOrganizationId(projectId);
        ProjectConfig projectConfig = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType);
        //获取状态机方案
        if (projectConfig.getSchemeId() != null) {
            //获取状态机
            Long stateMachineId = stateMachineSchemeConfigService.queryStateMachineIdBySchemeIdAndIssueTypeId(false, organizationId, projectConfig.getSchemeId(), issueTypeId);
            //获取当前状态拥有的转换
            List<TransformInfo> transformInfos = instanceService.queryListTransform(organizationId, AGILE_SERVICE, stateMachineId, issueId, currentStatusId);
            List<TransformDTO> transformDTOS = modelMapper.map(transformInfos, new TypeToken<List<TransformDTO>>() {
            }.getType());
            //获取组织中所有状态
            List<StatusDTO> statusDTOS = statusService.queryAllStatus(organizationId);
            Map<Long, StatusDTO> statusMap = statusDTOS.stream().collect(Collectors.toMap(StatusDTO::getId, x -> x));
            transformDTOS.forEach(transformDTO -> {
                StatusDTO statusDTO = statusMap.get(transformDTO.getEndStatusId());
                transformDTO.setStatusDTO(statusDTO);
            });
            //如果转换中不包含当前状态，则添加一个self
            if (transformDTOS.stream().noneMatch(transformDTO -> currentStatusId.equals(transformDTO.getEndStatusId()))) {
                TransformDTO self = new TransformDTO();
                self.setEndStatusId(currentStatusId);
                self.setStatusDTO(statusMap.get(currentStatusId));
                transformDTOS.add(self);
            }
            return transformDTOS;
        } else {
            throw new CommonException("error.queryIssueTypesByProjectId.stateMachineSchemeId.null");
        }
    }

    @Override
    public Map<Long, Map<Long, List<TransformDTO>>> queryTransformsMapByProjectId(Long projectId, String applyType) {
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        //获取状态机方案
        Long organizationId = projectUtil.getOrganizationId(projectId);
        ProjectConfig smProjectConfig = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType);
        if (smProjectConfig.getSchemeId() == null) {
            throw new CommonException("error.queryTransformsMapByProjectId.stateMachineSchemeId.null");
        }
        List<StateMachineSchemeConfigDTO> smsConfigDTO = stateMachineSchemeConfigService.queryBySchemeId(false, organizationId, smProjectConfig.getSchemeId());
        //获取问题类型方案
        ProjectConfig itProjectConfig = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.ISSUE_TYPE, applyType);
        if (itProjectConfig.getSchemeId() == null) {
            throw new CommonException("error.queryTransformsMapByProjectId.issueTypeSchemeId.null");
        }
        List<IssueType> issueTypes = issueTypeMapper.queryBySchemeId(organizationId, itProjectConfig.getSchemeId());
        List<Long> stateMachineIds = smsConfigDTO.stream().map(StateMachineSchemeConfigDTO::getStateMachineId).collect(Collectors.toList());
        //状态机id->状态id->转换列表
        Map<Long, Map<Long, List<TransformDTO>>> statusMap = transformService.queryStatusTransformsMap(organizationId, stateMachineIds);
        Map<Long, Long> idMap = smsConfigDTO.stream().collect(Collectors.toMap(StateMachineSchemeConfigDTO::getIssueTypeId, StateMachineSchemeConfigDTO::getStateMachineId));
        //问题类型id->状态id->转换列表
        Map<Long, Map<Long, List<TransformDTO>>> resultMap = new HashMap<>(issueTypes.size());
        //获取组织所有状态
        List<StatusDTO> statusDTOS = statusService.queryAllStatus(organizationId);
        Map<Long, StatusDTO> sMap = statusDTOS.stream().collect(Collectors.toMap(StatusDTO::getId, x -> x));
        statusMap.entrySet().forEach(x -> x.getValue().entrySet().forEach(y -> y.getValue().forEach(transformDTO -> {
            StatusDTO statusDTO = sMap.get(transformDTO.getEndStatusId());
            if (statusDTO != null) {
                transformDTO.setStatusType(statusDTO.getType());
            }
        })));
        //匹配默认状态机的问题类型映射
        Long defaultStateMachineId = idMap.get(0L);
        resultMap.put(0L, statusMap.get(defaultStateMachineId));
        //匹配状态机的问题类型映射
        for (IssueType issueType : issueTypes) {
            Long stateMachineId = idMap.get(issueType.getId());
            if (stateMachineId != null) {
                resultMap.put(issueType.getId(), statusMap.get(stateMachineId));
            }
        }
        return resultMap;
    }

    @Override
    public Long queryStateMachineId(Long projectId, String applyType, Long issueTypeId) {
        if (!EnumUtil.contain(SchemeApplyType.class, applyType)) {
            throw new CommonException(ERROR_APPLYTYPE_ILLEGAL);
        }
        Long organizationId = projectUtil.getOrganizationId(projectId);
        Long issueTypeSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.ISSUE_TYPE, applyType).getSchemeId();
        Long stateMachineSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType).getSchemeId();
        if (issueTypeSchemeId == null) {
            throw new CommonException("error.queryStateMachineId.issueTypeSchemeId.null");
        }
        if (stateMachineSchemeId == null) {
            throw new CommonException("error.queryStateMachineId.getStateMachineSchemeId.null");
        }
        return stateMachineSchemeConfigService.queryStateMachineIdBySchemeIdAndIssueTypeId(false, organizationId, stateMachineSchemeId, issueTypeId);
    }

    @Override
    public StatusDTO createStatusForAgile(Long projectId, String applyType, StatusDTO statusDTO) {
        Long organizationId = projectUtil.getOrganizationId(projectId);
        statusDTO.setOrganizationId(organizationId);
        Map<String, Object> result = checkCreateStatusForAgile(projectId, applyType);
        if ((Boolean) result.get(FLAG)) {
            Long stateMachineId = (Long) result.get(STATEMACHINEID);
            statusDTO = statusService.createStatusForAgile(organizationId, stateMachineId, statusDTO);
        } else {
            throw new CommonException((String) result.get(MESSAGE));
        }
        return statusDTO;
    }

    @Override
    public Map<String, Object> checkCreateStatusForAgile(Long projectId, String applyType) {
        Map<String, Object> result = new HashMap<>(3);
        result.put(FLAG, true);
        Long organizationId = projectUtil.getOrganizationId(projectId);
        Long stateMachineSchemeId = projectConfigMapper.queryBySchemeTypeAndApplyType(projectId, SchemeType.STATE_MACHINE, applyType).getSchemeId();
        //校验状态机方案是否只关联一个项目
        ProjectConfig select = new ProjectConfig();
        select.setSchemeId(stateMachineSchemeId);
        select.setSchemeType(SchemeType.STATE_MACHINE);
        select.setApplyType(SchemeApplyType.AGILE);
        if (projectConfigMapper.select(select).size() > 1) {
            result.put(FLAG, false);
            result.put(MESSAGE, "error.stateMachineScheme.multiScheme");
            return result;
        }
        //校验状态机方案是否只有一个状态机
        if (stateMachineSchemeConfigService.queryBySchemeId(false, organizationId, stateMachineSchemeId).size() > 1) {
            result.put(FLAG, false);
            result.put(MESSAGE, "error.stateMachineScheme.multiStateMachine");
            return result;
        }
        Long stateMachineId = stateMachineSchemeConfigService.selectDefault(false, organizationId, stateMachineSchemeId).getStateMachineId();
        if (stateMachineId == null) {
            result.put(FLAG, false);
            result.put(MESSAGE, "error.stateMachineScheme.defaultStateMachineId.notNull");
            return result;
        }
        //校验这个状态机是否只关联一个方案
        List<Long> schemeIds = stateMachineSchemeConfigService.querySchemeIdsByStateMachineId(false, organizationId, stateMachineId);
        if (schemeIds.size() > 1) {
            result.put(FLAG, false);
            result.put(MESSAGE, "error.stateMachineScheme.stateMachineInMoreThanOneScheme");
            return result;
        }
        result.put(STATEMACHINEID, stateMachineId);
        return result;
    }

    @Saga(code = "agile-remove-status", description = "移除状态", inputSchemaClass = StatusPayload.class)
    @Override
    public void removeStatusForAgile(Long projectId, Long statusId, String applyType) {
        Map<String, Object> result = checkCreateStatusForAgile(projectId, applyType);
        Boolean flag = (Boolean) result.get(FLAG);
        if (flag) {
            Long stateMachineId = (Long) result.get(STATEMACHINEID);
            Long organizationId = projectUtil.getOrganizationId(projectId);
            Long initStatusId = instanceService.queryInitStatusId(organizationId, stateMachineId);
            if (statusId.equals(initStatusId)) {
                throw new CommonException("error.initStatus.illegal");
            }
            try {
                statusService.removeStatusForAgile(organizationId, stateMachineId, statusId);
                StatusPayload statusPayload = new StatusPayload();
                statusPayload.setProjectId(projectId);
                statusPayload.setStatusId(statusId);
                sagaClient.startSaga("agile-remove-status", new StartInstanceDTO(JSON.toJSONString(statusPayload), "", "", ResourceLevel.PROJECT.value(), projectId));
            } catch (Exception e) {
                throw new RemoveStatusException("error.status.remove");
            }
        } else {
            throw new RemoveStatusException((String) result.get(MESSAGE));
        }
    }

    @Override
    public Boolean checkRemoveStatusForAgile(Long projectId, Long statusId, String applyType) {
        Map<String, Object> result = checkCreateStatusForAgile(projectId, applyType);
        Boolean flag = (Boolean) result.get(FLAG);
        if (flag) {
            Long stateMachineId = (Long) result.get(STATEMACHINEID);
            Long organizationId = projectUtil.getOrganizationId(projectId);
            Long initStatusId = instanceService.queryInitStatusId(organizationId, stateMachineId);
            return !statusId.equals(initStatusId);
        } else {
            throw new RemoveStatusException((String) result.get(MESSAGE));
        }
    }

    @Override
    public Map<String, List<Long>> queryProjectIdsMap(Long organizationId, Long stateMachineId) {
        //查询状态机方案中的配置
        List<Long> schemeIds = stateMachineSchemeConfigService.querySchemeIdsByStateMachineId(false, organizationId, stateMachineId);

        if (!schemeIds.isEmpty()) {
            List<ProjectConfig> projectConfigs = projectConfigMapper.queryBySchemeIds(schemeIds, SchemeType.STATE_MACHINE);
            return projectConfigs.stream().collect(Collectors.groupingBy(ProjectConfig::getApplyType, Collectors.mapping(ProjectConfig::getProjectId, Collectors.toList())));
        }
        return Collections.emptyMap();
    }

    @Override
    public Long queryWorkFlowFirstStatus(Long projectId, String applyType, Long issueTypeId, Long organizationId) {
        Long statusMachineId = projectConfigService.queryStateMachineId(projectId, applyType, issueTypeId);
        if (statusMachineId == null) {
            throw new CommonException(ERROR_ISSUE_STATE_MACHINE_NOT_FOUND);
        }
        Long initStatusId = instanceFeignClient.queryInitStatusId(organizationId, statusMachineId).getBody();
        if (initStatusId == null) {
            throw new CommonException(ERROR_ISSUE_STATUS_NOT_FOUND);
        }
        return initStatusId;
    }
}
