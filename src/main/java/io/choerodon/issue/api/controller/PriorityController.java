package io.choerodon.issue.api.controller;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.exception.CommonException;
import io.choerodon.issue.api.dto.PriorityDTO;
import io.choerodon.issue.api.service.PriorityService;
import io.choerodon.issue.api.validator.PriorityValidator;
import io.choerodon.issue.infra.utils.ParamUtils;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author cong.cheng
 * @date 2018/8/21
 */
@RestController
@RequestMapping(value = "/v1")
public class PriorityController {
    @Autowired
    private PriorityService priorityService;

    @Autowired
    private PriorityValidator priorityValidator;

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "展示页面")
    @CustomPageRequest
    @GetMapping("/organizations/{organization_id}/priority")
    public ResponseEntity<List<PriorityDTO>> selectAll(@PathVariable("organization_id") Long organizationId,
                                                       @RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String description,
                                                       @RequestParam(required = false) String colour,
                                                       @RequestParam(required = false) Boolean isDefault,
                                                       @RequestParam(required = false) String[] param) {
        PriorityDTO priorityDTO = new PriorityDTO();
        priorityDTO.setOrganizationId(organizationId);
        priorityDTO.setName(name);
        priorityDTO.setDescription(description);
        priorityDTO.setColour(colour);
        priorityDTO.setDefault(isDefault);
        return new ResponseEntity<>(priorityService.selectAll(priorityDTO, ParamUtils.arrToStr(param)), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "创建优先级")
    @PostMapping("/organizations/{organization_id}/priority")
    public ResponseEntity<PriorityDTO> create(@PathVariable("organization_id") Long organizationId, @RequestBody PriorityDTO priorityDTO) {
        priorityValidator.createValidate(priorityDTO);
        return new ResponseEntity<>(priorityService.create(organizationId, priorityDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "更新优先级")
    @PutMapping(value = "/organizations/{organization_id}/priority/{priority_id}")
    public ResponseEntity<PriorityDTO> update(@PathVariable("organization_id") Long organizationId, @PathVariable("priority_id") Long priorityId,
                                              @RequestBody @Valid PriorityDTO priorityDTO) {
        priorityDTO.setId(priorityId);
        priorityDTO.setOrganizationId(organizationId);
        priorityValidator.updateValidate(priorityDTO);
        return new ResponseEntity<>(priorityService.update(priorityDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "校验优先级名字是否未被使用")
    @GetMapping(value = "/organizations/{organization_id}/priority/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam("name") String name) {
        return Optional.ofNullable(priorityService.checkName(organizationId, name))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.priorityName.check"));
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "更新展示顺序")
    @PutMapping(value = "/organizations/{organization_id}/priority/sequence")
    public ResponseEntity<List<PriorityDTO>> updateByList(@PathVariable("organization_id") Long organizationId,
                                                          @RequestBody List<PriorityDTO> list) {

        return new ResponseEntity<>(priorityService.updateByList(list, organizationId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据组织id查询优先级,map")
    @GetMapping("/organizations/{organization_id}/priority/list")
    public ResponseEntity<Map<Long, PriorityDTO>> queryByOrganizationId(@ApiParam(value = "组织id", required = true)
                                                                        @PathVariable("organization_id") Long organizationId) {
        return Optional.ofNullable(priorityService.queryByOrganizationId(organizationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.priorityList.get"));

    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据组织id查询默认优先级")
    @GetMapping("/organizations/{organization_id}/priority/default")
    public ResponseEntity<PriorityDTO> queryDefaultByOrganizationId(@ApiParam(value = "组织id", required = true)
                                                                    @PathVariable("organization_id") Long organizationId) {
        return Optional.ofNullable(priorityService.queryDefaultByOrganizationId(organizationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.priority.get"));

    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据组织id查询优先级,list")
    @GetMapping("/organizations/{organization_id}/priority/list_by_org")
    public ResponseEntity<List<PriorityDTO>> queryByOrganizationIdList(@ApiParam(value = "组织id", required = true)
                                                                       @PathVariable("organization_id") Long organizationId) {
        return Optional.ofNullable(priorityService.queryByOrganizationIdList(organizationId))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.priorityList.get"));
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据id查询优先级")
    @GetMapping("/organizations/{organization_id}/priority/{id}")
    public ResponseEntity<PriorityDTO> queryById(@ApiParam(value = "组织id", required = true)
                                                 @PathVariable("organization_id") Long organizationId,
                                                 @ApiParam(value = "id", required = true)
                                                 @PathVariable Long id) {
        return Optional.ofNullable(priorityService.queryById(organizationId, id))
                .map(result -> new ResponseEntity<>(result, HttpStatus.CREATED))
                .orElseThrow(() -> new CommonException("error.priority.get"));
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "生效/失效优先级")
    @GetMapping("/organizations/{organization_id}/priority/enable/{id}")
    public ResponseEntity<PriorityDTO> enablePriority(@PathVariable("organization_id") Long organizationId,
                                                      @ApiParam(value = "id", required = true)
                                                      @PathVariable Long id,
                                                      @RequestParam(required = false) Boolean enable) {
        return new ResponseEntity<>(priorityService.enablePriority(organizationId, id, enable), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "校验删除优先级")
    @GetMapping("/organizations/{organization_id}/priority/check_delete/{id}")
    public ResponseEntity<Long> checkDelete(@PathVariable("organization_id") Long organizationId,
                                            @ApiParam(value = "id", required = true)
                                            @PathVariable Long id) {
        return new ResponseEntity<>(priorityService.checkDelete(organizationId, id), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "删除优先级")
    @DeleteMapping("/organizations/{organization_id}/priority/delete/{id}")
    public ResponseEntity delete(@PathVariable("organization_id") Long organizationId, @PathVariable("id") Long priorityId,
                                 @RequestParam(required = false) Long changePriorityId) {
        priorityService.delete(organizationId, priorityId, changePriorityId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
