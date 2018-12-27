package com.gop.finance.dto;

import com.gop.domain.BrokerAssetOperDetail;
import com.gop.domain.FinanceDetail;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by wuyanjie on 2018/4/25.
 */
@Data
public class BrokerAssetDto {
    Date createDate;

    BigDecimal amount;

    String assetCode;

    Integer uid;

    String account;

    Integer oper_uid;

    public BrokerAssetDto(BrokerAssetOperDetail detail, String account) {

        this.createDate = detail.getCreateDate();

        this.amount = detail.getAmount();

        this.assetCode = detail.getAssetCode();

        this.uid = detail.getUid();

        this.oper_uid = detail.getOperUid();

        this.account = account;
    }
}
