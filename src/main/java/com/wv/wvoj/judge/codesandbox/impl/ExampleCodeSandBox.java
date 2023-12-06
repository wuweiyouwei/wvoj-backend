package com.wv.wvoj.judge.codesandbox.impl;

import com.wv.wvoj.judge.codesandbox.CodeSandBox;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 * @author wv
 * @version V1.0
 * @date 2023/12/6 16:32
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeRequest) {
        System.out.println("示例代码沙箱");
        return null;
    }
}
