package com.gop.c2c.normaltransaction;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.gop.c2c.dto.C2cAlipayInfoDto;
import com.gop.c2c.dto.C2cBankInfoDto;
import com.gop.c2c.dto.C2cBasePayChannelDto;
import com.gop.c2c.dto.C2cTransOrderComplaintDetailDto;
import com.gop.c2c.dto.C2cWechatpayInfoDto;
import com.gop.c2c.service.C2cAlipayInfoService;
import com.gop.c2c.service.C2cBankInfoService;
import com.gop.c2c.service.C2cOrderPaymentDetailService;
import com.gop.c2c.service.C2cTransOrderComplaintService;
import com.gop.c2c.service.C2cTransOrderService;
import com.gop.c2c.service.C2cWechatService;
import com.gop.code.consts.C2cCodeConst;
import com.gop.domain.C2cAlipayInfo;
import com.gop.domain.C2cBankInfo;
import com.gop.domain.C2cOrderPaymentDetail;
import com.gop.domain.C2cTransOrder;
import com.gop.domain.C2cTransOrderComplaint;
import com.gop.domain.C2cWeChatInfo;
import com.gop.domain.User;
import com.gop.domain.enums.C2cComplaintStatus;
import com.gop.domain.enums.C2cPayType;
import com.gop.domain.enums.C2cTransOrderStatus;
import com.gop.domain.enums.C2cTransType;
import com.gop.exception.AppException;
import com.gop.mode.vo.PageModel;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;

@RestController("C2CComplaintController")
@RequestMapping("/c2ccomplaint")
public class C2CComplaintController {
	@Autowired
	private C2cTransOrderComplaintService c2cTransOrderComplaintService;
	
	@Autowired
	private C2cTransOrderService c2cTransOrderService;
	
	@Autowired
	private C2cOrderPaymentDetailService c2cOrderPaymentDetailService;
	
	@Autowired
	private C2cAlipayInfoService c2cAlipayInfoService;
	
	@Autowired
	private C2cBankInfoService c2cBankInfoService;
	
	@Autowired
	private C2cWechatService c2cWechatService;
	@Autowired
	private UserFacade userFacade;


	// 申诉单后台管理分页查询
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/query", method = RequestMethod.GET)
	public PageModel<C2cTransOrderComplaint> complaintQuery(@AuthForHeader AuthContext context,
			@RequestParam(value = "complainType", required = false )C2cTransType complainType, @RequestParam("pageNo") Integer pageNo,@RequestParam("pageSize") Integer pageSize,
			@RequestParam("status") C2cComplaintStatus status) {
		PageInfo<C2cTransOrderComplaint> pageInfo = new PageInfo<>(c2cTransOrderComplaintService.queryByStatusAndComplainType(pageNo,pageSize,status,complainType));
		PageModel<C2cTransOrderComplaint> pageModel = new PageModel<>();
		pageModel.setPageNo(pageNo);
		pageModel.setPageSize(pageSize);
		pageModel.setPageNum(pageInfo.getPages());
		pageModel.setTotal(pageInfo.getTotal());
		pageModel.setList(pageInfo.getList());
		return pageModel;
	}

