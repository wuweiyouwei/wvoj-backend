package com.wv.wvoj.service.impl;

import java.util.Date;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wv.wvoj.common.ErrorCode;
import com.wv.wvoj.constant.CommonConstant;
import com.wv.wvoj.exception.BusinessException;
import com.wv.wvoj.judge.JudgeService;
import com.wv.wvoj.mapper.QuestionSubmitMapper;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wv.wvoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wv.wvoj.model.entity.MessageSendLog;
import com.wv.wvoj.model.entity.Question;
import com.wv.wvoj.model.entity.QuestionSubmit;
import com.wv.wvoj.model.entity.User;
import com.wv.wvoj.model.enums.QuestionSubmitLanguageEnum;
import com.wv.wvoj.model.enums.QuestionSubmitStatusEnum;
import com.wv.wvoj.model.enums.SendLogStatusEnum;
import com.wv.wvoj.model.vo.QuestionSubmitVO;
import com.wv.wvoj.rabbitmq.MessageSendConfig;
import com.wv.wvoj.service.MessageSendLogService;
import com.wv.wvoj.service.QuestionService;
import com.wv.wvoj.service.QuestionSubmitService;
import com.wv.wvoj.service.UserService;
import com.wv.wvoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 21192
 * @description 针对表【question_submit(题目提交)】的数据库操作Service实现
 * @createDate 2023-11-25 16:58:46
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;


    @Resource
    private MessageSendLogService messageSendLogService;

    @Resource
    private RabbitTemplate rabbitTemplate;


    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体r
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }

        // 执行判题服务
//        CompletableFuture.runAsync(() -> {
//            judgeService.doJudge(questionSubmitId);
//        });
        Long questionSubmitId = questionSubmit.getId();

        // 异步：执行判题服务
        // 将要发给消息中间件的信息记录到数据库中
        // 使用雪花算法创建为主键的 msgId
        long msgId = IdUtil.getSnowflakeNextId();

        MessageSendLog messageSendLog = new MessageSendLog();
        messageSendLog.setMsgId(msgId);
        // 题目提交id
        messageSendLog.setQuestionSubmitId(questionSubmitId);
        // 设置路由键
        messageSendLog.setRouteKey(MessageSendConfig.SEND_CODE_QUEUE_NAME);
        // 设置交换机
        messageSendLog.setExchange(MessageSendConfig.SEND_CODE_EXCHANGE_NAME);
        // 设置消息状态为发送中
        messageSendLog.setStatus(SendLogStatusEnum.SENDING.getValue());
        // 表示没重试 最多重试三次
        messageSendLog.setTryCount(0);
        // 设置重试时间在一分钟之后
        messageSendLog.setTryTime(new Date(System.currentTimeMillis() + 60 * 1000));

        boolean saveMessageSendLog = messageSendLogService.save(messageSendLog);
        if (!saveMessageSendLog) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "日志插入失败");
        }

        // 发送消息
        rabbitTemplate.convertAndSend(MessageSendConfig.SEND_CODE_EXCHANGE_NAME,
                MessageSendConfig.SEND_CODE_QUEUE_NAME,
                questionSubmitId,
                new CorrelationData(String.valueOf(msgId)));

        return questionSubmitId;

    }

    /**
     * 获取查询包装器
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取提交题目封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        // 脱敏仅提交代码本人和管理员可以查看代码等敏感信息
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        long userId = loginUser.getId();
        // 非管理员继续脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }

        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent()
                , questionSubmitPage.getSize(), questionSubmitPage.getTotal());

        // 如果查询结果为空，则直接返回空的Page
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }

        // 将QuestionSubmit对象转换为QuestionSubmitVO对象
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList
                .stream()
                // 脱敏
                .map(questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser))
                .collect(Collectors.toList());

        // 将QuestionSubmitVO对象放入Page中
        questionSubmitVOPage.setRecords(questionSubmitVOList);

        return questionSubmitVOPage;
    }
}




