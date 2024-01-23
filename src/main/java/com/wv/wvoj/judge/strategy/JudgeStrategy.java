package com.wv.wvoj.judge.strategy;

import com.wv.wvoj.judge.codesandbox.model.JudgeInfo;

/**
 * 判题策略接口
 * @author wv
 * @version V1.0
 * @date 2024/1/23 8:53
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
