package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.FulfilmentAssignment;
import com.fulfilment.application.monolith.warehouses.domain.ports.FulfilmentAssignmentStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AssignWarehouseToProductForStoreUseCaseTest {

  @Test
  void assignShouldPersistAssignmentWhenAllConstraintsAreSatisfied() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    useCase.assign("STORE-001", "PRODUCT-001", "MWH.001");

    assertEquals(1, assignmentStore.assignments.size());
    assertEquals("STORE-001", assignmentStore.assignments.get(0).storeId);
    assertEquals("PRODUCT-001", assignmentStore.assignments.get(0).productId);
    assertEquals("MWH.001", assignmentStore.assignments.get(0).warehouseBusinessUnitCode);
  }

  @Test
  void assignShouldRejectWhenProductAlreadyHasTwoWarehousesInStore() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.002"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    WarehouseDomainException exception = assertThrows(
            WarehouseDomainException.class,
            () -> useCase.assign("STORE-001", "PRODUCT-001", "MWH.003"));

    assertEquals("Product can be fulfilled by max 2 warehouses per store", exception.getMessage());
    assertEquals(2, assignmentStore.assignments.size());
  }

  @Test
  void assignShouldRejectWhenStoreAlreadyUsesThreeDistinctWarehouses() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-002", "MWH.002"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-003", "MWH.003"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    WarehouseDomainException exception = assertThrows(
            WarehouseDomainException.class,
            () -> useCase.assign("STORE-001", "PRODUCT-004", "MWH.004"));

    assertEquals("Store can be fulfilled by max 3 warehouses", exception.getMessage());
    assertEquals(3, assignmentStore.assignments.size());
  }

  @Test
  void assignShouldAllowStoreToReuseAnExistingWarehouseAfterReachingThreeDistinctWarehouses() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-002", "MWH.002"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-003", "MWH.003"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    useCase.assign("STORE-001", "PRODUCT-004", "MWH.003");

    assertEquals(4, assignmentStore.assignments.size());
    assertEquals("MWH.003", assignmentStore.assignments.get(3).warehouseBusinessUnitCode);
  }

  @Test
  void assignShouldRejectWhenWarehouseAlreadyStoresFiveProductTypes() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-002", "PRODUCT-002", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-003", "PRODUCT-003", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-004", "PRODUCT-004", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-005", "PRODUCT-005", "MWH.001"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    WarehouseDomainException exception = assertThrows(
            WarehouseDomainException.class,
            () -> useCase.assign("STORE-006", "PRODUCT-006", "MWH.001"));

    assertEquals("Warehouse can store max 5 product types", exception.getMessage());
    assertEquals(5, assignmentStore.assignments.size());
  }

  @Test
  void assignShouldAllowWarehouseToReuseAnExistingProductTypeAfterReachingFiveTypes() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-002", "PRODUCT-002", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-003", "PRODUCT-003", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-004", "PRODUCT-004", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-005", "PRODUCT-005", "MWH.001"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    useCase.assign("STORE-006", "PRODUCT-005", "MWH.001");

    assertEquals(6, assignmentStore.assignments.size());
    assertEquals("PRODUCT-005", assignmentStore.assignments.get(5).productId);
  }

  @Test
  void assignShouldAllowProductToReuseAnExistingWarehouseAfterReachingTwoDistinctWarehouses() {
    InMemoryFulfilmentAssignmentStore assignmentStore = new InMemoryFulfilmentAssignmentStore();
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.001"));
    assignmentStore.save(new FulfilmentAssignment("STORE-001", "PRODUCT-001", "MWH.002"));

    AssignWarehouseToProductForStoreUseCase useCase =
            new AssignWarehouseToProductForStoreUseCase(assignmentStore);

    useCase.assign("STORE-001", "PRODUCT-001", "MWH.002");

    assertEquals(3, assignmentStore.assignments.size());
    assertEquals("MWH.002", assignmentStore.assignments.get(2).warehouseBusinessUnitCode);
  }

  private static class InMemoryFulfilmentAssignmentStore implements FulfilmentAssignmentStore {
    private final List<FulfilmentAssignment> assignments = new ArrayList<>();

    @Override
    public List<FulfilmentAssignment> findByStore(String storeId) {
      return assignments.stream()
              .filter(assignment -> assignment.storeId.equals(storeId))
              .toList();
    }

    @Override
    public List<FulfilmentAssignment> findByStoreAndProduct(String storeId, String productId) {
      return assignments.stream()
              .filter(assignment -> assignment.storeId.equals(storeId))
              .filter(assignment -> assignment.productId.equals(productId))
              .toList();
    }

    @Override
    public List<FulfilmentAssignment> findByWarehouse(String warehouseBusinessUnitCode) {
      return assignments.stream()
              .filter(assignment -> assignment.warehouseBusinessUnitCode.equals(warehouseBusinessUnitCode))
              .toList();
    }

    @Override
    public void save(FulfilmentAssignment assignment) {
      assignments.add(assignment);
    }
  }
}
