package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  @Test
  void archiveShouldSetArchivedTimestampForExistingWarehouse() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.001");
	warehouseStore.create(existing);

	ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(warehouseStore);

	useCase.archive(warehouse("MWH.001"));

	assertNotNull(existing.archivedAt);
  }

  @Test
  void archiveShouldRejectMissingWarehouse() {
	ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(new InMemoryWarehouseStore());

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.archive(warehouse("MWH.404")));

	assertEquals("Warehouse does not exist: MWH.404", exception.getMessage());
  }

  @Test
  void archiveShouldRejectAlreadyArchivedWarehouse() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.002");
	existing.archivedAt = LocalDateTime.now();
	warehouseStore.create(existing);

	ArchiveWarehouseUseCase useCase = new ArchiveWarehouseUseCase(warehouseStore);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.archive(warehouse("MWH.002")));

	assertEquals("Warehouse is already archived", exception.getMessage());
  }

  private static Warehouse warehouse(String businessUnitCode) {
	Warehouse warehouse = new Warehouse();
	warehouse.businessUnitCode = businessUnitCode;
	warehouse.location = "AMSTERDAM-001";
	warehouse.capacity = 50;
	warehouse.stock = 25;
	return warehouse;
  }

  private static class InMemoryWarehouseStore implements WarehouseStore {
	private final List<Warehouse> warehouses = new ArrayList<>();

	@Override
	public List<Warehouse> getAll() {
	  return warehouses;
	}

	@Override
	public void create(Warehouse warehouse) {
	  warehouses.add(warehouse);
	}

	@Override
	public void update(Warehouse warehouse) {
	  // no-op for in-memory reference semantics
	}

	@Override
	public void remove(Warehouse warehouse) {
	  warehouses.remove(warehouse);
	}

	@Override
	public Warehouse findByBusinessUnitCode(String buCode) {
	  return warehouses.stream()
			  .filter(warehouse -> warehouse.businessUnitCode.equals(buCode))
			  .findFirst()
			  .orElse(null);
	}
  }
}
