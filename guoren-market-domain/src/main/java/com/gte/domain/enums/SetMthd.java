package com.gte.domain.enums;

public enum SetMthd {
    RATIO(1), STATIC(2);

    private final int value;

    //必须增加一个构造函数,变量,得到该变量的值
    private SetMthd(int i) {
        value = i;
    }

    /**
     * @return 枚举变量实际返回值
     */
    public int getValue()
    {
        return value;
    }
}
