package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {

    Warehouse existing =
            warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);

    if (existing == null) {
      throw new WarehouseDomainException(
              "Warehouse to replace does not exist: "
                      + newWarehouse.businessUnitCode);
    }

    if (existing.archivedAt != null) {
      throw new WarehouseDomainException(
              "Warehouse is already archived: "
                      + newWarehouse.businessUnitCode);
    }

    // Stock must match
    if (!existing.stock.equals(newWarehouse.stock)) {
      throw new WarehouseDomainException(
              "Stock must match when replacing a warehouse");
    }

    // New capacity must handle existing stock
    if (newWarehouse.capacity < existing.stock) {
      throw new WarehouseDomainException(
              "New warehouse capacity cannot accommodate existing stock");
    }

    // Archive old warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);

    // Create replacement
    newWarehouse.createdAt = LocalDateTime.now();
    warehouseStore.create(newWarehouse);
  }
}