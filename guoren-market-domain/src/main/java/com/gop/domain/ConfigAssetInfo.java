package com.gop.domain;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ConfigAssetInfo {
    private Integer id;

    private String assetCode;

    private String releaseTime;

    private BigDecimal usedAmount;

    private BigDecimal releaseAmount;

    private String price;

    private String info;
    
    private String infoSearch;
    
    private String description;
    
    private String usDescription;
    
    private String ftDescription;
    
    private String ftName;
    
    private String usName;
    
    private String cnName;
}