package com.gjq.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 非同期タスクの設定
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // コアスレッド数：スレッドプール作成時に初期化されるスレッド数
        executor.setCorePoolSize(5);
        // 最大スレッド数：スレッドプールの最大スレッド数。待機キューがいっぱいになった後にのみ、コアスレッド数を超えてスレッドが作成されます
        executor.setMaxPoolSize(10);
        // 待機キュー（バッファキュー）：実行タスクを保持するためのキュー
        executor.setQueueCapacity(25);
        // スレッドの生存時間（アイドル時間）：コアスレッド数を超えたスレッドがアイドル状態になってから破棄されるまでの時間
        executor.setKeepAliveSeconds(60);
        // スレッド名の接頭辞：設定により、処理中のタスクが属するスレッドプールの特定が容易になります
        executor.setThreadNamePrefix("Async-");
        // 待機キューがいっぱいになった後の拒絶ポリシー：呼び出し元スレッド（通常はメインスレッド）でタスクを処理します
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // すべてのタスクが終了してからスレッドプールをシャットダウンするように設定
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // スレッドプールを初期化
        executor.initialize();
        return executor;
    }
}