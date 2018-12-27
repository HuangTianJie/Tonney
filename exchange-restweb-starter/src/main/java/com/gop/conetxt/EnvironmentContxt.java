package com.gop.conetxt;

import java.util.Locale;

import com.google.common.base.Strings;
import com.gop.common.Environment;

import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;


@Data
public class EnvironmentContxt implements Environment {

	private ApplicationConfig applicationConfig;

	private MsgFactory msgFactory;

	public String[] messages;

	public EnvironmentEnum getSystemEnvironMent() {
		if (applicationConfig == null) {
			return EnvironmentEnum.CHINA;
		}
		String environment = applicationConfig.getEnvironment();
		if (Strings.isNullOrEmpty(environment)) {
			return EnvironmentEnum.CHINA;
		} else {
			if (environment.equals(EnvironmentEnum.US.getExplian())) {
				return EnvironmentEnum.US;
			} else {
				return EnvironmentEnum.CHINA;
			}
		}
	}

	public String getMsg(String code, String... args) {
		if (msgFactory == null) {
			init();
		}
		Locale lang = LocaleContextHolder.getLocale();
		if(lang != null){
			return msgFactory.get(code, args, lang);
		}
		EnvironmentEnum environmentEnum = getSystemEnvironMent();
		if (environmentEnum.equals(EnvironmentEnum.CHINA)) {
			return msgFactory.get(code, args, Locale.CHINA);
		} else {
			return msgFactory.get(code, args, Locale.US);
		}

	}
/*	public String getTheadLoaclMsg(String code, String... args) {
		if (msgFactory == null) {
			init();
		}
		Locale lang = LocaleContextHolder.getLocale();
		return msgFactory.get(code, args, lang);
	}*/

	private synchronized void init() {
		if (msgFactory == null) {
			if (null == messages || messages.length == 0) {
				messages = new String[1];
				messages[0] = "message";
			}
			msgFactory = new MsgFactory();
			msgFactory.setBaseNames(messages);

		}
	}

}
