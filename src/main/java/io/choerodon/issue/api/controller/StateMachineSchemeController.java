package io.choerodon.issue.api.controller;

import io.choerodon.base.annotation.Permission;
import io.choerodon.base.enums.ResourceType;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.issue.api.dto.StateMachineSchemeConfigDTO;
import io.choerodon.issue.api.dto.StateMachineSchemeDTO;
import io.choerodon.issue.api.dto.payload.StateMachineSchemeChangeItem;
import io.choerodon.issue.api.service.StateMachineSchemeConfigService;
import io.choerodon.issue.api.service.StateMachineSchemeService;
import io.choerodon.issue.api.validator.StateMachineSchemeValidator;
import io.choerodon.issue.infra.utils.ParamUtils;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.CustomPageRequest;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/v1/organizations/{organization_id}/state_machine_scheme")
public class StateMachineSchemeController {

    @Autowired
    private StateMachineSchemeService schemeService;

    @Autowired
    private StateMachineSchemeValidator schemeValidator;

    @Autowired
    private StateMachineSchemeConfigService configService;

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "查询状态机方案列表")
    @CustomPageRequest
    @GetMapping
    public ResponseEntity<Page<StateMachineSchemeDTO>> pagingQuery(@ApiIgnore
                                                                   @SortDefault(value = "id", direction = Sort.Direction.DESC) PageRequest pageRequest,
                                                                   @PathVariable("organization_id") Long organizationId,
                                                                   @RequestParam(required = false) String name,
                                                                   @RequestParam(required = false) String description,
                                                                   @RequestParam(required = false) String[] param) {
        StateMachineSchemeDTO schemeDTO = new StateMachineSchemeDTO();
        schemeDTO.setOrganizationId(organizationId);
        schemeDTO.setName(name);
        schemeDTO.setDescription(description);
        return new ResponseEntity<>(schemeService.pageQuery(organizationId, pageRequest, schemeDTO, ParamUtils.arrToStr(param)), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "创建状态机方案")
    @PostMapping
    public ResponseEntity<StateMachineSchemeDTO> create(@PathVariable("organization_id") Long organizationId, @RequestBody StateMachineSchemeDTO schemeDTO) {
        schemeValidator.createValidate(schemeDTO);
        return new ResponseEntity<>(schemeService.create(organizationId, schemeDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "更新状态机方案")
    @PutMapping(value = "/{scheme_id}")
    public ResponseEntity<StateMachineSchemeDTO> update(@PathVariable("organization_id") Long organizationId, @PathVariable("scheme_id") Long schemeId,
                                                        @RequestBody StateMachineSchemeDTO schemeDTO) {
        schemeValidator.updateValidate(schemeDTO);
        return new ResponseEntity<>(schemeService.update(organizationId, schemeId, schemeDTO), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "删除状态机方案")
    @DeleteMapping(value = "/{scheme_id}")
    public ResponseEntity<Boolean> delete(@PathVariable("organization_id") Long organizationId, @PathVariable("scheme_id") Long schemeId) {
        return new ResponseEntity<>(schemeService.delete(organizationId, schemeId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据id查询状态机方案对象")
    @GetMapping(value = "/query_scheme_with_config/{scheme_id}")
    public ResponseEntity<StateMachineSchemeDTO> querySchemeWithConfigById(@PathVariable("organization_id") Long organizationId, @PathVariable("scheme_id") Long schemeId, @RequestParam("isDraft") Boolean isDraft) {
        return new ResponseEntity<>(schemeService.querySchemeWithConfigById(isDraft, organizationId, schemeId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "创建方案配置")
    @PostMapping(value = "/create_config/{scheme_id}/{state_machine_id}")
    public ResponseEntity<StateMachineSchemeDTO> createConfig(@PathVariable("organization_id") Long organizationId, @PathVariable("scheme_id") Long schemeId,
                                                              @PathVariable("state_machine_id") Long stateMachineId,
                                                              @RequestBody List<StateMachineSchemeConfigDTO> schemeDTOs) {
        return new ResponseEntity<>(configService.create(organizationId, schemeId, stateMachineId, schemeDTOs), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "根据状态机id删除方案配置")
    @DeleteMapping(value = "/delete_config/{scheme_id}/{state_machine_id}")
    public ResponseEntity<StateMachineSchemeDTO> deleteConfig(@PathVariable("organization_id") Long organizationId,
                                                              @PathVariable("scheme_id") Long schemeId,
                                                              @PathVariable("state_machine_id") Long stateMachineId) {
        return new ResponseEntity<>(configService.delete(organizationId, schemeId, stateMachineId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "校验状态机方案名字是否未被使用")
    @GetMapping(value = "/check_name")
    public ResponseEntity<Boolean> checkName(@PathVariable("organization_id") Long organizationId,
                                             @RequestParam("name") String name) {
        return Optional.ofNullable(schemeService.checkName(organizationId, name))
                .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseThrow(() -> new CommonException("error.stateMachineSchemeName.check"));
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "校验发布状态机方案")
    @GetMapping(value = "/check_deploy/{scheme_id}")
    public ResponseEntity<List<StateMachineSchemeChangeItem>> checkDeploy(@PathVariable("organization_id") Long organizationId,
                                                                          @PathVariable("scheme_id") Long schemeId) {
        return new ResponseEntity<>(configService.checkDeploy(organizationId, schemeId), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "发布状态机方案")
    @PostMapping(value = "/deploy/{scheme_id}")
    public ResponseEntity<Boolean> deploy(@PathVariable("organization_id") Long organizationId,
                                          @PathVariable("scheme_id") Long schemeId,
                                          @RequestParam("objectVersionNumber") Long objectVersionNumber,
                                          @RequestBody List<StateMachineSchemeChangeItem> changeItems) {
        return new ResponseEntity<>(configService.deploy(organizationId, schemeId, changeItems, objectVersionNumber), HttpStatus.OK);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "删除状态机方案草稿")
    @DeleteMapping(value = "/delete_draft/{scheme_id}")
    public ResponseEntity<StateMachineSchemeDTO> deleteDraft(@PathVariable("organization_id") Long organizationId,
                                                             @PathVariable("scheme_id") Long stateMachineId) {
        return new ResponseEntity<>(configService.deleteDraft(organizationId, stateMachineId), HttpStatus.NO_CONTENT);
    }

    @Permission(type = ResourceType.ORGANIZATION)
    @ApiOperation(value = "【内部调用】更新状态机方案发布进度")
    @PutMapping(value = "/update_deploy_progress/{scheme_id}")
    public ResponseEntity<Boolean> updateDeployProgress(@PathVariable("organization_id") Long organizationId, @PathVariable("scheme_id") Long schemeId,
                                                        @RequestParam("deployProgress") Integer deployProgress) {
        return new ResponseEntity<>(schemeService.updateDeployProgress(organizationId, schemeId, deployProgress), HttpStatus.OK);
    }
}
