REST API Document PUBLIC
=============================================

根路径
------------------------------------------
    
    https://domain_name

ENDPOINT
------------------------------------------

### Depth

1. URI请求路径 `/trade/trade?symbol=`symbolCode`&brokerId=10003`

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |symbolCode|String|是|交易对（填充到URL路径中）|


2. 响应示例

```
    {
    "sell": [
        [
            0.00002736, 
            1071.36299794
        ], 
        [
            0.00002738, 
            0.19978522
        ], 
        [
            0.00002742, 
            75.9087355
        ]
    ], 
    "buy": [
        [
            0.00002618, 
            16.33033728
        ], 
        [
            0.00002611, 
            605.26813127
        ], 
        [
            0.0000261, 
            499.81630013
        ]
    ]
}
```

### Klines

1. URI请求路径 `/dataload/kline-query/pages?startTime=1522206827&endTime=1523502887&pageSize=300&symbol=BTC_EOS&kline=15m`

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |startTime|long|是|开始时间秒数（填充到URL路径中）|
    |endTime|long|是|结束时间秒数（填充到URL路径中）|
    |pageSize|int|是|加载条数最大300（填充到URL路径中）|
    |symbol|String|是|交易对（填充到URL路径中）|
    |kline|String|是|`1m` `5m` `15m` `30m` `1h` `1h`（填充到URL路径中）|


2. 响应示例

```
{
    "code": "100200", 
    "data": {
        "pages": {
            "list": [
                {
                    "amount": 1228.7905, 
                    "close": 0.0012868, 
                    "klineType": "MIN15", 
                    "lowPrice": 0.0012703, 
                    "maxPrice": 0.0012931, 
                    "open": 0.0012917, 
                    "symbol": "BTC_EOS", 
                    "time": 1523502000
                }, 
                {
                    "amount": 1957.1155, 
                    "close": 0.0012917, 
                    "klineType": "MIN15", 
                    "lowPrice": 0.0012498, 
                    "maxPrice": 0.0012925, 
                    "open": 0.0012519, 
                    "symbol": "BTC_EOS", 
                    "time": 1523501100
                }
            ], 
            "pageNum": 1, 
            "pageSize": 2, 
            "pages": 1, 
            "size": 2, 
            "startIndex": 0, 
            "total": 2
        }
    }, 
    "msg": "成功"
}
```

### 24HOURS

1. URI请求路径 `https://domain_name/trade/info?symbol=BTC_EOS&brokerId=10003`

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |symbol|String|是|交易对（填充到URL路径中）|
   
2. 响应示例

```
{
    "24high": "0.00129310000000000000", 
    "24low": "0.00087410000000000000", 
    "24Total": "95162.53042596000000000000", 
    "order": [
        {
            "time": "1523503566", 
            "type": "SELL", 
            "num": 9.15, 
            "price": 0.0012772
        }, 
        {
            "time": "1523503506", 
            "type": "BUY", 
            "num": 2.3165, 
            "price": 0.00128
        }
    ], 
    "24Price": "0.00087520000000000000"
}
```

REST API Document PRIVATE
===================================

请求交互
-------------------------------------

### REST访问的根URL
    
    https://domain_name/exchangeApi


### 请求交互说明

