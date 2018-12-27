package com.gop.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.gop.code.consts.SecurityCodeConst;
import com.gop.exception.AppException;
import com.gop.util.TokenHelper;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.strategy.AuthStrategy;
import com.gop.web.base.model.LoginSession;

import lombok.extern.slf4j.Slf4j;

@Service("checkLoginStrategy")
@Slf4j
public class CheckLoginStrategy implements AuthStrategy {
	@Autowired
	private TokenHelper tokenHelper;

	@Override
	public void pre(AuthContext authContext) {

		String token = authContext.getToke();
		if (null == token) {
			log.info("null == loginSession");
			throw new AppException(SecurityCodeConst.NO_LOGIN);
		}
		LoginSession loginsession = null;
		int uid = 0;
		try {
			uid = tokenHelper.validAndGetDetailFromToken(token, Integer.class);
		} catch (Exception e) {

			throw new AppException(SecurityCodeConst.TOKE_HAS_INVALID);
		}
		loginsession = new LoginSession();
		loginsession.setUserId(uid);
		authContext.setLoginSession(loginsession);
	}

	@Override
	public boolean match(AuthContext authContext) {
		if (authContext == null) {
			return false;
		}
		String token = authContext.getToke();
		if (Strings.isNullOrEmpty(token)) {
			return false;
		}
		return true;
	}

	@Override
	public void after(AuthContext authContext, Throwable throwable) {
		// TODO Auto-generated method stub

	}

}
