package com.wv.wvoj;

import com.wv.wvoj.config.WxOpenConfig;

import javax.annotation.Resource;

import com.wv.wvojapiclientsdk.client.ApiClient;
import com.wv.wvojapiclientsdk.model.ExecuteCodeRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

/**
 * 主类测试
 * <p>
 * <p>
 * wv
 */
@SpringBootTest
class MainApplicationTests {


    @Resource
    private ApiClient apiClient;

    @Test
    public void testSdk() {
        String s = apiClient.callSandbox(new ExecuteCodeRequest());
        System.out.println(apiClient);
    }

}
