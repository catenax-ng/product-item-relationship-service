//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Application entry point.
 */
@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class IrsApplication {

    /**
     * The IRS API version.
     */
    public static final String API_VERSION = "0.2";

    /**
     * The URL prefix for IRS API URLs.
     */
    public static final String API_PREFIX = "irs";

    /**
     * Executor queue capacity.
     */
    private static final int EXEC_QUEUE_CAPACITY = 500;

    /**
     * Entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(IrsApplication.class, args);
    }

    @Bean
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(EXEC_QUEUE_CAPACITY);
        executor.setThreadNamePrefix(API_PREFIX);
        executor.initialize();

        return executor;
    }
}
