package com.wjybxx.fastjgame.misc;

import com.wjybxx.fastjgame.utils.MathUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 带有指数回退特征的永久尝试策略。
 * Curator自带的重试策略不是两种兼容的。
 *
 * 修改自{@link org.apache.curator.retry.RetryForever} 和
 * {@link org.apache.curator.retry.ExponentialBackoffRetry}
 *
 * @author wjybxx
 * @version 1.0
 * @date 2019/5/12 15:35
 * @github - https://github.com/hl845740757
 */
public class BackoffRetryForever implements RetryPolicy {

    private static final Logger logger = LoggerFactory.getLogger(BackoffRetryForever.class);

    private final Random random = new Random();
    /**
     * 基础睡眠时间(毫秒)(最小值)
     */
    private final int baseSleepTimeMs;
    /**
     * 最大睡眠时间(毫秒)(最大值)
     */
    private final int maxSleepTimeMs;

    public BackoffRetryForever(int baseSleepTimeMs, int maxSleepTimeMs) {
        if (baseSleepTimeMs<=0){
            throw new IllegalArgumentException("baseSleepTimeMs must greater than 0");
        }
        if (maxSleepTimeMs<baseSleepTimeMs){
            throw new IllegalArgumentException("maxSleepTimeMs must greater than baseSleepTimeMs");
        }
        this.baseSleepTimeMs = baseSleepTimeMs;
        this.maxSleepTimeMs = maxSleepTimeMs;
    }

    @Override
    public boolean allowRetry(int retryCount, long elapsedTimeMs, RetrySleeper sleeper) {
        try
        {
            sleeper.sleepFor(getSleepTimeMs(retryCount,elapsedTimeMs), TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            // 捕获中断一般需要在退出前恢复中断，要养成习惯
            Thread.currentThread().interrupt();
            logger.warn("Error occurred while sleeping", e);
            return false;
        }
        return true;
    }

    /**
     * 获取睡眠时间
     * @param retryCount 尝试次数 the number of times retried so far (0 the first time)
     */
    private long getSleepTimeMs(int retryCount, long elapsedTimeMs){
        // 退避指数，0~29
        int exponential=Math.min(29,retryCount);
        // 转为十进制倍数
        int multiple = 1 << (exponential + 1);

        // curator源码有bug，int不可以直接乘得到long，会越界(不过可能等不到越界已经睡死了...)
        long sleepMs = MathUtils.safeMultiplyInt(baseSleepTimeMs, Math.max(1,random.nextInt(multiple)));
        // 超过最大睡眠时间
        if (sleepMs > maxSleepTimeMs)
        {
            logger.warn(String.format("Sleep extension too large (%d). Pinning to %d", sleepMs, maxSleepTimeMs));
            sleepMs = maxSleepTimeMs;
        }
        return sleepMs;
    }

}
