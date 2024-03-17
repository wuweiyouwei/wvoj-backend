package com.wv.wvoj.manager;

import com.wv.wvoj.common.ErrorCode;
import com.wv.wvoj.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author wv
 * @version V1.0
 * @date 2024/3/14 16:03
 */
/**
 * 专门提供 RedisLimiter 限流基础服务的（提供了通用的能力,放其他项目都能用）
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;
    /**
     * 限流操作
     *
     * @param key 区分不同的限流器，比如不同的用户 id 应该分别统计
     */
    public void doRateLimit(String key) {
        // 创建一个名称为user_limiter的限流器，每秒最多访问 2 次
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 限流器的统计规则(每四秒一个请求，每四秒允许用户提交一次)
        // RateType.OVERALL表示速率限制作用于整个令牌桶,即限制所有请求的速率
        rateLimiter.trySetRate(RateType.OVERALL, 1, 4, RateIntervalUnit.SECONDS);
        // 每当一个操作来了后，请求一个令牌
        boolean canOp = rateLimiter.tryAcquire(1);
        // 如果没有令牌,还想执行操作,就抛出异常
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
