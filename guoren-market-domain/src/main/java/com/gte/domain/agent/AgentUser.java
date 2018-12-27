package com.gte.domain.agent;

import com.gop.domain.User;
import lombok.Data;
import lombok.ToString;


@Data
public class AgentUser extends User{
    private Integer adminId;
}