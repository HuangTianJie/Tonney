package com.gte.domain.agent;

import com.gop.domain.TradeOrder;
import lombok.Data;
import lombok.ToString;

@Data
public class AgentTradeOrder extends TradeOrder {
    private Integer adminId;
}
