package com.gte.cash.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.gte.domain.cash.dto.CashUserCard;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@ApiModel(value = "card对象", description = "银行卡对象")
public class CashUserCardDto {

    @ApiModelProperty(hidden = true)
    @JSONField(serialize = false)
    private Integer id;

    @NotNull
    @ApiModelProperty(required = true, value = "户名")
    private String name;

    @NotNull
    @ApiModelProperty(required = true, value = "卡号")
    private String cardNumber;

    public CashUserCardDto(CashUserCard card) {
        this.id = card.getId();
        this.name = card.getName();
        this.cardNumber = card.getCardNumber();
    }
}
