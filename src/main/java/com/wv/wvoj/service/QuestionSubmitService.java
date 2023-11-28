package com.wv.wvoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wv.wvoj.model.dto.question.QuestionQueryRequest;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wv.wvoj.model.entity.Question;
import com.wv.wvoj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wv.wvoj.model.entity.User;
import com.wv.wvoj.model.vo.QuestionSubmitVO;
import com.wv.wvoj.model.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 21192
 * @description 针对表【question_submit(题目提交)】的数据库操作Service
 * @createDate 2023-11-25 16:58:46
 */
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return 提交记录的 id
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);


    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 获取提交题目视图
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取提交题目封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);
}
