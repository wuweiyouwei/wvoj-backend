package com.wv.wvoj.judge.codesandbox.impl;

import com.wv.wvoj.judge.codesandbox.CodeSandBox;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 * @author wv
 * @version V1.0
 * @date 2023/12/6 16:32
 */
public class RemoteCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeRequest) {
        System.out.println("远程代码沙箱");

        return null;
    }
}
