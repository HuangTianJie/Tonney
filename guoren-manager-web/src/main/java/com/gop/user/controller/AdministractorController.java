package com.gop.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.gop.Swagger2;
import com.gop.code.consts.CommonCodeConst;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.domain.Administrators;
import com.gop.domain.ManagerOperLog;
import com.gop.domain.enums.LockStatus;
import com.gop.domain.enums.ManagerOperType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.dto.*;
import com.gop.user.facade.AdministratorsFacade;
import com.gop.user.service.AdministractorService;
import com.gop.user.service.ManagerGoogleCodeConfigService;
import com.gop.user.service.ManagerOperLogService;
import com.gop.user.service.ManagerPasswordOperRecordService;
import com.gop.util.TokenHelper;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gop.web.base.model.LoginSession;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/admin")
@RestController("managerAdministractorController")
@Api(value="admin", description = "管理员管理模块")
public class AdministractorController {
	@Autowired
	TokenHelper tokenHelper;

	@Autowired
	private AdministratorsFacade administratorsFacade;

	@Autowired
	private AdministractorService administractorService;
	
	@Autowired
	private ManagerOperLogService managerOperLogService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private ManagerGoogleCodeConfigService managerGoogleCodeConfigService;
	
	@Autowired
	private ManagerPasswordOperRecordService managerPasswordOperRecordService;


	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
													 @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/login-password", method = RequestMethod.POST)
	// @ApiOperation("修改用户登录密码")
	public void setLoginPassword(@AuthForHeader AuthContext context,
			@Valid @RequestBody AdministractorLoginPasswordDto dto) {
		int adminId = context.getLoginSession().getUserId();
		administractorService.updatePassword(adminId, dto.getLoginPassword(), null);
	}
	
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/reset-login-password", method = RequestMethod.POST)
	public void reSetLoginPassword(@AuthForHeader AuthContext context,
			@Valid @RequestBody AdministractorResetLoginPasswordDto dto) {
		administractorService.resetLoginPassword(dto.getAdminId(), dto.getLoginPassword(), null);
	}

