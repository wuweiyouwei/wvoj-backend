package com.wv.wvoj.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * 题目用例封装类
 * @author wv
 * @version V1.0
 * @date 2023/11/25 18:08
 */
@Data
public class JudgeCase implements Serializable {


    /**
     * 题目输出用例
     */
    private String input;

    /**
     * 题目输出用例
     */
    private String output;

    private static final long serialVersionUID = 1L;

}
