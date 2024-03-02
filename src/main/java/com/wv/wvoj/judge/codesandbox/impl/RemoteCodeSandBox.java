package com.wv.wvoj.judge.codesandbox.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.wv.wvoj.common.ErrorCode;
import com.wv.wvoj.exception.BusinessException;
import com.wv.wvoj.judge.codesandbox.CodeSandBox;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wv.wvoj.service.UserService;
import com.wv.wvojapiclientsdk.client.ApiClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 远程代码沙箱（实际调用接口的沙箱）
 * @author wv
 * @version V1.0
 * @date 2023/12/6 16:32
 */
public class RemoteCodeSandBox implements CodeSandBox {


    @Resource
    private ApiClient apiClient;


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeRequest) {
        System.out.println("远程代码沙箱");

        // API 调用远程代码沙箱
//        String url = "http://localhost:8090/executeCode";
//        String requestJson = JSONUtil.toJsonStr(executeRequest);
//        String responseStr = HttpUtil.createPost(url)
//                .body(requestJson)
//                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
//                .execute()
//                .body();
//        ApiClient apiClient = new ApiClient(accessKey, secretKey);
//        String jsonExecuteRequest = JSONUtil.toJsonStr(executeRequest);
//        com.wv.wvojapiclientsdk.model.ExecuteCodeRequest executeCodeRequest = BeanUtil.toBean(jsonExecuteRequest, com.wv.wvojapiclientsdk.model.ExecuteCodeRequest.class);
        com.wv.wvojapiclientsdk.model.ExecuteCodeRequest executeCodeRequest = BeanUtil.copyProperties(executeRequest, com.wv.wvojapiclientsdk.model.ExecuteCodeRequest.class);
        String responseStr = apiClient.callSandbox(executeCodeRequest);
        if (StrUtil.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR,"executeCode remoteCodeSandBox error, message = " + responseStr);
        }
        ExecuteCodeResponse executeCodeResponse = JSONUtil.toBean(responseStr, ExecuteCodeResponse.class);
        System.out.println(executeCodeResponse);
        return executeCodeResponse;
    }
}
