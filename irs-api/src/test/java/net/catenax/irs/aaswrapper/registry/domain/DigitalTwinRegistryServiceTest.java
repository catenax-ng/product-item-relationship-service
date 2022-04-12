package net.catenax.irs.aaswrapper.registry.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.persistence.BlobPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "local", "test" })
class DigitalTwinRegistryServiceTest {
    private DigitalTwinRegistryClientAsync asyncClient;

    @BeforeEach
    void setUp() {
        this.asyncClient = new DigitalTwinRegistryClientAsync(new DigitalTwinRegistryClientLocalStub());
    }

    @Test
    void getAASDescriptorAsync() throws ExecutionException, InterruptedException {
        final CompletableFuture<AssetAdministrationShellDescriptor> result = this.asyncClient.getAASDescriptorAsync(
                "aasidentifier");

        if (result.isDone()) {
            final AssetAdministrationShellDescriptor aasDescriptor = result.get();
            assertEquals(aasDescriptor.getIdShort(), "idShort");
            assertNotNull(aasDescriptor.getSubmodelDescriptors());
            assertNotNull(aasDescriptor.getDescription());
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        public BlobPersistence inMemoryBlobStore() {
            return new InMemoryBlobStore();
        }
    }
}