package io.choerodon.issue.infra.enums;

import io.choerodon.issue.infra.dto.ObjectSchemeFieldDTO;
import io.choerodon.issue.infra.dto.PageFieldDTO;
import io.choerodon.issue.infra.feign.IamFeignClient;
import io.choerodon.issue.infra.feign.vo.ProjectCategoryVO;
import io.choerodon.issue.infra.feign.vo.ProjectVO;
import io.choerodon.issue.infra.utils.SpringBeanUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @since 2019/4/2
 */
public class FieldCode {
    private FieldCode() {
    }

    public static final String ISSUE_TYPE = "issueType";
    public static final String SUMMARY = "summary";
    public static final String DESCRIPTION = "description";
    public static final String REMAINING_TIME = "remainingTime";
    public static final String STORY_POINTS = "storyPoints";
    public static final String STATUS = "status";
    public static final String PRIORITY = "priority";
    public static final String COMPONENT = "component";
    public static final String LABEL = "label";
    public static final String INFLUENCE_VERSION = "influenceVersion";
    public static final String FIX_VERSION = "fixVersion";
    public static final String EPIC = "epic";
    public static final String SPRINT = "sprint";
    public static final String EPIC_NAME = "epicName";
    public static final String REPORTER = "reporter";
    public static final String ASSIGNEE = "assignee";
    public static final String CREATION_DATE = "creationDate";
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";
    public static final String TIME_TRACE = "timeTrace";
    public static final String BENFIT_HYPOTHESIS = "benfitHypothesis";
    public static final String ACCEPTANCE_CRITERA = "acceptanceCritera";
    public static final String FEATURE_TYPE = "featureType";
    public static final String PI = "pi";

    /**
     * 项目群/项目群子项目/敏捷项目对字段的过滤
     *
     * @param organizationId
     * @param projectId
     * @param fields
     * @return
     */
    public static List<ObjectSchemeFieldDTO> objectSchemeFieldsFilter(Long organizationId, Long projectId, List<ObjectSchemeFieldDTO> fields) {
        if (projectId != null) {
            IamFeignClient iamFeignClient = SpringBeanUtil.getBean(IamFeignClient.class);
            ProjectVO project = iamFeignClient.queryProjectInfo(projectId).getBody();
            if (project != null) {
                List<String> categoryCodes = project.getCategories().stream().map(ProjectCategoryVO::getCode).collect(Collectors.toList());
                if (categoryCodes.contains(ProjectCategoryCode.PROGRAM)) {
                    //项目群
                    return fields;
                } else if (categoryCodes.contains(ProjectCategoryCode.PROGRAM_PROJECT)) {
                    //项目群子项目
                    return fields;
                } else {
                    //敏捷项目
                    return fields.stream().filter(field -> !field.getCode().equals(PI) && !field.getCode().equals(BENFIT_HYPOTHESIS) && !field.getCode().equals(ACCEPTANCE_CRITERA) && !field.getCode().equals(FEATURE_TYPE)).collect(Collectors.toList());
                }
            }
        }
        return fields;
    }

    /**
     * 项目群/项目群子项目/敏捷项目对字段的过滤
     *
     * @param organizationId
     * @param projectId
     * @param fields
     * @return
     */
    public static List<PageFieldDTO> pageFieldsFilter(Long organizationId, Long projectId, List<PageFieldDTO> fields) {
        if (projectId != null) {
            IamFeignClient iamFeignClient = SpringBeanUtil.getBean(IamFeignClient.class);
            ProjectVO project = iamFeignClient.queryProjectInfo(projectId).getBody();
            if (project != null) {
                List<String> categoryCodes = project.getCategories().stream().map(ProjectCategoryVO::getCode).collect(Collectors.toList());
                if (categoryCodes.contains(ProjectCategoryCode.PROGRAM)) {
                    //项目群
                    return fields;
                } else if (categoryCodes.contains(ProjectCategoryCode.PROGRAM_PROJECT)) {
                    //项目群子项目
                    return fields;
                } else {
                    //敏捷项目
                    return fields.stream().filter(field -> !field.getFieldCode().equals(PI) && !field.getFieldCode().equals(BENFIT_HYPOTHESIS) && !field.getFieldCode().equals(ACCEPTANCE_CRITERA) && !field.getFieldCode().equals(FEATURE_TYPE)).collect(Collectors.toList());
                }
            }
        }
        return fields;
    }
}
