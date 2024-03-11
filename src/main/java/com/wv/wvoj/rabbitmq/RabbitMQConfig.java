package com.wv.wvoj.rabbitmq;

import com.wv.wvoj.model.entity.MessageSendLog;
import com.wv.wvoj.model.enums.SendLogStatusEnum;
import com.wv.wvoj.service.MessageSendLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Date;

@Configuration
@Slf4j
public class RabbitMQConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {


    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MessageSendLogService sendLogService;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String s) {
        long msgId = Long.parseLong(correlationData.getId());
        if (ack) {
            //说明消息到达交换机
            MessageSendLog sendLog = new MessageSendLog();
            sendLog.setMsgId(msgId);
            sendLog.setStatus(SendLogStatusEnum.SUCCESS.getValue());
            sendLog.setUpdateTime(new Date(System.currentTimeMillis()));
            //更新数据库
            sendLogService.updateById(sendLog);
            log.info("消息成功到达交换机：{}", msgId);
        } else {
            log.info("消息未到达交换机:{}", msgId);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.error("消息未到达队列：{}",returnedMessage.toString());
    }
}