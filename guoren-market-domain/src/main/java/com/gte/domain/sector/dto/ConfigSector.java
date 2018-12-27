package com.gte.domain.sector.dto;

import com.gte.domain.sector.enums.Sector;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigSector {

    private Integer id;

    private Sector sector;

    private String assetCode;

}