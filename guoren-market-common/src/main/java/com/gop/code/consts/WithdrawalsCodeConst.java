package com.gop.code.consts;

/**
 * 提现
 * 
 * @author wangyang
 *
 */
public class WithdrawalsCodeConst {
	// 用户等级不够
	public static final String LESS_WITHDRAWAL_LEVEL_ERROR = "107115";
	// 每日转出金额操过限制
	public static final String MAX_DAILY_WITHDRAWAL_AMOUNT_ERROR = "107102";
	// 单笔最大转出金额
	public static final String SUPER_MAX_WITHDRAWAL_CURRENCY_AMOUNT = "107103";
	// 单笔最少转出金额
	public static final String LESS_MIN_WITHDRAWAL_CURRENCY_AMOUNT = "107104";

	// 单笔最大转出coin
	public static final String SUPER_MAX_WITHDRAWAL_COIN_AMOUNT = "107105";
	// TODO
	// 单笔最少转出金coin
	public static final String LESS_MIN_WITHDRAWAL_COIN_AMOUNT = "107106";

	public static final String WITHDRAWAL_COIN_STATE_ERROR = "107107";

	public static final String WITHDRAWAL_CURRENCY_STATE_ERROR = "107108";

	public static final String WITHDRAWAL_CURRENCY_RECORD_NOT_EXIST = "108109";

	public static final String WITHDRAWAL_COIN_RECORD_NOT_EXIST = "108110";
	public static final String WITHDRAWAL_COIN_ADILY_MAX_OVER_ERROR = "107110";
	public static final String WITHDRAWAL_CURRENCY_RECORD_HAS_EXIST = "108111";

	public static final String WITHDRAWAL_COIN_RECORD_HAS_EXIST = "108112";

	public static final String LESS_MIN_WITHDRAWAL_COIN_FEE = "108113";
	public static final String WITHDRAWAL_COIN_ADDRESS_ERROR = "108114";
	//重置谷歌验证码后24小时之内不允许提币
	public static final String RESET_GOOGLE_CODE_WITHDRAW_LIMIT = "108115";

}