	/**
	 * 用户登录
	 * 
	 * Description: <br/>
	 * 
	 * @Author：zhangxianglong
	 * @date 2016年3月18日下午6:51:58
	 * @return
	 * @see
	 */
	@ApiOperation(value="管理员登录", notes="根据用户id修改密码")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="query", name = "brokerId", value = "用户ID", required = true, dataType = "String",defaultValue="10003"  ),
			@ApiImplicitParam(paramType="query", name = "account", value = "account", required = true, dataType = "String",defaultValue="15956986771"  ),
			@ApiImplicitParam(paramType="header", name = "authorization", value = "令牌", required = true, dataType = "String",defaultValue="login-password=a123456789,account-no=15956986771,google-code="  )
	})
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public JSONObject login(@AuthForHeader AuthContext context) {
		String account = context.getUserAccount();
		Administrators administrators = administractorService.getAdministractor(account);
		if (!administratorsFacade.checkLoginPwd(account, context.getLoginPassword())) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_ERROR);
		}
		String googleCode = context.getGoogleCode();
		//校验账户谷歌验证码
		administratorsFacade.checkManagerGoogleCode(administrators.getAdminId(), administrators.getMobile(), googleCode);
		AdministratorDto dto = new AdministratorDto(administrators);

		LoginSession session = new LoginSession();
		session.setUserId(administrators.getAdminId());
		context.setLoginSession(session);
		String token = tokenHelper.generateToken(administrators.getAdminId());
		JSONObject json = new JSONObject();
		json.put("token", token);
		json.put("role", administrators.getRole().toString());
		
		boolean boundGoogle = managerGoogleCodeConfigService.countManagerGoogleCodeConfigByAdminId(administrators.getAdminId());
		json.put("boundGoogle", boundGoogle);
		boolean setPwdFlag = managerPasswordOperRecordService.countSetManagerPassword(administrators.getAdminId());
		json.put("setPwdFlag", setPwdFlag);
		
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {
			ip = "Unknow";
		}
		managerOperLogService.recordManagerOperLog(administrators.getAdminId(), ip, ManagerOperType.LOGIN, "");
		return json;
	}

	/**
	 * 用户退出登录
	 * 
	 * Description: <br/>
	 * 
	 * @Author：zhangxianglong
	 * @date 2016年3月18日下午6:51:58
	 * @return
	 * @see
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	// @ApiOperation("用户退出登录")
	public void logout(@AuthForHeader AuthContext context) {
		String ip = request.getHeader("X-Real-IP");
		if (ip == null || ip.length() == 0) {
			ip = "Unknow";
		}
		String account = context.getUserAccount();
		Administrators administrators = administractorService.getAdministractor(account);
		managerOperLogService.recordManagerOperLog(administrators.getAdminId(), ip, ManagerOperType.LOGOUT, "");
		context.setLoginSession(null);
	}

	/**
	 * 创建管理员用户
	 *
	 * @return Object
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	// @ApiOperation("创建管理员用户")
	public void create(@AuthForHeader AuthContext context,@Valid@RequestBody CreateAdministratorDto dto) {
		if (11 != dto.getAccount().length()) {
			throw new AppException(CommonCodeConst.FIELD_ERROR);
		}
		Integer adminId = 1;

		log.info("userName is : " + dto.getUserName());
		administractorService.createAdminstrator(new Administrators(), adminId, dto.getAccount(), dto.getPassword(), dto.getUserName(),
				dto.getRoleId(), null);
	}

	/**
	 * 锁定管理员
	 *
	 * @return
	 */
	// @ApiOperation("锁定管理员")
	@ApiOperation(value="锁定管理员", notes="根据用户id锁定")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType="header", name = "authorization", value = "令牌", required = true, dataType = "String",defaultValue="token="+ Swagger2.token+",isNeedPassword=false"  )
	})
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@PostMapping(value = "/lock")
	public void lockAdmin(@AuthForHeader AuthContext context, @RequestBody(required = false) LockAdministractorDto dto) {
		int adminId = context.getLoginSession().getUserId();
		if (adminId == dto.getUid()) {
			throw new AppException(SecurityCodeConst.NO_PERMISSION,"不可以锁自己");
		}
		administractorService.lockAdmin(dto.getUid(), null);
	}

	/**
	 * 解锁管理员
	 *
	 * @return
	 */
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})") })
	@RequestMapping(value = "/unlock", method = RequestMethod.POST)
	// @ApiOperation("解锁管理员")
	public void unlockAdmin(@AuthForHeader AuthContext context, @RequestBody LockAdministractorDto dto) {
		// int adminId = context.getLoginSession().getUserId();
		// Integer adminId = 1;
		administractorService.unlockAdmin(dto.getUid(), null);
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	// @ApiOperation("查询管理员列表")
	public PageModel<AdministratorDto> adminList(@AuthForHeader AuthContext context,
			@RequestParam(value = "adminId", required = false) Integer adminId,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "lockStatus", required = false) LockStatus lockStatus,
			@RequestParam("pageSize") Integer pageSize, @RequestParam("pageNo") Integer pageNo) {

		PageInfo<Administrators> pageInfo = administractorService.getAdministratorList(adminId, account, lockStatus,
				pageNo, pageSize);

		PageModel<AdministratorDto> pageMode = new PageModel<>();
		pageMode.setPageNo(pageNo);
		pageMode.setPageSize(pageSize);
		pageMode.setPageNum(pageInfo.getPages());
		pageMode.setTotal(pageInfo.getTotal());
		pageMode.setList(
				pageInfo.getList().stream().map(admin -> new AdministratorDto(admin)).collect(Collectors.toList()));

		return pageMode;
	}

	@RequestMapping(value = "/info", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public AdministratorDto info(@AuthForHeader AuthContext context) {
		String account = context.getUserAccount();
		Administrators administrators = administractorService.getAdministractor(account);
		if (!administratorsFacade.checkLoginPwd(account, context.getLoginPassword())) {
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_ERROR);
		}
		AdministratorDto dto = new AdministratorDto(administrators);

		return dto;
	}
	
	@RequestMapping(value = "/operlog-query", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<ManagerOperLog> operLogQuery(@AuthForHeader AuthContext context,
			@RequestParam("adminId") Integer adminId,@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
		List<ManagerOperLog> managerOperLogs = managerOperLogService.selectManagerOperLogByAdminId(adminId,pageNo,pageSize);
		PageInfo<ManagerOperLog> pageInfo = new PageInfo<>(managerOperLogs);
		PageModel<ManagerOperLog> pageModel = new PageModel<ManagerOperLog>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(managerOperLogs);
		return pageModel;
	}
	
	@RequestMapping(value = "/loginlog-info", method = RequestMethod.GET)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public PageModel<ManagerOperLog> loginLogInfo(@AuthForHeader AuthContext context,
			@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
		Integer adminId = context.getLoginSession().getUserId();
		List<ManagerOperLog> managerOperLogs = managerOperLogService.selectManagerOperLogByAdminId(adminId,pageNo,pageSize);
		PageInfo<ManagerOperLog> pageInfo = new PageInfo<>(managerOperLogs);
		PageModel<ManagerOperLog> pageModel = new PageModel<ManagerOperLog>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setPageSize(pageSize);
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(managerOperLogs);
		return pageModel;
	}
}
