package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver
  ) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {

    // Business Unit Code must be unique
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseDomainException(
              "Warehouse with business unit code already exists: " + warehouse.businessUnitCode
      );
    }

    // Location must exist
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WarehouseDomainException(
              "Invalid location: " + warehouse.location
      );
    }

    // Max number of warehouses per location
    long warehousesAtLocation =
            warehouseStore.getAll().stream()
                    .filter(w -> warehouse.location.equals(w.location) && w.archivedAt == null)
                    .count();

    if (warehousesAtLocation >= location.maxNumberOfWarehouses) {
      throw new WarehouseDomainException(
              "Maximum number of warehouses reached for location " + warehouse.location
      );
    }

    // Capacity validation
    if (warehouse.capacity > location.maxCapacity) {
      throw new WarehouseDomainException(
              "Warehouse capacity exceeds location max capacity"
      );
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new WarehouseDomainException(
              "Warehouse stock exceeds its capacity"
      );
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(warehouse);
  }
}