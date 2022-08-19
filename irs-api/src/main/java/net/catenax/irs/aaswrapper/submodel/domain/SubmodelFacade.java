//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.dto.QuantityDTO;
import net.catenax.irs.dto.RelationshipAspect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for submodel domain
 */
@Slf4j
@Service
@AllArgsConstructor
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param jobData                 relevant job data values
     * @return The Aspect Model for the given submodel
     */
    public AssemblyPartRelationshipDTO getAssemblyPartRelationshipSubmodel(final String submodelEndpointAddress, final JobParameter jobData) {
        final AssemblyPartRelationship submodel = this.submodelClient.getSubmodel(submodelEndpointAddress,
                AssemblyPartRelationship.class);

        log.info("Submodel: {}, childParts {}", submodel.getCatenaXId(), submodel.getChildParts());

        final Set<ChildData> submodelParts = thereAreChildParts(submodel)
                ? new HashSet<>(submodel.getChildParts())
                : Collections.emptySet();

        final String lifecycleContext = jobData.getBomLifecycle();
        if (shouldFilterByLifecycleContext(lifecycleContext)) {
            filterSubmodelPartsByLifecycleContext(submodelParts, lifecycleContext);
        }

        return buildAssemblyPartRelationshipResponse(submodelParts, submodel.getCatenaXId());
    }

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model as JSON-String for the given submodel
     */
    public String getSubmodelRawPayload(final String submodelEndpointAddress) {
        final String submodel = this.submodelClient.getSubmodel(submodelEndpointAddress);
        log.info("Returning Submodel as String: '{}'", submodel);
        return submodel;
    }

    private boolean thereAreChildParts(final AssemblyPartRelationship submodel) {
        return submodel.getChildParts() != null;
    }

    @SuppressWarnings("PMD.NullAssignment")
    private AssemblyPartRelationshipDTO buildAssemblyPartRelationshipResponse(final Set<ChildData> submodelParts,
            final String catenaXId) {
        final Set<ChildDataDTO> childParts = new HashSet<>();
        submodelParts.forEach(childData -> childParts.add(mapToChildDataChildDataDTO(childData)));

        return AssemblyPartRelationshipDTO.builder().catenaXId(catenaXId).childParts(childParts).relationshipAspect(
                RelationshipAspect.AssemblyPartRelationship).build();
    }

    private ChildDataDTO mapToChildDataChildDataDTO(final ChildData childData) {
        final String datatypeURI = thereIsMeasurementUnit(childData) ? childData.getQuantity()
                                                                                .getMeasurementUnit()
                                                                                .getDatatypeURI() : null;
        final String lexicalValue = thereIsMeasurementUnit(childData) ? childData.getQuantity()
                                                                                 .getMeasurementUnit()
                                                                                 .getLexicalValue() : null;
        final QuantityDTO.MeasurementUnitDTO measurementUnitDTO = QuantityDTO.MeasurementUnitDTO.builder()
                                                                                                .datatypeURI(
                                                                                                        datatypeURI)
                                                                                                .lexicalValue(
                                                                                                        lexicalValue)
                                                                                                .build();
        final QuantityDTO quantityDTO = QuantityDTO.builder()
                                                   .quantityNumber(thereIsQuantity(childData)
                                                           ? childData.getQuantity()
                                                                      .getQuantityNumber()
                                                           : null)
                                                   .measurementUnit(measurementUnitDTO)
                                                   .build();
        return ChildDataDTO.builder()
                           .childCatenaXId(childData.getChildCatenaXId())
                           .lifecycleContext(childData.getLifecycleContext().getValue())
                           .assembledOn(childData.getAssembledOn())
                           .lastModifiedOn(childData.getLastModifiedOn())
                           .quantity(quantityDTO)
                           .build();
    }

    private boolean thereIsMeasurementUnit(final ChildData childData) {
        return childData.getQuantity() != null && childData.getQuantity().getMeasurementUnit() != null;
    }

    private boolean thereIsQuantity(final ChildData childData) {
        return childData.getQuantity() != null;
    }

    private void filterSubmodelPartsByLifecycleContext(final Set<ChildData> submodelParts,
            final String lifecycleContext) {
        submodelParts.removeIf(isNotLifecycleContext(lifecycleContext));
    }

    private boolean shouldFilterByLifecycleContext(final String lifecycleContext) {
        return StringUtils.isNotBlank(lifecycleContext);
    }

    private Predicate<ChildData> isNotLifecycleContext(final String lifecycleContext) {
        return childData -> !childData.getLifecycleContext().getValue().equals(lifecycleContext);
    }
}
