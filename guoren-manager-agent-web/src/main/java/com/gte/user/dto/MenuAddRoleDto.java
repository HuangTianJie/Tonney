package com.gte.user.dto;

import lombok.Data;
import lombok.ToString;

/**
 * Created by wuyanjie on 2018/4/16.
 */
@Data
public class MenuAddRoleDto {
    private Integer menuId;
    private boolean hasChoose;
    public MenuAddRoleDto(Integer menuId,boolean hasChoose){
        this.menuId = menuId;
        this.hasChoose = hasChoose;
    }
}
