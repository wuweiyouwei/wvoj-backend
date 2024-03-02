package com.wv.wvoj.judge.codesandbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将 CodeSandBox 交给 IOC 容器管理
 * @author wv
 * @version V1.0
 * @date 2024/3/2 22:06
 */
@Configuration
public class CodeSandBoxFactoryConfig {

    @Value("${codesandbox.type:example}")
    private String type;

    @Bean
    public CodeSandBox codeSandBoxFactory() {
        return CodeSandBoxFactory.getInstance(type);
    }
}
