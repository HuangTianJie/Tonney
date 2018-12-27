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
import com.gte.cash.service.CashOrderDepositAuditService;
import com.gte.cash.service.CashOrderDepositUserService;
import com.gte.domain.cash.dto.CashOrderDepositAudit;
import com.gte.domain.cash.dto.CashOrderDepositUser;
import com.gte.domain.cash.enums.DepositStatus;
import com.gte.mapper.cash.CashOrderDepositUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@Slf4j
public class CashOrderDepositUserServiceImpl implements CashOrderDepositUserService {

    @Resource
    private CashOrderDepositUserMapper mapper;
    @Resource
    private CashOrderDepositAuditService cashOrderDepositAuditService;
    @Resource
    private UserAccountFacade userAccountFacade;


    @Override
    public int insertSelective(CashOrderDepositUser record) {
        return mapper.insertSelective(record);
    }

    @Override
    public CashOrderDepositUser selectByPrimaryKey(Integer id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public CashOrderDepositUser selectByOrderNo(String orderNo) {
        return mapper.selectByOrderNo(orderNo);
    }

    @Override
    public int updateByPrimaryKeySelective(CashOrderDepositUser record) {
        return mapper.updateByPrimaryKeySelective(record);
    }

    @Override
    @Transactional
    public int approval(CashOrderDepositAudit recordAudit, CashOrderDepositUser order, BigDecimal realNumber) {
        //更新订单状态
        CashOrderDepositUser.CashOrderDepositUserBuilder build = CashOrderDepositUser.builder().id(order.getId())
                .watchful(order.getWatchful()).adminId(recordAudit.getAdminId())
                .formStatus(recordAudit.getBeforeStatus())
                .version(order.getVersion())
                .status(recordAudit.getAfterStatus());
        if(realNumber != null && realNumber.compareTo(BigDecimal.ZERO) != 0){
            build.realNumber(realNumber);
        }
        int i = mapper.updateByPrimaryKeySelectiveOptimisticLocking(build.build());
        if(i == 1){
            if(recordAudit.getAfterStatus().equals(DepositStatus.SUCCESS)){
                String requestNo = SequenceUtil.getNextId();
                ArrayList<AssetOperationDto> assetOperationList = Lists.newArrayList();
                AssetOperationDto assetOperationDtoFee = new AssetOperationDto();
                assetOperationDtoFee.setAccountClass(AccountClass.ASSET);
                assetOperationDtoFee.setAccountSubject(AccountSubject.DEPOSIT_COMMON);
                assetOperationDtoFee.setBusinessSubject(BusinessSubject.DEPOSIT);
                assetOperationDtoFee.setAssetCode(order.getAssetCode());
                assetOperationDtoFee.setLockAmount(BigDecimal.ZERO);
                assetOperationDtoFee.setLoanAmount(BigDecimal.ZERO);
                assetOperationDtoFee.setAmount(order.getRealNumber());
                assetOperationDtoFee.setUid(order.getUid());
                assetOperationDtoFee.setMemo("充值-完成");
                assetOperationDtoFee.setRequestNo(requestNo);
                assetOperationDtoFee.setIndex(1);
                assetOperationList.add(assetOperationDtoFee);
                userAccountFacade.assetOperation(assetOperationList);
            }
        }else{
            throw new AppException(CommonCodeConst.FIELD_ERROR,"操作失败，数据已过时,请刷新页面！");
        }
        cashOrderDepositAuditService.insertSelective(recordAudit);
        return i;
    }

    @Override
    public PageModel<CashOrderDepositUser> selectBySelective(CashOrderDepositUser dao, Integer pageNo, Integer pageSize) {
        if(pageNo != null){
            PageHelper.startPage(pageNo, pageSize);
        }
        PageHelper.orderBy("id DESC");
        PageInfo<CashOrderDepositUser> lst = new PageInfo<>(mapper.selectBySelective(dao));

        PageModel<CashOrderDepositUser> pageModel = new PageModel<>();
        pageModel.setPageNo(pageNo);
        pageModel.setPageNum(lst.getPages());
        pageModel.setPageSize(pageSize);
        pageModel.setTotal(lst.getTotal());
        pageModel.setList(lst.getList());
        return pageModel;
    }
}