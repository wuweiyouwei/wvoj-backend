package com.wv.wvoj.judge.codesandbox.impl;
import com.wv.wvoj.judge.codesandbox.model.JudgeInfo;

import java.util.List;

import com.wv.wvoj.judge.codesandbox.CodeSandBox;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wv.wvoj.model.enums.JudgeInfoMessageEnum;
import com.wv.wvoj.model.enums.QuestionSubmitStatusEnum;

/**
 * 示例代码沙箱（仅为了跑通业务流程）
 * @author wv
 * @version V1.0
 * @date 2023/12/6 16:32
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeRequest) {

        List<String> inputList = executeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());

        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
