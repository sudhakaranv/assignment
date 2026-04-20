package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.api.WarehouseResource;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.api.beans.Warehouse;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject
  WarehouseStore warehouseStore;

  @Inject
  CreateWarehouseOperation createWarehouse;

  @Inject
  ReplaceWarehouseOperation replaceWarehouse;

  @Inject
  ArchiveWarehouseOperation archiveWarehouse;

  // ----------------------------------------------------
  // READ operations
  // ----------------------------------------------------

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream()
            .map(this::toWarehouseResponse)
            .toList();
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse =
            warehouseStore.findByBusinessUnitCode(id);

    if (warehouse == null) {
      throw new WebApplicationException("Warehouse not found: " + id, 404);
    }

    return toWarehouseResponse(warehouse);
  }

  // ----------------------------------------------------
  // CREATE
  // ----------------------------------------------------

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      var domainWarehouse = toDomain(data);
      createWarehouse.create(domainWarehouse);
      return toWarehouseResponse(domainWarehouse);
    } catch (WarehouseDomainException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  // ----------------------------------------------------
  // ARCHIVE
  // ----------------------------------------------------

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    try {
      var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
      warehouse.businessUnitCode = id;
      archiveWarehouse.archive(warehouse);
    } catch (WarehouseDomainException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  // ----------------------------------------------------
  // REPLACE
  // ----------------------------------------------------

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {

    try {
      var newWarehouse = toDomain(data);
      newWarehouse.businessUnitCode = businessUnitCode;

      replaceWarehouse.replace(newWarehouse);

      return toWarehouseResponse(newWarehouse);
    } catch (WarehouseDomainException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  // ----------------------------------------------------
  // MAPPERS
  // ----------------------------------------------------

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(
          Warehouse dto) {

    var warehouse =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

    warehouse.businessUnitCode = dto.getBusinessUnitCode();
    warehouse.location = dto.getLocation();
    warehouse.capacity = dto.getCapacity();
    warehouse.stock = dto.getStock();

    return warehouse;
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {

    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}