package com.automaster.entity;

public enum CustomerType {

    SELLER("卖方"), BUYER("买方");
    private final String desc;

    CustomerType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}