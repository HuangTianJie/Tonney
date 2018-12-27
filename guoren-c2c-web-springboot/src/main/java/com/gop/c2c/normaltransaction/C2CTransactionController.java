package com.gop.c2c.normaltransaction;

import com.google.common.base.Strings;
import com.gop.c2c.dto.*;
import com.gop.c2c.facade.C2cPayFacade;
import com.gop.c2c.service.*;
import com.gop.code.consts.C2cCodeConst;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.C2cAlipayInfo;
import com.gop.domain.C2cBankInfo;
import com.gop.domain.C2cOrderPaymentDetail;
import com.gop.domain.C2cTransOrder;
import com.gop.domain.C2cWeChatInfo;
import com.gop.domain.User;
import com.gop.domain.enums.C2cPayType;
import com.gop.exception.AppException;
import com.gop.user.facade.UserFacade;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * @author zhushengtao
 *
 */

@RequestMapping("/c2ctransaction")
@RestController("C2CTransactionController")
public class C2CTransactionController {
	@Autowired
	private C2cTransOrderService c2cTransOrderService;
	@Autowired
	private C2cSellAdvertisementService c2cSellAdvertisementService;
	@Autowired
	private UserFacade userFacade;
	@Autowired
	private C2cOrderRecordService c2cOrderRecordService;
	@Autowired
	private C2cOrderPaymentDetailService c2cOrderPaymentDetailService;
	@Autowired
	private C2cPayFacade c2cPayFacade;
	@Autowired
	private C2cAlipayInfoService c2cAlipayInfoService;
	@Autowired
	private C2cBankInfoService c2cBankInfoService;
	@Autowired
	private C2cWechatService c2cWechatService;

	// 查询订单详情
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/order-detail", method = RequestMethod.GET)
	public C2cTransOrderDto orderDetail(@AuthForHeader AuthContext context,
			@RequestParam("transOrderId") String transOrderId) {
		C2cTransOrder transOrder = c2cTransOrderService.selectByTransOrderId(transOrderId);
		Integer uid = context.getLoginSession().getUserId();
		if (null == transOrder) {
			throw new AppException(C2cCodeConst.INVALID_TRANSORDER, "订单不存在");
		}
		// 校验用户是否为订单中
		if (!(uid.equals(transOrder.getBuyUid()) || uid.equals(transOrder.getSellUid()))) {
			throw new AppException(C2cCodeConst.USER_MATCH_ORDER_ERROR);
		}
		C2cTransOrderDto dto = new C2cTransOrderDto(transOrder);
		// C2cTransOrderDto dto = new C2cTransactionOrderDto(order);
		C2cOrderPaymentDetail detail = new C2cOrderPaymentDetail();
		if (C2cPayType.ALIPAY.toString().equals(transOrder.getBuyPayType())) {
			detail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(transOrder.getAdvertId(),
					C2cPayType.ALIPAY);
			C2cAlipayInfo info = c2cAlipayInfoService.selectById(detail.getPayChannelId());
			C2cAlipayInfoDto c2cAlipayInfoDto = C2cAlipayInfoDto.builder().build();
			c2cAlipayInfoDto.setC2cPayType(C2cPayType.ALIPAY);
			;
			c2cAlipayInfoDto.setUid(info.getUid());
			c2cAlipayInfoDto.setName(info.getName());
			c2cAlipayInfoDto.setAlipayNo(info.getAlipayNo());
			c2cAlipayInfoDto.setCreateDate(info.getCreateDate());
			c2cAlipayInfoDto.setUpdateDate(info.getUpdateDate());
			dto.setDto(c2cAlipayInfoDto);
		}
		if (C2cPayType.BANK.toString().equals(transOrder.getBuyPayType())) {
			detail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(transOrder.getAdvertId(),
					C2cPayType.BANK);
			C2cBankInfo info = c2cBankInfoService.selectById(detail.getPayChannelId());
			C2cBankInfoDto c2cBankInfoDto = C2cBankInfoDto.builder().build();
			c2cBankInfoDto.setAcnumber(info.getAcnumber());
			c2cBankInfoDto.setBank(info.getBank());
			c2cBankInfoDto.setC2cPayType(C2cPayType.BANK);
			c2cBankInfoDto.setSubBank(info.getSubbank());
			c2cBankInfoDto.setName(info.getName());
			dto.setDto(c2cBankInfoDto);
		}
		if (C2cPayType.WECHAT.toString().equals(transOrder.getBuyPayType())) {
			detail = c2cOrderPaymentDetailService.selectDetailByAdvertIdAndPaytype(transOrder.getAdvertId(),
					C2cPayType.WECHAT);
			C2cWeChatInfo info  = c2cWechatService.selectStatusUsingByUid(detail.getUid());
			User userinfo = userFacade.getUser(detail.getUid());
			C2cWechatpayInfoDto c2cWechatInfoDto = C2cWechatpayInfoDto.builder().build();
			c2cWechatInfoDto.setC2cPayType(C2cPayType.WECHAT);
			c2cWechatInfoDto.setCreateDate(info.getCreateDate());
			c2cWechatInfoDto.setTag(info.getTag());
			c2cWechatInfoDto.setUpdateDate(info.getUpdateDate());
			c2cWechatInfoDto.setName(userinfo.getFullname());
			dto.setDto(c2cWechatInfoDto);
		}
		return dto;
	}

