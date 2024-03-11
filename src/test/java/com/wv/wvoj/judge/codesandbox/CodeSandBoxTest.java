package com.wv.wvoj.judge.codesandbox;

import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wv
 * @version V1.0
 * @date 2023/12/6 16:45
 */
@SpringBootTest
class CodeSandBoxTest {

    @Value("${codesandbox.type:example}")
    private String type;

    /**
     * 测试代码沙箱
     */
    @Test
    void executeCode() {
        String code = "int main{}";
        String language = "Java";
        List<String> list = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(list)
                .build();
        CodeSandBox instance = CodeSandBoxFactory.getInstance(type);
        instance.executeCode(executeCodeRequest);
    }


    /**
     * 测试代码沙箱代理类
     */
    @Test
    void executeCodeWithProxy() {


        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"a + b 等于 \" + (a + b));\n" +
                "    }\n" +
                "}";
        String language = "Java";
        List<String> list = Arrays.asList("1 2", "3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(list)
                .build();
        CodeSandBox instance = CodeSandBoxFactory.getInstance(type);
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(instance);
        ExecuteCodeResponse executeCodeResponse = instance.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);

    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String type = scanner.next();
            String code = "public static void main(String[] args) {\n" +
                    "        int a = Integer.parseInt(args[0]);\n" +
                    "        int b = Integer.parseInt(args[1]);\n" +
                    "        System.out.println(\"a + b = \" + (a + b));\n" +
                    "    }";
            String language = "Java";
            List<String> list = Arrays.asList("1 2", "3 4");
            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                    .code(code)
                    .language(language)
                    .inputList(list)
                    .build();
            CodeSandBox instance = CodeSandBoxFactory.getInstance(type);
            ExecuteCodeResponse executeCodeResponse = instance.executeCode(executeCodeRequest);
            Assertions.assertNotNull(executeCodeResponse);
        }
    }
}