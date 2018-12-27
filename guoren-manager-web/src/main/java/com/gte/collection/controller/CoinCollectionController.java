package com.gte.collection.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gop.code.consts.CommonCodeConst;
import com.gop.domain.ChannelCoinAddressDeposit;
import com.gop.domain.response.CollectionAccountResponse;
import com.gop.exception.AppException;
import com.gop.web.base.annotation.AuthForHeader;
import com.gop.web.base.auth.AuthContext;
import com.gop.web.base.auth.annotation.Strategy;
import com.gop.web.base.auth.annotation.Strategys;
import com.gte.collection.service.CoinCollectionService;
/**
 * 归集操作
 * @author DELL
 *
 */
@RestController
@Api("归集操作类")
public class CoinCollectionController {
	@Autowired
	private CoinCollectionService coinCollectionService;
	  @Autowired
	  private RestTemplate restTemplate;
	  @Value("${server.rpc.url}")
	  private String serverRpcUrl;
	  @Value("${eth.rpc.url}")
	  private String ethRpcUrl;
	  /**
	   * 查询用户的交易所的充值地址
	   * @param assetCode
	   * @return
	   */
	  @GetMapping("/queryCollectionAddress/{assetCode}")
	  @ApiOperation(value = "查询用户的交易所的充值地址", notes = "给钱包项目调用")
         public List<ChannelCoinAddressDeposit> queryCollectionAddresses(@PathVariable("assetCode") String assetCode){
	    	 List<ChannelCoinAddressDeposit> collectionAddressList = coinCollectionService.getCollectionAddressList(assetCode);
        	 return collectionAddressList;
         }
		  /**
		   * 归集账号查询
		   */
		  @SuppressWarnings("unchecked")
		  @GetMapping("/collectionAccountQuery")
		  @ApiOperation(value = "归集账号查询", notes = "归集账号查询")
			@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
		  public List<CollectionAccountResponse> collectionAccountQuery(@AuthForHeader AuthContext context,String assetsCode){
			  //查链上各币种的提现账户余额
			  String json = restTemplate.getForObject(serverRpcUrl+"/queryChainTotalAsset?assetsCode={assetsCode}",String.class,assetsCode);
			  JSONArray JSONArrays = JSONArray.parseArray(json);
			  //market数据库查出用户可用余额，冻结余额
			List<CollectionAccountResponse> collectionAccountList = coinCollectionService.getCollectionAccountList(assetsCode);
			for (int i = 0; i < collectionAccountList.size(); i++) {
				CollectionAccountResponse collectionAccountResponse = collectionAccountList.get(i);
				collectionAccountResponse.setAmountChain(new BigDecimal(0));
				collectionAccountResponse.setPaltTotalAmount(new BigDecimal(0));
				String assetsCode2 = collectionAccountResponse.getAssetsCode();
				if (!StringUtils.isEmpty(assetsCode2)) {
					for (int j = 0; j < JSONArrays.size(); j++) {
						JSONObject JSONObject = JSONArrays.getJSONObject(j);
						String assetsCode3 = JSONObject.getString("assetsCode");
						if (assetsCode2.equals(assetsCode3)) {
							collectionAccountResponse.setAmountChain(JSONObject.getBigDecimal("chainAmount"));
							collectionAccountResponse.setPaltTotalAmount(JSONObject.getBigDecimal("paltTotalAmount"));
							collectionAccountResponse.setPaltTotalCollectionAmount(JSONObject.getBigDecimal("paltTotalCollectionAmount"));
						}
					}
					
				}
			}  
			return collectionAccountList;
		  }
		  /**
		   * 提币账号归集查询
		   */
		  @GetMapping("/withdraCollectionAccountQuery")
		  @ApiOperation(value = "提币账号归集查询", notes = "提币账号归集查询")
			@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
		  public Object withdraCollectionAccountQuery(@AuthForHeader AuthContext context,String assetCode,
				  @RequestParam("pageNo") @ApiParam(defaultValue = "1") Integer pageNo,
                  @RequestParam("pageSize") @ApiParam(defaultValue = "10") Integer pageSize){
			  System.out.println(serverRpcUrl);
			  System.out.println(ethRpcUrl);
			  String url = serverRpcUrl+"/withdraCollectionAccountQuery?assetCode={assetCode}&pageNo={pageNo}&pageSize={pageSize}";
			    Map<String, Object> uriVariables = new HashMap<String, Object>();
			    uriVariables.put("assetCode",assetCode);
			    uriVariables.put("pageNo",pageNo);
			    uriVariables.put("pageSize", pageSize);
			return restTemplate.getForObject(url,Object.class,uriVariables);
		  }
		  /**
		   * 提币账号归集明细查询
		   */
		  @GetMapping("/withdraAccountCollectionDetailedQuery")
		  @ApiOperation(value = "提币账号归集明细查询", notes = "提币账号归集明细查询")
		  @Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
	      public Object withdraAccountCollectionDetailedQuery(@AuthForHeader AuthContext context,@RequestParam String collectionId,
	    		  @RequestParam("pageNo") @ApiParam(defaultValue = "1") Integer pageNo,
                  @RequestParam("pageSize") @ApiParam(defaultValue = "10") Integer pageSize){
			  String url =serverRpcUrl+"/withdraAccountCollectionDetailedQuery?collectionId={collectionId}&pageNo={pageNo}&pageSize={pageSize}";
			  Map<String, Object> uriVariables = new HashMap<String, Object>();
			  uriVariables.put("collectionId",collectionId);
			    uriVariables.put("pageNo",pageNo);
			    uriVariables.put("pageSize", pageSize);
	    	  return restTemplate.getForObject(url,Object.class,uriVariables);
	    	  
	 }
		  /**
		   * 资产归集
		   * @param collectNum
		   * @param assetsCode
		   */
		   @GetMapping("/collectionAssets")
		   @ApiOperation(value = "资产归集", notes = "资产归集")
			@Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
		   public void collectionAssets(@AuthForHeader AuthContext context,@RequestParam String collectNum,@RequestParam BigDecimal conditionNum,@RequestParam String assetsCode){
			   if ("USDT".equals(assetsCode)) {
				   collectionUSDT(collectNum,conditionNum);
			}else if ("ETH".equals(assetsCode)) {
				collectionETH(collectNum,conditionNum);
			}
		   }
		  /**
		   * USDT归集
		   */
		  private void collectionUSDT(String collectNum,BigDecimal conditionNum){
			  Map<String, Object> uriVariables = new HashMap<String, Object>();
			  String url =serverRpcUrl+"/collectionUSDT?collectNum={collectNum}&conditionNum={conditionNum}";
			  uriVariables.put("collectNum",collectNum);
			    uriVariables.put("conditionNum",conditionNum);
			   String jsonStr = restTemplate.getForObject(url,String.class,uriVariables);
			   JSONObject parseObject = JSONObject.parseObject(jsonStr);
			   Integer status = parseObject.getInteger("status");
			   String msg = parseObject.getString("msg");
			   if (status!=200) {
				   throw new AppException(CommonCodeConst.FIELD_ERROR,msg);
			}
			
		  }
		  /**
		   * ETH归集
		   */
		  private void collectionETH(String collectNum,BigDecimal conditionNum){
			  Map<String, Object> uriVariables = new HashMap<String, Object>();
			  String url =ethRpcUrl+"/collectionETH?collectNum={collectNum}&conditionNum={conditionNum}";
			  uriVariables.put("collectNum",collectNum);
			    uriVariables.put("conditionNum",conditionNum);
			  String jsonStr = restTemplate.getForObject(url,String.class,uriVariables);
			   JSONObject parseObject = JSONObject.parseObject(jsonStr);
			   Integer status = parseObject.getInteger("status");
			   String msg = parseObject.getString("msg");
			   if (status!=200) {
				   throw new AppException(CommonCodeConst.FIELD_ERROR,msg);
			}
		  }
		  /**
		   * 查询所有充值USDT地址和余额
		   */
		  @GetMapping("/getTotalAddressesAndBalances")
		  @ApiOperation(value = "查询所有充值USDT地址和余额", notes = "查询所有充值USDT地址和余额")
		  @Strategys(strategys = { @Strategy(authStrategy = "exe({{'checkLoginStrategy'}})") })
		  public Object getTotalAddressesAndBalances(@AuthForHeader AuthContext context,String assetCode,@RequestParam("pageNo") @ApiParam(defaultValue = "1") Integer pageNo,
                  @RequestParam("pageSize") @ApiParam(defaultValue = "10") Integer pageSize) {
			  if (StringUtils.isEmpty(assetCode)) {
				  throw new AppException(CommonCodeConst.FIELD_ERROR,"参数不能为空");
			}
			  Map<String, Object> uriVariables = new HashMap<String, Object>();
			    uriVariables.put("pageNo",pageNo);
			    uriVariables.put("pageSize", pageSize);
			  String USDTUrl=serverRpcUrl+"/getTotalAddressesAndBalances?pageNo={pageNo}&pageSize={pageSize}";
			  String ETHUrl=ethRpcUrl+"/getTotalAddressesAndBalances?pageNo={pageNo}&pageSize={pageSize}";
			  if ("ETH".equals(assetCode)) {
				  return  restTemplate.getForObject(ETHUrl,Object.class,uriVariables);
			}else if ("USDT".equals(assetCode)) {
				return  restTemplate.getForObject(USDTUrl,Object.class,uriVariables);

		  }else{
			  throw new AppException(CommonCodeConst.FIELD_ERROR,"参数错误");
			  }
		  }
}
