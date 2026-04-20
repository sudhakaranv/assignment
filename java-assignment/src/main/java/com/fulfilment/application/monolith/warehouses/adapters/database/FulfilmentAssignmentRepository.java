package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.FulfilmentAssignmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentRepository
        implements FulfilmentAssignmentStore,
        PanacheRepository<DbFulfilmentAssignment> {

    @Override
    public List<FulfilmentAssignment> findByStore(String storeId) {
        return list("storeId", storeId).stream()
                .map(DbFulfilmentAssignment::toDomain)
                .toList();
    }

    @Override
    public List<FulfilmentAssignment> findByStoreAndProduct(
            String storeId, String productId) {

        return list("storeId = ?1 and productId = ?2", storeId, productId)
                .stream()
                .map(DbFulfilmentAssignment::toDomain)
                .toList();
    }

    @Override
    public List<FulfilmentAssignment> findByWarehouse(String warehouseCode) {
        return list("warehouseBusinessUnitCode", warehouseCode).stream()
                .map(DbFulfilmentAssignment::toDomain)
                .toList();
    }

    @Override
    public void save(FulfilmentAssignment assignment) {
        persist(DbFulfilmentAssignment.fromDomain(assignment));
    }
}