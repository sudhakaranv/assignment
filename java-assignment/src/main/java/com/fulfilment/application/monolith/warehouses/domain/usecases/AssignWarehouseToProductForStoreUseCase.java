package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.FulfilmentAssignmentStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AssignWarehouseToProductForStoreUseCase {

    private final FulfilmentAssignmentStore assignmentStore;

    public AssignWarehouseToProductForStoreUseCase(
            FulfilmentAssignmentStore assignmentStore) {
        this.assignmentStore = assignmentStore;
    }

    public void assign(
            String storeId,
            String productId,
            String warehouseBusinessUnitCode) {

        // Constraint 1:
        // Each Product max 2 Warehouses per Store
        var productAssignments =
                assignmentStore.findByStoreAndProduct(storeId, productId);

        boolean warehouseAlreadyAssignedToProductInStore =
                productAssignments.stream()
                        .anyMatch(a -> a.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode));

        long distinctWarehousesForProductInStore =
                productAssignments.stream()
                        .map(a -> a.warehouseBusinessUnitCode)
                        .distinct()
                        .count();

        if (!warehouseAlreadyAssignedToProductInStore
                && distinctWarehousesForProductInStore >= 2) {
            throw new WarehouseDomainException(
                    "Product can be fulfilled by max 2 warehouses per store");
        }

        // Constraint 2:
        // Each Store max 3 Warehouses
        var storeAssignments = assignmentStore.findByStore(storeId);

        boolean warehouseAlreadyUsedByStore =
                storeAssignments.stream()
                        .anyMatch(a -> a.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode));

        long distinctWarehousesForStore =
                storeAssignments.stream()
                        .map(a -> a.warehouseBusinessUnitCode)
                        .distinct()
                        .count();

        if (!warehouseAlreadyUsedByStore && distinctWarehousesForStore >= 3) {
            throw new WarehouseDomainException(
                    "Store can be fulfilled by max 3 warehouses");
        }

        // Constraint 3:
        // Each Warehouse max 5 Product types
        var warehouseAssignments = assignmentStore.findByWarehouse(warehouseBusinessUnitCode);

        boolean productAlreadyStoredInWarehouse =
                warehouseAssignments.stream()
                        .anyMatch(a -> a.productId.equals(productId));

        long distinctProductsForWarehouse =
                warehouseAssignments.stream()
                        .map(a -> a.productId)
                        .distinct()
                        .count();

        if (!productAlreadyStoredInWarehouse && distinctProductsForWarehouse >= 5) {
            throw new WarehouseDomainException(
                    "Warehouse can store max 5 product types");
        }

        // All constraints satisfied → save assignment
        assignmentStore.save(
                new FulfilmentAssignment(
                        storeId,
                        productId,
                        warehouseBusinessUnitCode));
    }
}