package com.wv.wvoj.model.enums;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 题目提交状态枚举
 * @author wv
 * @version V1.0
 * @date 2023/11/27 18:53
 */
public enum SendLogStatusEnum {

    // 0-发送中 1-发送成功 2-发送失败
    SENDING("发送中", 0),
    SUCCESS("发送成功", 1),
    FAILED("发送失败", 2);
    private final String text;

    private final Integer value;

    SendLogStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     *
     * @return
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static SendLogStatusEnum getEnumByValue(Integer value) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }
        for (SendLogStatusEnum anEnum : SendLogStatusEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

}
