package com.example.dpoker.Utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 企业生产级线程池模板
 */
public final class BizThreadPool {

    /** 核心线程数：CPU 密集任务建议为 N+1；IO 密集任务建议为 2N+1；混合任务需压测 */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() + 1;

    /** 最大线程数：峰值流量下可接受的最大并发数 */
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 4;

    /** 空闲线程存活时间 */
    private static final long KEEP_ALIVE_SECONDS = 60L;

    /** 任务队列：生产环境严禁使用无界队列，防止 OOM */
    private static final BlockingQueue<Runnable> WORK_QUEUE =
            new ArrayBlockingQueue<>(1024);

    /** 线程工厂：统一命名、守护线程标记、异常回调 */
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final AtomicLong seq = new AtomicLong(0);
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "biz-pool-" + seq.incrementAndGet());
            t.setDaemon(false);          // 非守护，确保业务执行完才退出
            t.setUncaughtExceptionHandler((th, ex) ->
                    System.err.printf("线程 %s 抛出未捕获异常: %s%n", th.getName(), ex.getMessage()));
            return t;
        }
    };

    /** 拒绝策略：记录日志 + 抛异常给上层，便于快速失败并触发告警 */
    private static final RejectedExecutionHandler REJECT_HANDLER = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            String msg = String.format("Task %s rejected from %s", r.toString(), executor.toString());
            System.err.println(msg);   // 可替换为日志框架
            throw new RejectedExecutionException(msg);
        }
    };

    /** 全局单例线程池 */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_SECONDS,
            TimeUnit.SECONDS,
            WORK_QUEUE,
            THREAD_FACTORY,
            REJECT_HANDLER
    );

    /* 预热线程，减少第一次调用时的延迟 */
    static {
        EXECUTOR.prestartAllCoreThreads();
    }

    /* 不允许外部 new */
    private BizThreadPool() {}

    /** 提交任务：支持 Runnable/Callable */
    public static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return EXECUTOR.submit(task);
    }

    /* ========== 优雅停机 ========== */
    public static void shutdownGracefully() {
        // 1. 停止接收新任务
        EXECUTOR.shutdown();
        try {
            // 2. 等待已有任务完成（最长 30 s）
            if (!EXECUTOR.awaitTermination(30, TimeUnit.SECONDS)) {
                // 3. 超时后强制关闭
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ie) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /* ========== 运行时监控（可对接 Prometheus） ========== */
    public static ThreadPoolExecutor getExecutor() {
        return EXECUTOR;   // 只读监控，禁止外部 shutdown
    }

    /* 使用示例 */
    public static void main(String[] args) {
        execute(() -> System.out.println("hello pool " + Thread.currentThread().getName()));
        shutdownGracefully();
    }
}