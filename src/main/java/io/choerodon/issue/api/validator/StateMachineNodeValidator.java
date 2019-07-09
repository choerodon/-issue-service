package io.choerodon.issue.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.issue.api.dto.StateMachineNodeDTO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
public class StateMachineNodeValidator {

    public void createValidate(StateMachineNodeDTO nodeDTO) {
        if (StringUtils.isEmpty(nodeDTO.getStateMachineId())) {
            throw new CommonException("error.stateMachineNode.stateMachineId.empty");
        }
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStatusDTO() == null) {
            throw new CommonException("error.stateMachineNode.state.null");
        }
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStatusDTO() != null && StringUtils.isEmpty(nodeDTO.getStatusDTO().getName())) {
            throw new CommonException("error.stateMachineNode.state.name.empty");
        }
    }

    public void updateValidate(StateMachineNodeDTO nodeDTO) {
        if (StringUtils.isEmpty(nodeDTO.getStatusId()) && nodeDTO.getStatusDTO() == null) {
            throw new CommonException("error.stateMachineNode.state.null");
        }
        if (nodeDTO.getStatusDTO() != null && StringUtils.isEmpty(nodeDTO.getStatusDTO().getName())) {
            throw new CommonException("error.stateMachineNode.state.name.empty");
        }
    }
}
