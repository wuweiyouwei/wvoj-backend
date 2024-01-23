package com.wv.wvoj.judge;

import com.wv.wvoj.judge.codesandbox.model.JudgeInfo;
import com.wv.wvoj.judge.strategy.DefaultJudgeStrategyImpl;
import com.wv.wvoj.judge.strategy.JavaJudgeStrategyImpl;
import com.wv.wvoj.judge.strategy.JudgeContext;
import com.wv.wvoj.judge.strategy.JudgeStrategy;
import com.wv.wvoj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 策略管理器（简化调用）
 *
 * @author wv
 * @version V1.0
 * @date 2024/1/23 9:32
 */
@Service
public class JudgeManager {

    JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();

        JudgeStrategy judgeStrategy = new DefaultJudgeStrategyImpl();

        if (language.equals("java")){
            judgeStrategy = new JavaJudgeStrategyImpl();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
