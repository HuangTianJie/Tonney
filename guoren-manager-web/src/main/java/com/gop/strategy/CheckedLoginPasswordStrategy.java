package com.gop.strategy;

import com.gop.code.consts.SecurityCodeConst;
import com.gop.code.consts.UserCodeConst;
import com.gop.exception.AppException;
import com.gop.user.facade.AdministratorsFacade;
import com.gop.util.TokenHelper;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.strategy.AuthStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 校验登录密码正确与否
 * 
 * @author liuze
 *
 */
@Service("checkedLoginPasswordStrategy")
@Slf4j
public class CheckedLoginPasswordStrategy implements AuthStrategy {

	@Autowired
	private AdministratorsFacade administratorsFacade;
	@Autowired
	private TokenHelper tokenHelper;

	@Override
	public void pre(AuthContext authContext) {
		String token = authContext.getToke();
		if (null == token) {
			throw new AppException(SecurityCodeConst.NO_LOGIN);
		}
		int uid = 0;
		try {
			uid = tokenHelper.validAndGetDetailFromToken(token, Integer.class);
		} catch (Exception e) {

			throw new AppException(SecurityCodeConst.TOKE_HAS_INVALID);
		}
		if (!"false".equals(authContext.isNeedPassword()) && !administratorsFacade.checkLoginPwd(uid, authContext.getLoginPassword())) { //
			throw new AppException(UserCodeConst.LOGIN_PASSWORD_ERROR);
		}

	}

	@Override
	public boolean match(AuthContext authContext) {
		return true;
	}

	@Override
	public void after(AuthContext authContext, Throwable throwable) {

	}

}
