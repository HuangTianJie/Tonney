package com.gop.c2c.normaltransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gop.c2c.dto.C2cComplaintOrderDto;
import com.gop.c2c.service.C2cTransOrderComplaintService;
import com.gop.domain.C2cTransOrderComplaint;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

/**
 * 
 * @author zhushengtao
 *
 */
@RestController("C2CComplaintController")
@RequestMapping("/c2ccomplaint")
public class C2CComplaintController {
	@Autowired
	private C2cTransOrderComplaintService c2cTransOrderComplaintService;

	@RequestMapping(value = "/complaint-add", method = RequestMethod.POST)
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	public void complaintAdd(@AuthForHeader AuthContext context, @RequestBody C2cComplaintOrderDto dto) {
		Integer uid = context.getLoginSession().getUserId();
		C2cTransOrderComplaint c2cTransOrderComplaint = new C2cTransOrderComplaint();
		c2cTransOrderComplaint.setTransOrderId(dto.getTransOrderId());
		c2cTransOrderComplaint.setComplainReason(dto.getComplainReason());
		c2cTransOrderComplaint.setPayType(dto.getPayType());
		c2cTransOrderComplaint.setPayNo(dto.getPayNo());
		c2cTransOrderComplaint.setCapture(dto.getCapture());
		c2cTransOrderComplaint.setRemark(dto.getRemark());
		c2cTransOrderComplaintService.creatComplaint(c2cTransOrderComplaint, uid, dto.getPhone());
	}
}
