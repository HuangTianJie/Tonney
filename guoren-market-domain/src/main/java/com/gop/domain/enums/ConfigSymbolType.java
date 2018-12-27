package com.gop.domain.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ConfigSymbolType {
	ASSETFEERATE("assetfeerate"), //页面展示的交易手续费
	ASSETBUYFEERATE("assetbuyfeerate"),//买方手续费
	ASSETSELLFEERATE("assetsellfeerate"),//卖方手续费
	ASSETBUYMINFEE("assetbuyminfee"),//买方最小手续费
	ASSETSELLMINFEE("assetsellminfee"),//卖方最小手续费
	ASSETMINFEE("assetminfee"),
	// 1.5.8.9版本添加
	PRICEPRECISION("priceprecision"), // ---价格精度
	AMOUNTPRECISION("amountprecision"), // ---数量精度
	HIGHLIGHTNO("highlightno"), // ---高亮位数
	SHOWSTATUS("showstatus");// ---前端显示状态
	private String name;

	public String getName() {
		return name;
	}

	public static ConfigSymbolType getType(String name) {
		switch (name) {
		case "assetfeerate":
			return ASSETFEERATE;
		case "assetbuyfeerate":
			return ASSETBUYFEERATE;
		case "assetsellfeerate":
			return ASSETSELLFEERATE;
		case "assetbuyminfee":
			return ASSETBUYMINFEE;
		case "assetsellminfee":
			return ASSETSELLMINFEE;
		case "assetminfee":
			return ASSETMINFEE;
		case "priceprecision":
			return PRICEPRECISION;
		case "amountprecision":
			return AMOUNTPRECISION;
		case "highlightno":
			return HIGHLIGHTNO;
		case "showstatus":
			return SHOWSTATUS;
		default:
			return null;
		}
	}

	private ConfigSymbolType() {
	}

	private ConfigSymbolType(String name) {
		this.name = name;
	}

	public boolean validateValue(String value) {
		switch (this.name) {
		case "assetfeerate":
			return validAssetCodeFeeRate(value);
		case "assetbuyfeerate":
			return validAssetCodeFeeRate(value);
		case "assetsellfeerate":
			return validAssetCodeFeeRate(value);
		case "assetbuyminfee":
			return validAssetCodeFee(value);
		case "assetsellminfee":
			return validAssetCodeFee(value);
		case "assetminfee":
			return validAssetCodeFee(value);
		case "priceprecision":
		case "amountprecision":
		case "highlightno":
			return validPrecisionOrNo(value);
		case "showstatus":
			return validShowStatus(value);
		default:
			return false;
		}
	}

	private boolean validShowStatus(String value) {
		return "ON".equalsIgnoreCase(value) || "OFF".equalsIgnoreCase(value);
	}

	private boolean validPrecisionOrNo(String value) {
		String str = value;
		// 正则表达式规则
		String regEx = "^([0-9])|(\\d*[1-9]\\d*[0-9])$";
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		// 忽略大小写的写法
		// Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		// 查找字符串中是否有匹配正则表达式的字符/字符串
		return matcher.matches();
	}

	private boolean validAssetCodeFee(String value) {
		String str = value;
		// 正则表达式规则
		String regEx = "^([01](\\.0+)?|0\\.[0-9]+)$";
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		// 忽略大小写的写法
		// Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		// 查找字符串中是否有匹配正则表达式的字符/字符串
		return matcher.matches();
	}

	private boolean validAssetCodeFeeRate(String value) {
		String str = value;
		// 正则表达式规则大于等于零小于等于1
		String regEx = "^([01](\\.0+)?|0\\.[0-9]+)|(0)$";
		// 编译正则表达式
		Pattern pattern = Pattern.compile(regEx);
		// 忽略大小写的写法
		// Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		// 查找字符串中是否有匹配正则表达式的字符/字符串
		return matcher.matches();
	}
}
