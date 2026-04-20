package com.fulfilment.application.monolith.warehouses.domain.ports;

import com.fulfilment.application.monolith.warehouses.domain.models.FulfilmentAssignment;
import java.util.List;

public interface FulfilmentAssignmentStore {

    List<FulfilmentAssignment> findByStore(String storeId);

    List<FulfilmentAssignment> findByStoreAndProduct(
            String storeId, String productId);

    List<FulfilmentAssignment> findByWarehouse(String warehouseBusinessUnitCode);

    void save(FulfilmentAssignment assignment);
}