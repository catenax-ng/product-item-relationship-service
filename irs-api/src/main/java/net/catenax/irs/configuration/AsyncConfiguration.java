//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Asynchronous thread pool configuration
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 3;
    private static final int QUEUE_CAPACITY = 100;

    /**
     * Custom thread pool executor
     *
     * @return customized executor
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();

        return executor;
    }
}
