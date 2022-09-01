//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.job;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.LinkedItem;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.exceptions.JsonParseException;
import org.springframework.web.client.RestClientException;

/**
 * Builds relationship array from AAShell
 */
@Slf4j
public class RelationshipProcessor extends AbstractProcessor {

    private final SubmodelFacade submodelFacade;

    public RelationshipProcessor(final AbstractProcessor nextStep,
            final SubmodelFacade submodelFacade) {
        super(nextStep);
        this.submodelFacade = submodelFacade;
    }

    @Override
    public ItemContainer process(final ItemContainer.ItemContainerBuilder itemContainerBuilder, final JobParameter jobData,
            final AASTransferProcess aasTransferProcess, final String itemId) {

        itemContainerBuilder.build().getShells().stream().findFirst().ifPresent(
            shell -> shell.findAssemblyPartRelationshipEndpointAddresses().forEach(address -> {
                try {
                    final List<Relationship> relationships = submodelFacade.getRelationships(address, jobData);
                    processEndpoint(aasTransferProcess, itemContainerBuilder, relationships);
                } catch (RestClientException | IllegalArgumentException e) {
                    log.info("Submodel Endpoint could not be retrieved for Endpoint: {}. Creating Tombstone.",
                            address);
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                } catch (JsonParseException e) {
                    log.info("Submodel payload did not match the expected AspectType. Creating Tombstone.");
                    itemContainerBuilder.tombstone(Tombstone.from(itemId, address, e, retryCount));
                }
            })
        );

        return next(itemContainerBuilder, jobData, aasTransferProcess, itemId);
    }

    private void processEndpoint(final AASTransferProcess aasTransferProcess,
            final ItemContainer.ItemContainerBuilder itemContainer, final List<Relationship> relationships) {
        final List<String> childIds = relationships.stream()
                                                   .map(Relationship::getLinkedItem)
                                                   .map(LinkedItem::getChildCatenaXId)
                                                   .map(GlobalAssetIdentification::getGlobalAssetId)
                                                   .collect(Collectors.toList());
        log.info("Processing Relationships with {} children", childIds.size());

        aasTransferProcess.addIdsToProcess(childIds);
        itemContainer.relationships(relationships);
    }
}
