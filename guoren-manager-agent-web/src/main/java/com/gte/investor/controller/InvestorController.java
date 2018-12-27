package com.gte.investor.controller;

import com.github.pagehelper.PageInfo;
import com.gop.mode.vo.PageModel;
import com.gop.user.service.UserLoginLogService;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.agent.service.AgentUserService;
import com.gte.domain.agent.AgentUser;
import com.gte.user.dto.UserBaseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController("investorManagerAgentController")
@RequestMapping("/agent/investor")
@Slf4j
@Api("投资人管理")
public class InvestorController {

    @Autowired
    private AgentUserService agentUserService;

    @Autowired
    private UserLoginLogService userLoginLogService;

    @Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
            @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
    @GetMapping("user-list")
    @ApiOperation("获取投资人信息列表")
    public PageModel<UserBaseDto> getUserDetail(@AuthForHeader AuthContext context,
                                                @RequestParam(value = "uid", required = false) Integer uId,
                                                @RequestParam(value = "email", required = false) String email,
                                                @RequestParam(value = "fullname", required = false) String fullname,
                                                @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startDate,
                                                @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endDate,
                                                @RequestParam(value = "sortProp", required = false) String sortProp,
                                                @RequestParam(value = "sortOrder", required = false) String sortOrder,
                                                @RequestParam("pageNo") @ApiParam(defaultValue="1") Integer pageNo,
                                                @RequestParam("pageSize") @ApiParam(defaultValue="10") Integer pageSize) {

        String orderBy = null;
        if(sortProp == null || sortOrder == null){
            orderBy = "create_date desc";
        }else{
            orderBy = sortProp+" "+sortOrder;
        }
        Integer adminId = context.getLoginSession().getUserId();
        PageInfo<AgentUser> pageInfo = agentUserService.getBaseUserListByAgent(adminId, uId, email, fullname,startDate,endDate,orderBy, pageNo, pageSize);
        PageModel<UserBaseDto> pageMode = new PageModel<>();
        pageMode.setPageNo(pageNo);
        pageMode.setPageSize(pageSize);
        pageMode.setPageNum(pageInfo.getPages());
        pageMode.setTotal(pageInfo.getTotal());

        List<UserBaseDto> lstDtos = pageInfo.getList().stream().map(user -> new UserBaseDto(
                user,
                userLoginLogService.getFirstLoginByUid(user.getUid())
        )).collect(Collectors.toList());

        pageMode.setList(lstDtos);
        return pageMode;
    }

}
