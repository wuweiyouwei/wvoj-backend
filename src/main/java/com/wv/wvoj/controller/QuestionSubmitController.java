package com.wv.wvoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wv.wvoj.common.BaseResponse;
import com.wv.wvoj.common.ErrorCode;
import com.wv.wvoj.common.ResultUtils;
import com.wv.wvoj.exception.BusinessException;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wv.wvoj.model.entity.QuestionSubmit;
import com.wv.wvoj.model.entity.User;
import com.wv.wvoj.model.vo.QuestionSubmitVO;
import com.wv.wvoj.service.QuestionSubmitService;
import com.wv.wvoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 * wv
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                                  HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取题目提交列表（除了管理员外，普通用户自己只能看到非答案等公开信息）
     *
     * @param questionSubmitQueryRequest 题目提交查询参数
     * @param request 请求
     * @return 分页数据
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 从数据库中查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userService.getLoginUser(request);
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

}
