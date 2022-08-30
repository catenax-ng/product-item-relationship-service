package org.eclipse.tractusx.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.tractusx.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.component.GlobalAssetIdentification;
import org.eclipse.tractusx.component.Job;
import org.eclipse.tractusx.component.LinkedItem;
import org.eclipse.tractusx.component.RegisterJob;
import org.eclipse.tractusx.component.Relationship;
import org.eclipse.tractusx.component.enums.AspectType;
import org.eclipse.tractusx.component.enums.BomLifecycle;
import org.eclipse.tractusx.component.enums.JobState;
import org.eclipse.tractusx.connector.job.DataRequest;
import org.eclipse.tractusx.connector.job.MultiTransferJob;
import org.eclipse.tractusx.connector.job.ResponseStatus;
import org.eclipse.tractusx.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.connector.job.TransferProcess;
import org.eclipse.tractusx.dto.JobParameter;
import org.eclipse.tractusx.dto.RelationshipAspect;
import org.eclipse.tractusx.services.MeterRegistryService;
import net.datafaker.Faker;
import org.eclipse.tractusx.controllers.IrsAppConstants;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class TestMother {

    private static final String AS_BUILT = "AsBuilt";

    Faker faker = new Faker();

    public static RegisterJob registerJobWithoutDepthAndAspect() {
        return registerJobWithDepthAndAspect(null, null);
    }

    public static RegisterJob registerJobWithoutDepth() {
        return registerJobWithDepthAndAspect(null, List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP));
    }

    public static RegisterJob registerJobWithDepthAndAspect(final Integer depth, final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", depth, aspectTypes,
                false);
    }

    public static RegisterJob registerJobWithDepthAndAspectAndCollectAspects(final Integer depth,
            final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6", depth, aspectTypes,
                true);
    }

    public static RegisterJob registerJobWithGlobalAssetIdAndDepth(final String globalAssetId, final Integer depth,
            final List<AspectType> aspectTypes, final boolean collectAspects) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(globalAssetId);
        registerJob.setDepth(depth);
        registerJob.setAspects(aspectTypes);
        registerJob.setCollectAspects(collectAspects);

        return registerJob;
    }

    public static JobParameter jobParameter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsBuilt")
                           .aspectTypes(List.of(AspectType.SERIAL_PART_TYPIZATION.toString(),
                                   AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()))
                           .build();
    }

    public static JobParameter jobParameterFilter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsRequired")
                           .aspectTypes(List.of(AspectType.MATERIAL_FOR_RECYCLING.toString()))
                           .build();
    }

    public static JobParameter jobParameterEmptyFilter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                           .treeDepth(0)
                           .bomLifecycle("AsRequired")
                           .aspectTypes(List.of())
                           .build();
    }

    public static MeterRegistryService simpleMeterRegistryService() {
        return new MeterRegistryService(new SimpleMeterRegistry());
    }

    public AASTransferProcess aasTransferProcess() {
        return new AASTransferProcess(faker.lorem().characters(IrsAppConstants.UUID_SIZE), faker.number().numberBetween(1, 100));
    }

    public Job fakeJob(JobState state) {
        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                  .jobState(state)
                  .createdOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .owner(faker.lorem().characters())
                  .lastModifiedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .build();
    }

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                               .job(fakeJob(jobState))
                               .jobParameter(jobParameter())
                               .jobParameter(jobParameter())
                               .build();
    }

    public DataRequest dataRequest() {
        return new DataRequest() {
        };
    }

    public TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    public TransferInitiateResponse response(ResponseStatus status) {
        return TransferInitiateResponse.builder().transferId(UUID.randomUUID().toString()).status(status).build();
    }

    public TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }

    public Relationship relationship() {
        final LinkedItem linkedItem = LinkedItem.builder()
                                                .childCatenaXId(GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                                                .lifecycleContext(BomLifecycle.AS_BUILT)
                                                .build();

        return new Relationship(GlobalAssetIdentification.of(UUID.randomUUID().toString()),
                linkedItem,
                RelationshipAspect.AssemblyPartRelationship.name());
    }
}