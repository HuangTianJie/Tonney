package com.gte.cash.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.gop.asset.dto.AssetOperationDto;
import com.gop.asset.facade.UserAccountFacade;
import com.gop.code.consts.CommonCodeConst;
import com.gop.exception.AppException;
import com.gop.financecheck.enums.AccountClass;
import com.gop.financecheck.enums.AccountSubject;
import com.gop.financecheck.enums.BusinessSubject;
import com.gop.mode.vo.PageModel;
import com.gop.util.SequenceUtil;
import com.gte.cash.service.CashOrderWithdrawAuditService;
import com.gte.cash.service.CashOrderWithdrawUserFeeService;
import com.gte.cash.service.CashOrderWithdrawUserService;
import com.gte.domain.cash.dto.CashOrderWithdrawAudit;
import com.gte.domain.cash.dto.CashOrderWithdrawUser;
import com.gte.domain.cash.dto.CashOrderWithdrawUserFee;
import com.gte.domain.cash.enums.WithdrawStatus;
import com.gte.mapper.cash.CashOrderWithdrawUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CashOrderWithdrawUserServiceImpl implements CashOrderWithdrawUserService {

    @Resource
    private CashOrderWithdrawUserMapper mapper;
    @Resource
    private CashOrderWithdrawUserFeeService cashOrderWithdrawUserFeeService;
    @Resource
    private CashOrderWithdrawAuditService cashOrderWithdrawAuditService;
    @Resource
    private UserAccountFacade userAccountFacade;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return mapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(CashOrderWithdrawUser record) {
        return mapper.insert(record);
    }

    @Override
    @Transactional
    public int insertSelective(CashOrderWithdrawUser record) {

        int i = mapper.insertSelective(record);
        if(i == 1){
            String requestNo = SequenceUtil.getNextId();
            ArrayList<AssetOperationDto> list = Lists.newArrayList();
            AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
            assetOperationDtoFee.setAccountClass(AccountClass.ASSET);
            assetOperationDtoFee.setAccountSubject(AccountSubject.WITHDRAW_COMMON);
            assetOperationDtoFee.setBusinessSubject(BusinessSubject.LOCK);
            assetOperationDtoFee.setAssetCode(record.getAssetCode());
            assetOperationDtoFee.setLockAmount(record.getNumber());
            assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
            assetOperationDtoFee.setAmount(record.getNumber().negate());
            assetOperationDtoFee.setUid(record.getUid());
            assetOperationDtoFee.setMemo("提现-冻结资产");
            assetOperationDtoFee.setRequestNo(requestNo);
            assetOperationDtoFee.setIndex(1);
            list.add(assetOperationDtoFee);
            userAccountFacade.assetOperation(list);
        }
        return  i;
    }


    @Override
    public CashOrderWithdrawUser selectByPrimaryKey(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(CashOrderWithdrawUser record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(CashOrderWithdrawUser record) {
        return mapper.updateByPrimaryKey(record);
    }

    @Override
    public PageModel<CashOrderWithdrawUser> selectBySelective(CashOrderWithdrawUser dao, Integer pageNo, Integer pageSize) {
        if(pageNo != null){
            PageHelper.startPage(pageNo, pageSize);
        }
        PageHelper.orderBy("id DESC");
        PageInfo<CashOrderWithdrawUser> lst = new PageInfo<>(mapper.selectBySelective(dao));

        PageModel<CashOrderWithdrawUser> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(lst.getPages());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(lst.getTotal());
        pageModel.setList(lst.getList());
        return pageModel;
    }

    @Override
    public CashOrderWithdrawUser selectByOrderNo(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }

    @Override
    @Transactional
    public int approval(CashOrderWithdrawAudit recordAudit, CashOrderWithdrawUser order, List<CashOrderWithdrawUserFee> list) {
        //更新订单状态
        CashOrderWithdrawUser.CashOrderWithdrawUserBuilder build = CashOrderWithdrawUser.builder().id(order.getId())
                .watchful(order.getWatchful()).adminId(recordAudit.getAdminId())
                .version(order.getVersion())
                .status(recordAudit.getAfterStatus());
        int i = mapper.updateByPrimaryKeySelectiveOptimisticLocking(build.build());
        if(i == 1){
            if(recordAudit.getAfterStatus().equals(WithdrawStatus.SUCCESS)){
                String requestNo = SequenceUtil.getNextId();
                ArrayList<AssetOperationDto> assetOperationList = Lists.newArrayList();
                AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
                assetOperationDtoFee.setAccountClass(AccountClass.ASSET);
                assetOperationDtoFee.setAccountSubject(AccountSubject.WITHDRAW_COMMON);
                assetOperationDtoFee.setBusinessSubject(BusinessSubject.WITHDRAW);
                assetOperationDtoFee.setAssetCode(order.getAssetCode());
                assetOperationDtoFee.setLockAmount(order.getNumber().negate());
                assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
                assetOperationDtoFee.setAmount(BigDecimal.ZERO);
                assetOperationDtoFee.setUid(order.getUid());
                assetOperationDtoFee.setMemo("提现-完成");
                assetOperationDtoFee.setRequestNo(requestNo);
                assetOperationDtoFee.setIndex(1);
                assetOperationList.add(assetOperationDtoFee);
                userAccountFacade.assetOperation(assetOperationList);

            }else if(recordAudit.getAfterStatus().equals(WithdrawStatus.DEDUCT)) {
                if (list != null && list.size() > 0 && i > 0) {
                    cashOrderWithdrawUserFeeService.deleteByOrderNo(order.getOrderNo());
                    cashOrderWithdrawUserFeeService.insertBatch(list);
                }
            }else if(recordAudit.getAfterStatus().equals(WithdrawStatus.REFUSE)) {
                String requestNo = SequenceUtil.getNextId();
                ArrayList<AssetOperationDto> assetlist = Lists.newArrayList();
                AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
                assetOperationDtoFee.setAccountClass(AccountClass.ASSET);
                assetOperationDtoFee.setAccountSubject(AccountSubject.WITHDRAW_COMMON);
                assetOperationDtoFee.setBusinessSubject(BusinessSubject.UNLOCK);
                assetOperationDtoFee.setAssetCode(order.getAssetCode());
                assetOperationDtoFee.setLockAmount(order.getNumber().negate());
                assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
                assetOperationDtoFee.setAmount(order.getNumber());
                assetOperationDtoFee.setUid(order.getUid());
                assetOperationDtoFee.setMemo("提现-冻结释放");
                assetOperationDtoFee.setRequestNo(requestNo);
                assetOperationDtoFee.setIndex(1);
                assetlist.add(assetOperationDtoFee);
                userAccountFacade.assetOperation(assetlist);
            }
        }else{
            throw new AppException(CommonCodeConst.FIELD_ERROR,"操作失败，数据已过时,请刷新页面！");
        }
        cashOrderWithdrawAuditService.insertSelective(recordAudit);
        return i;
    }

}