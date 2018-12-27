package com.gop.currency.transfer.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class DepositFileDto {
	
	
	//@ApiModelProperty("上传的文件")
	MultipartFile file;
	
	//@ApiModelProperty("source")
	String source;
	
	
}
