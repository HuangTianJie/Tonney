package com.gte.domain.cash.dto;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CashUserCard {
    /**
     * id
     */
    private Integer id;

    /**
     * 用户id
     */
    private Integer uid;

    /**
     * 户名
     */
    private String name;

    /**
     * 卡号
     */
    private String cardNumber;

    /**
     * 创建时间
     */
    private Date createDate;
}