	// 后台申诉单详情查询
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public C2cTransOrderComplaintDetailDto complaintDetail(@AuthForHeader AuthContext context, @RequestParam("complainId")String complainId) {
		C2cTransOrderComplaint c2cTransOrderComplaint = c2cTransOrderComplaintService.selectByComplainId(complainId);
		if(c2cTransOrderComplaint == null) {
			throw new AppException(C2cCodeConst.TRANSORDER_STATUS_NO_COMPLAINT);
		}
		C2cTransOrder c2cTransOrder = c2cTransOrderService.selectByTransOrderId(c2cTransOrderComplaint.getTransOrderId());
		C2cTransOrderComplaintDetailDto dto = C2cTransOrderComplaintDetailDto.builder()
				.complainId(c2cTransOrderComplaint.getComplainId())
				.orderId(c2cTransOrderComplaint.getOrderId())
				.transOrderId(c2cTransOrderComplaint.getTransOrderId())
				.uid(c2cTransOrderComplaint.getUid())
				.complainType(c2cTransOrderComplaint.getComplainType())
				.complainReason(c2cTransOrderComplaint.getComplainReason())
				.buyPhone(c2cTransOrderComplaint.getBuyPhone())
				.sellPhone(c2cTransOrderComplaint.getSellPhone())
				.payType(c2cTransOrderComplaint.getPayType())
				.payNo(c2cTransOrderComplaint.getPayNo())
				.capture(c2cTransOrderComplaint.getCapture())
				.remark(c2cTransOrderComplaint.getRemark())
				.status(c2cTransOrderComplaint.getStatus())
				.transOrderStatus(c2cTransOrderComplaint.getTransOrderStatus())
				.operUid(c2cTransOrderComplaint.getOperUid())
				.createDate(c2cTransOrderComplaint.getCreateDate())
				.updateDate(c2cTransOrderComplaint.getUpdateDate())
				.money(c2cTransOrder.getMoney())
				.number(c2cTransOrder.getNumber())
				.transPayType(c2cTransOrder.getBuyPayType())
				.assetCode(c2cTransOrder.getAssetCode())
				.build();
		if (C2cPayType.ALIPAY.toString().equals(c2cTransOrder.getBuyPayType())) {
			C2cOrderPaymentDetail c2cOrderPaymentDetail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(c2cTransOrder.getAdvertId(),C2cPayType.ALIPAY);
			C2cAlipayInfo c2cAlipayInfo = c2cAlipayInfoService.selectById(c2cOrderPaymentDetail.getPayChannelId());
			C2cAlipayInfoDto c2cAlipayInfoDto = C2cAlipayInfoDto.builder()
					.uid(c2cAlipayInfo.getUid())
					.alipayNo(c2cAlipayInfo.getAlipayNo())
					.name(c2cAlipayInfo.getName())
					.createDate(c2cAlipayInfo.getCreateDate())
					.updateDate(c2cAlipayInfo.getUpdateDate())
					.build();
			c2cAlipayInfoDto.setC2cPayType(C2cPayType.ALIPAY);
			dto.setC2cBasePayChannelDto(c2cAlipayInfoDto);
		}else if (C2cPayType.BANK.toString().equals(c2cTransOrder.getBuyPayType())) {
			C2cOrderPaymentDetail c2cOrderPaymentDetail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(c2cTransOrder.getAdvertId(),C2cPayType.BANK);
			C2cBankInfo c2cBankInfo = c2cBankInfoService.selectByUid(c2cOrderPaymentDetail.getPayChannelId());
			C2cBankInfoDto c2cBankInfoDto = C2cBankInfoDto.builder()
					.bank(c2cBankInfo.getBank())
					.subBank(c2cBankInfo.getSubbank())
					.acnumber(c2cBankInfo.getAcnumber())
					.name(c2cBankInfo.getName())
					.build();
			c2cBankInfoDto.setC2cPayType(C2cPayType.BANK);
			dto.setC2cBasePayChannelDto(c2cBankInfoDto);
		}else if(C2cPayType.WECHAT.toString().equals(c2cTransOrder.getBuyPayType())){
			C2cOrderPaymentDetail c2cOrderPaymentDetail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(c2cTransOrder.getAdvertId(),C2cPayType.WECHAT);
			C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(c2cOrderPaymentDetail.getUid());
			User userinfo = userFacade.getUser(c2cOrderPaymentDetail.getUid());
			C2cWechatpayInfoDto c2cWechatInfoDto = C2cWechatpayInfoDto.builder().tag(info.getTag()).uid(info.getUid()).name(userinfo.getFullname()).build();
			c2cWechatInfoDto.setC2cPayType(C2cPayType.WECHAT);
			dto.setC2cBasePayChannelDto(c2cWechatInfoDto);
		}
		return dto;
	}

	// 后台申诉单强制打币
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkLoginPasswordStrategy'}})")})
	@RequestMapping(value = "/transfor-force", method = RequestMethod.GET)
	public void coinTransfor(@AuthForHeader AuthContext context, @RequestParam("complainId")String complainId) {
		Integer managerUid = context.getLoginSession().getUserId();

		c2cTransOrderComplaintService.forceTransforCoin(complainId, managerUid);

	}
	//后台申诉强制关闭订单
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'CheckUserRoleStrategy'}})"),
		@Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkedLoginPasswordStrategy'}})")})
	@RequestMapping(value = "/close-force", method = RequestMethod.GET)
	public void complaintClose(@AuthForHeader AuthContext context,  @RequestParam("complainId")String complainId) {
		Integer managerUid = context.getLoginSession().getUserId();
		c2cTransOrderComplaintService.forceCloseComplaint(complainId, managerUid);
	}
}
