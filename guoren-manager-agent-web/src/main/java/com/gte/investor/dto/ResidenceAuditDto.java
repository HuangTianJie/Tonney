package com.gte.investor.dto;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
@ToString
public class ResidenceAuditDto {
	
	@NotNull
	private Integer id;
	
	@NotBlank
	private String auditStatus;
	
	@NotBlank
	private String auditMessageId;
	
	@NotBlank
	private String auditMessage;
	
}
