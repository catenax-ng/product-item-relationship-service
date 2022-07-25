//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.job;

import java.util.Arrays;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.services.MeterRegistryService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Define the pointcut and joinpoint for IRS Metrics
 */
@Aspect
@Slf4j
@Component
public class IRSMetricsAspect {

    /**
     * Record the metrics base on annotation
     */
    @Autowired
    private MeterRegistryService registryService;

    // execution(* net.catenax.irs.*.*(..)) &&
    @Before("@annotation(net.catenax.irs.connector.job.IRSMetrics)")
    public final void setMetricsBaseOnState(final JoinPoint joinPoint) throws Throwable {

        final IRSMetrics metrics = ((MethodSignature) joinPoint.getSignature()).getMethod()
                                                                               .getAnnotation(IRSMetrics.class);
        if (metrics.value().length == 0) {
            tryMetricsThroughMethodArgumentParameter(joinPoint);
        } else {
            Arrays.stream(metrics.value()).forEach(m -> {
                log.info("Record metrics for JobState: {}", m);
                registryService.recordJobStateMetric(JobState.value(m));
            });
        }

    }

    private void tryMetricsThroughMethodArgumentParameter(final JoinPoint joinPoint) {
        final Optional<Class[]> optClazz = Optional.of(
                ((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes());
        if (optClazz.isPresent()) {
            return;
        }

        final Class[] clazzTypes = optClazz.get();
        Arrays.stream(clazzTypes).forEach(c -> {
            if (c.isAssignableFrom(MultiTransferJob.class)) {
                Arrays.stream(joinPoint.getArgs()).forEach(p -> {
                    if (p instanceof MultiTransferJob) {
                        registryService.recordJobStateMetric(((MultiTransferJob) p).getJob().getJobState());
                    }
                });
            }
        });
    }
}
