package com.gop.domain.response;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

/**
 * 归集查询实体类
 * @author DELL
 *
 */
public class CollectionAccountResponse {
	@Getter
	@Setter
    private String assetsCode;//币种
	@Getter
	@Setter
    private BigDecimal amountAvailable;//提币账户可用余额
	@Getter
	@Setter
    private BigDecimal amountLock;//提币账户冻结余额
	@Getter
	@Setter
    private BigDecimal amountTotal;//提币账户总余额
	@Getter
	@Setter
    private BigDecimal amountChain;//提币地址余额
	@Getter
	@Setter
    private BigDecimal paltTotalAmount;//平台总资产包含粉尘金额不包含提现余额
	@Getter
	@Setter
    private BigDecimal paltTotalCollectionAmount;//平台可归集总资产不包含提现余额
}
