package net.catenax.irs.smoketest;

import net.catenax.irs.persistence.BlobPersistence;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntTestConfig {

    @Primary
    @Bean
    public BlobPersistence inMemoryBlobStore() {
        return new IntInMemoryBlobStore();
    }
}