	// 确定买入
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/order-ensure", method = RequestMethod.POST)
	public C2cTransactionOrderDto orderEnsure(@AuthForHeader AuthContext context,
			@RequestBody C2cTransactionOrderArgsDto dto) {
		// 验证前台参数
		Integer uid = context.getLoginSession().getUserId();

		if (null == dto || Strings.isNullOrEmpty(dto.getAdvertId()) || BigDecimal.ZERO.compareTo(dto.getNumber()) >= 0
				|| BigDecimal.ZERO.compareTo(dto.getMoney()) >= 0) {
			System.out.println("参数异常");
			throw new AppException(CommonCodeConst.FIELD_ERROR, "参数异常");
		}
		String c2cTransOrderNum = c2cTransOrderService.createTransOrder(dto.getAdvertId(), uid, dto.getNumber(),
				dto.getPayType(), dto.getRemark());
		// 返回dto
		C2cOrderPaymentDetail orderPaymentDetail = c2cOrderPaymentDetailService
				.selectDetailByAdvertIdAndPaytype(dto.getAdvertId(), dto.getPayType());
		C2cBasePayChannelDto basicPayChannelDto = c2cPayFacade.getbasicPayChannelDtoByPaymentDetail(orderPaymentDetail);
		List<C2cBasePayChannelDto> list = new ArrayList<>();
		list.add(basicPayChannelDto);
		C2cTransactionOrderDto returnDto = new C2cTransactionOrderDto();
		returnDto.setDtoList(list);
		returnDto.setC2cTransOrderNum(c2cTransOrderNum);
		returnDto.setCurrentTime(new Date());
		returnDto.setOrderCreateDate(c2cTransOrderService.selectByTransOrderId(c2cTransOrderNum).getCreateDate());
		return returnDto;
	}

	// 买家确认支付
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	@RequestMapping(value = "/order-pay", method = RequestMethod.GET)
	public void orderPay(@AuthForHeader AuthContext context, @RequestParam("transOrderId") String transOrderId) {
		C2cTransOrder C2cTransOrder = c2cTransOrderService.selectByTransOrderId(transOrderId);
		Integer operaUid = context.getLoginSession().getUserId();
		// 更新交易单
		// 状态改为为已付款
		c2cTransOrderService.updateTransOrderToPaid(transOrderId, operaUid, operaUid, "");
	}

	// 卖家确认订单并打币
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})"),
			@Strategy(authStrategy = "exe({{'checkPayPasswordStregy'}})")})
	@RequestMapping(value = "/transfor-ensure", method = RequestMethod.GET)
	public void transforEnsure(@AuthForHeader AuthContext context, @RequestParam("transOrderId") String transOrderId) {
		Integer uid = context.getLoginSession().getUserId();
		c2cTransOrderService.updateTransOrderToFinishedAndTransforCoin(transOrderId, uid, uid, "");

	}

	// 取消交易单
	@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	@RequestMapping(value = "/order-cancel", method = RequestMethod.GET)
	public void orderCancel(@AuthForHeader AuthContext context, @RequestParam("transOrderId") String transOrderId) {
		Integer uid = context.getLoginSession().getUserId();
		c2cTransOrderService.updateTransOrderToCancel(transOrderId, uid, uid, "");

	}

}
