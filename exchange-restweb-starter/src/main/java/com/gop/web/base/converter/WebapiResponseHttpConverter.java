package com.gop.web.base.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.springfox.SwaggerJsonSerializer;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.gop.code.consts.CommonCodeConst;
import com.gop.conetxt.WebApiResponseFactory;
import com.gop.web.base.model.WebApiResponse;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.UiConfiguration;

public class WebapiResponseHttpConverter extends FastJsonHttpMessageConverter {

	private WebApiResponseFactory webApiResponseFactory;

	public WebApiResponseFactory getWebApiResponseFactory() {
		return webApiResponseFactory;
	}

	public void setWebApiResponseFactory(WebApiResponseFactory webApiResponseFactory) {
		this.webApiResponseFactory = webApiResponseFactory;
	}

	@Override
	protected void writeInternal(Object obj, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		HttpHeaders headers = outputMessage.getHeaders();
		ByteArrayOutputStream outnew = new ByteArrayOutputStream();
		FastJsonConfig fastjsonConfig = getFastJsonConfig();
		fastjsonConfig.getSerializeConfig().put(Json.class, SwaggerJsonSerializer.instance);
		int writeAsToString = 0;
		if (obj != null) {
			String className = obj.getClass().getName();
			if ("com.fasterxml.jackson.databind.node.ObjectNode".equals(className)) {
				writeAsToString = 1;
			}
			//ADD swagger
			if (/*"springfox.documentation.swagger.web.UiConfiguration".equals(className) ||
				"springfox.documentation.spring.web.json.Json".equals(className)||*/
					className.startsWith("springfox.documentation")
				) {
				writeAsToString = 2;
			}
			List list;
			if("java.util.ArrayList".equals(className) && (list = (List) (obj)).size() == 1 ){
				if(/*"springfox.documentation.swagger.web.SwaggerResource".equals(list.get(0).getClass().getName())*/
						list.get(0).getClass().getName().startsWith("springfox.documentation")
						){
					writeAsToString = 2;
				}
			}
		}

		if (writeAsToString == 1) {
			String text = obj.toString();
			OutputStream out = outputMessage.getBody();
			out.write(text.getBytes());
			if (fastjsonConfig.isWriteContentLength()) {
				headers.setContentLength(text.length());
			}
		}else //ADD swagger
			if(writeAsToString == 2){
			int len = JSON.writeJSONString(outnew, //
					fastjsonConfig.getCharset(), //
					obj, //
					fastjsonConfig.getSerializeConfig(), //
					fastjsonConfig.getSerializeFilters(), //
					fastjsonConfig.getDateFormat(), //
					JSON.DEFAULT_GENERATE_FEATURE, //
					fastjsonConfig.getSerializerFeatures());
			if (fastjsonConfig.isWriteContentLength()) {
				headers.setContentLength(len);
			}

			OutputStream out = outputMessage.getBody();
			outnew.writeTo(out);
		} else {
			if (!(obj instanceof WebApiResponse)) {
				Locale locale = LocaleContextHolder.getLocale();
				obj = webApiResponseFactory.get(CommonCodeConst.SERIVCE_SUCCESS, null, obj, locale);
			}
			int len = JSON.writeJSONString(outnew, //
					fastjsonConfig.getCharset(), //
					obj, //
					fastjsonConfig.getSerializeConfig(), //
					fastjsonConfig.getSerializeFilters(), //
					fastjsonConfig.getDateFormat(), //
					JSON.DEFAULT_GENERATE_FEATURE, //
					fastjsonConfig.getSerializerFeatures());
			if (fastjsonConfig.isWriteContentLength()) {
				headers.setContentLength(len);
			}

			OutputStream out = outputMessage.getBody();
			outnew.writeTo(out);
		}

		outnew.close();

	}

}
