package com.wv.wvoj.judge;

import com.wv.wvoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wv.wvoj.model.entity.QuestionSubmit;

/**
 * 判题服务
 * @author wv
 * @version V1.0
 * @date 2023/12/6 18:18
 */
public interface JudgeService {

    /**
     * 判题服务
     * @param questionId 题目 id
     * @return
     */
    QuestionSubmit doJudge(long questionId);
}
