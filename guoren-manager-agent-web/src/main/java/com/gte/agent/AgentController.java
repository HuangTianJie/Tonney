package com.gte.agent;

import com.github.pagehelper.PageInfo;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.enums.LockStatus;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.service.AdministractorService;
import com.gop.user.service.RoleManagerService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.agent.dto.AgentDto;
import com.gte.agent.service.AgentService;
import com.gte.domain.agent.Agent;
import com.gte.user.dto.AdministratorDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent")
@Api(value="/agent", tags = "代理商")
@Slf4j
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AdministractorService administractorService;


    @ApiOperation(value = "查询代理商列表", notes = "查询代理商列表")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @GetMapping("/list")
    public PageModel<AgentDto> login(@AuthForHeader AuthContext context,
                                     @RequestParam("pageNo") @ApiParam(defaultValue = "1") Integer pageNo,
                                     @RequestParam("pageSize") @ApiParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Agent> pageInfo = agentService.getAgentList(pageNo, pageSize);
        PageModel<AgentDto> pageMode = new PageModel<>();
        pageMode.setPageNo(pageNo);
        pageMode.setPageSize(pageSize);
        pageMode.setPageNum(pageInfo.getPages());
        pageMode.setTotal(pageInfo.getTotal());
        pageMode.setList(
                pageInfo.getList().stream().map(admin -> new AgentDto(admin)).collect(Collectors.toList()));
        return pageMode;
    }

    @ApiOperation(value = "查询代理商详情", notes = "查询代理商详情")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @GetMapping("/info")
    public AgentDto info(@AuthForHeader AuthContext context,
                         @RequestParam("adminId") Integer adminId) {
        Agent agent =  agentService.getAgentByByAdminId(adminId);
        return agent== null ? null : new AgentDto(agent);
    }

    @ApiOperation(value = "新增代理商表", notes = "查询代理商")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping("/insert")
    public void insert(@AuthForHeader AuthContext context,
                       @RequestBody @ApiParam(name = "代理商信息", value = "传入json格式", required = true) Agent agent) {
        agent.setAdminId(null);
        if (agentService.getAgentByAccount(agent.getMobile()) == null
                || administractorService.getAdministractor(agent.getMobile()) == null) {
            if (agentService.insert(agent) != 1){
                throw new AppException(CommonCodeConst.FIELD_ERROR, "新增失败!");
            }
        } else {
            throw new AppException(CommonCodeConst.FIELD_ERROR, "手机号码已注册!");
        }

    }

    @ApiOperation(value = "修改代理商表", notes = "修改代理商")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping("/update")
    public void update(@AuthForHeader AuthContext context,
                       @RequestBody @ApiParam(name = "代理商信息", value = "传入json格式", required = true) Agent agent) {
        agent.setLocked(null);
        agent.setRuleId(null);
        if(administractorService.getAdministractor(agent.getMobile()).getAdminId() != agent.getAdminId()){
            if(agentService.update(agent) != 1){
                throw new AppException(CommonCodeConst.FIELD_ERROR, "修改失败!");
            }
        }
    }

    @ApiOperation(value = "审批代理商表", notes = "审批代理商")
    @Strategys(strategys = {@Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})")})
    @PostMapping("/approval")
    public void approval(@AuthForHeader AuthContext context,
                         @RequestParam("adminId") Integer adminId, @RequestParam("up") Integer up) {
        Agent agent = new Agent();
        agent.setAdminId(adminId);
        if (up.intValue() == 1) {
            agent.setLocked(LockStatus.UNLOCK);
        } else {
            agent.setLocked(LockStatus.LOCK);
        }
        agentService.update(agent);
    }
}
