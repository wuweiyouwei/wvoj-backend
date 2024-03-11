package com.wv.wvoj.rabbitmq;

import com.rabbitmq.client.Channel;
import com.wv.wvoj.common.ErrorCode;
import com.wv.wvoj.exception.BusinessException;
import com.wv.wvoj.judge.JudgeService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 消费者
 */
@Component
@Slf4j
public class MyMessageConsumer {

    private static final String MESSAGE_CONSUMER_KEY = "judgeservice:consumer:";

    @Resource
    private JudgeService judgeService;

    @Resource
    private RedisTemplate redisTemplate;

    // 指定程序监听的消息队列和确认机制
    @SneakyThrows
    @RabbitListener(queues = MessageSendConfig.SEND_CODE_QUEUE_NAME)
    public void receiveMessage(Message message, Channel channel) {
        Long questionSubmitId = (Long) message.getPayload();
        log.info("receiveMessage message = {}", questionSubmitId);

        Long deliveryTag = (Long) message.getHeaders().get(AmqpHeaders.DELIVERY_TAG);
        if (Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(MESSAGE_CONSUMER_KEY + questionSubmitId, String.valueOf(questionSubmitId))))
        {
            //存在说明这个消息已经被处理了，手动ack
            //直接丢掉消息
            channel.basicNack(deliveryTag, false, false);
        } else {
            // 判题
            log.info("开始判题...");
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false);
            //将数据存入redis
            redisTemplate.opsForHash().put(MESSAGE_CONSUMER_KEY + questionSubmitId, String.valueOf(questionSubmitId), String.valueOf(questionSubmitId));
            // 过期时间
            redisTemplate.expire(MESSAGE_CONSUMER_KEY + questionSubmitId, 1, TimeUnit.MINUTES);
        }

    }

}