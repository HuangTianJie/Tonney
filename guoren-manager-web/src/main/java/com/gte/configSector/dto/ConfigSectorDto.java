package com.gte.configSector.dto;

import com.gte.domain.sector.enums.Sector;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@ApiModel(value="ConfigSector对象",description="交易区配置对象")
public class ConfigSectorDto {

    @ApiModelProperty(hidden = true)
    private Integer id;

    @ApiModelProperty(required = true,value = "主版: PRIMARY; 创新版: INNOVATE;")
    private Sector sector;

    @ApiModelProperty(required = true,value = "交易币")
    private String assetCode;

}