package io.choerodon.issue.api.validator;

import io.choerodon.core.exception.CommonException;
import io.choerodon.issue.api.dto.StatusDTO;
import io.choerodon.issue.infra.enums.StatusType;
import io.choerodon.issue.infra.utils.EnumUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author peng.jiang, dinghuang123@gmail.com
 */
@Component
public class StateValidator {

    public void validate(StatusDTO statusDTO) {
        if (StringUtils.isEmpty(statusDTO.getName())) {
            throw new CommonException("error.status.name.empty");
        }
        if (StringUtils.isEmpty(statusDTO.getType())) {
            throw new CommonException("error.status.type.empty");
        }

        if (!EnumUtil.contain(StatusType.class, statusDTO.getType())) {
            throw new CommonException("error.status.type.illegal");
        }
    }
}
