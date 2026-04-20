package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.api.FulfilmentResource;
import com.api.beans.FulfilmentAssignmentRequest;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.usecases.AssignWarehouseToProductForStoreUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@RequestScoped
public class FulfilmentResourceImpl implements FulfilmentResource {

    @Inject
    AssignWarehouseToProductForStoreUseCase assignUseCase;

    @Override
    @Transactional
    public void assignWarehouseToProductForAStore(
            FulfilmentAssignmentRequest request) {

        try {
            assignUseCase.assign(
                    request.getStoreId(),
                    request.getProductId(),
                    request.getWarehouseBusinessUnitCode()
            );
        } catch (WarehouseDomainException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        }
    }
}