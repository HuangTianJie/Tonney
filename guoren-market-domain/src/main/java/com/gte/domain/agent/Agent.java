package com.gte.domain.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gop.domain.enums.ExamineStatus;
import com.gop.domain.enums.LockStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * 代理商
 * 
 * @author jinsong
 *
 */
@Data
@ApiModel(value="user对象",description="用户对象user")
public class Agent{

    @JsonProperty(value="num")
    private Integer id;

	/**
	 * 管理员id
	 */
    @ApiModelProperty(example="1")
    private Integer adminId;

    /**
     * 代理商名称
     */
    @ApiModelProperty(example="ABC")
    private String agentName;

    /**
     * 营业执照
     */
    @ApiModelProperty(example="asdf/asdf/asdf/sadf.png")
    private String  businessLicence;

    /**
     * 链接
     */
    @ApiModelProperty(hidden=true)
    private  String url;

    /**
     *二维码
     */
    @ApiModelProperty(hidden=true)
    private String code;
    /**
     * 返佣规则
     */
    @ApiModelProperty(example="1")
    private Integer ruleId;

    /**
     * 结算日
     */
    @ApiModelProperty(example="1")
    private Integer day;
    /**
     * 锁定状态，0-锁定，1-未锁定
     */
    @ApiModelProperty(hidden=true)
    private LockStatus locked;

    private ExamineStatus status;
    
    /**
     * 管理员名称
     */
    @ApiModelProperty(example="代理商ABC")
    private String opName;


    @ApiModelProperty(example="12345678900")
    private String mobile;


    /**
     * 登录密码
     */
    @ApiModelProperty(example="123456")
    @JsonProperty(value="password")
    private String loginPassword;

    @ApiModelProperty(hidden=true)
    private String role;

    private Date createDate;

    @ApiModelProperty(example="127.0.0.1")
    private String createip;

    private Date updateDate;

    @ApiModelProperty(example="127.0.0.1")
    private String updateip;

    private Integer createAdminId;

}
