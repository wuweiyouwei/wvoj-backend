package com.wv.wvoj.rabbitmq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wv.wvoj.model.entity.MessageSendLog;
import com.wv.wvoj.service.MessageSendLogService;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
@Component
@EnableScheduling
//@Deprecated
public class SendCodeJob {


    @Resource
    private MessageSendLogService sendLogService;

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 每隔十秒执行一次
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void messageSend() {
        QueryWrapper<MessageSendLog> qw = new QueryWrapper<>();
        qw.lambda()
                .eq(MessageSendLog::getStatus, 0)
                .le(MessageSendLog::getTryTime, new Date());
        List<MessageSendLog> list = sendLogService.list(qw);
        for (MessageSendLog sendLog : list) {
            sendLog.setUpdateTime(new Date());
            if (sendLog.getTryCount() > 2) {
                // 说明已经重试了三次了，此时直接设置消息发送失败
                sendLog.setStatus(2);
                sendLogService.updateById(sendLog);
            } else {
                //还未达到上限，重试
                //更新重试次数
                sendLog.setTryCount(sendLog.getTryCount() + 1);
                sendLogService.updateById(sendLog);
                rabbitTemplate.convertAndSend(sendLog.getExchange(), sendLog.getRouteKey(), sendLog.getQuestionSubmitId(),
                        new CorrelationData(String.valueOf(sendLog.getMsgId())));
            }
        }

    }
}