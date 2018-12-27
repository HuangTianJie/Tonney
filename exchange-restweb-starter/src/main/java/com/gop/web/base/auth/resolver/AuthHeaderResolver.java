package com.gop.web.base.auth.resolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class AuthHeaderResolver implements HandlerMethodArgumentResolver {
	private static final String AUTH_HEADER = "authorization";

	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		if (parameter.hasParameterAnnotation(AuthForHeader.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		String header = webRequest.getHeader(AUTH_HEADER);
		if (!parameter.getParameterType().isAssignableFrom(AuthContext.class)) {
			return null;
		} else {
			return AuthContext.build(header, webRequest);
		}

	}

}
