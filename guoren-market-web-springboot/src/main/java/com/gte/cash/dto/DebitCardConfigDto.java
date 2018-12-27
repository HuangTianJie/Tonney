package com.gte.cash.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DebitCardConfigDto {

    String remittanceWay;

    String receiveAccount;

    String receiveNumber;

    String receiveBank;

    String receiveRemark;

}