- 请求参数：

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |businessNo|String|是|Api账号|
    |nonceStr|String|是|32位随机数字|
    |timestamp|long|是|时间戳（秒数）|
    |data|String|是|<span id = "data">**JSON格式**数据</span>|
    |sign|String|是|[签名](#signature)（见签名方式）|

- 提交方式

    将封装好的请求参数转换为`JSON 格式`通过POST 或 GET 方式提交至服务器。

- 服务器响应

    服务器首先对用户请求数据进行参数安全校验，通过校验后根据业务逻辑将响应数据以`JSON 格式` 响应给客户

- 响应数据：

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |code|String|是|[响应码](#respCode)|
    |msg|String|是|响应信息|
    |data|String|是|<span id = "retData">**JSON格式**数据</span>|

### 签名方式

<span id = "signature"></span>

1. 将`data`数据以键值对的形式添加到map中
2. 将`nonceStr`添加到map中
3. 将`timestamp`添加到map中
4. 将`apiSecret`添加到map中
5. 以升序的方式将map排序；并拼接成URL字符串形式
6. 将拼接的字符串进行MD5加密；转成HEX大写字符串

```
private static final Comparator<Entry<String, Object>> keyComparator = (o1, o2) -> o1.getKey().compareTo(o2.getKey());


private String generateSign(Map<String, Object> data, long timestamp, String nonceStr, String apiSecret) {
    Map<String, Object> dataJson = new HashMap<>();
    for (String key : data.keySet()) {
      Object obj = data.get(key);
      if (obj != null) {
        dataJson.put(key, data.get(key));
      }
    }
    dataJson.put("nonceStr", nonceStr);
    dataJson.put("timestamp", timestamp);
    dataJson.put("apiSecret", apiSecret);
    String result = dataJson.entrySet().stream().sorted(keyComparator)
            .map(entry -> entry.getKey() + "=" + entry.getValue().toString())
            .collect(Collectors.joining("&"));
    return DigestUtils.md5Hex(result).toUpperCase();
  }

```

API参考
------------------------------------------

### 查询用户资产

1. URI路径 `/api/asset`

2. 请求参数 [API数据](#data)

    无 `{}`

3. 响应[数据](#retData)【ARRAY】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |assetCode|String|是|资产编码|
    |amountAvailable|String|是|可用金额|
    |amountLock|String|是|冻结金额|
    |amountLoan|String|是|贷款金额|

### 交易挂单

1. URI路径 `/api/matchOrder`

2. 请求参数【OBJ】 [API数据](#data)

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |outOrderNo|String|是|订单号（唯一）|
    |symbol|String|是|交易对|
    |tradeCoinFlag|String|是| `FIXED` 限价撮合 |
    |tradeCoinType|String|是| `BUY`  `SELL` |
    |price|BigDecimal|是|价格|
    |amount|BigDecimal|是|数量|

3. 响应[数据](#retData)【OBJ】

    参看响应码


### 查询挂单

1. URI路径 `api/orderquery`

2. 请求参数 [API数据](#data)【OBJ】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |outTradeNo|String|是|订单号|

3. 响应[数据](#retData)【OBJ】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |createTime|String|是|创建时间|
    |tradedNumber|String|是|撮合数量|
    |numberOver|String|是|剩余数量|
    |orderNo|String|是|订单号|
    |payTransactionNo|String|是|交易单号|
    |tradeCoinType|String|是| `BUY`  `SELL` |
    |tradeCoinFlag|String|是| `FIXED` |
    |symbol|String|是|交易对|
    |price|String|是|下单价格|
    |number|String|是|下单数量|
    |matchedMoney|String|是|成交金额|
    |moneyOver|String|是||
    |money|String|是||
    |tradeCoinStatus|String|是|`PROCESSING`  `SUCCESS` `CANCEL` `WAITING` `FAIL`|



### 查询进行中的挂单（最大返回200）

1. URI路径 `/api/matchOrder/process`

2. 请求参数 [API数据](#data)【OBJ】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |symbol|String|是|交易对|

3. 响应[数据](#retData)【ARRAY】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |createTime|String|是|创建时间|
    |tradedNumber|String|是|撮合数量|
    |numberOver|String|是|剩余数量|
    |orderNo|String|是|订单号|
    |payTransactionNo|String|是|交易单号|
    |tradeCoinType|String|是| `BUY`  `SELL` |
    |tradeCoinFlag|String|是| `FIXED` |
    |symbol|String|是|交易对|
    |price|String|是|下单价格|
    |number|String|是|下单数量|
    |matchedMoney|String|是|成交金额|
    |moneyOver|String|是||
    |money|String|是||
    |tradeCoinStatus|String|是|`PROCESSING`  `SUCCESS` `CANCEL` `WAITING` `FAIL`|

### 取消挂单

1. URI路径 `/api/cancel`

2. 请求参数 [API数据](#data)【OBJ】

    |参数名|   参数类型|   必填| 描述|
    | :-----    | :-----   | :-----    | :-----   |
    |outTradeNo|String|是|订单号|

3. 响应[数据](#retData)【OBJ】

    参看响应码


### 响应码

<span id = "respCode"></span>

|响应码|   响应信息|
| :-----    | :-----   |
|100200|成功|
|100104|非法的请求|
|100103|服务不可用|
|100100|传入字段校验错误|
|102100|非法的交易对|
|102101|订单号重复|
|102103|交易对未启用|
|102104|交易对被关闭|
|102105|交易引擎不存在|
|101102|订单不存在|
