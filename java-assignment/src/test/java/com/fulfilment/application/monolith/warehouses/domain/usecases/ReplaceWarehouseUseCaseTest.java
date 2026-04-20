package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  @Test
  void replaceShouldArchiveExistingWarehouseAndCreateReplacement() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.001", "AMSTERDAM-001", 50, 30);
	warehouseStore.create(existing);

	ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(warehouseStore);
	Warehouse replacement = warehouse("MWH.001", "AMSTERDAM-002", 60, 30);

	useCase.replace(replacement);

	assertNotNull(existing.archivedAt);
	assertNotNull(replacement.createdAt);
	assertEquals(2, warehouseStore.getAll().size());
	assertSame(replacement, warehouseStore.getAll().get(1));
  }

  @Test
  void replaceShouldRejectMissingWarehouse() {
	ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(new InMemoryWarehouseStore());

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.replace(warehouse("MWH.404", "AMSTERDAM-001", 50, 10)));

	assertEquals("Warehouse to replace does not exist: MWH.404", exception.getMessage());
  }

  @Test
  void replaceShouldRejectAlreadyArchivedWarehouse() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.002", "AMSTERDAM-001", 50, 25);
	existing.archivedAt = LocalDateTime.now();
	warehouseStore.create(existing);

	ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(warehouseStore);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.replace(warehouse("MWH.002", "AMSTERDAM-002", 70, 25)));

	assertEquals("Warehouse is already archived: MWH.002", exception.getMessage());
	assertEquals(1, warehouseStore.getAll().size());
  }

  @Test
  void replaceShouldRejectWhenStockDoesNotMatch() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.003", "AMSTERDAM-001", 50, 25);
	warehouseStore.create(existing);

	ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(warehouseStore);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.replace(warehouse("MWH.003", "AMSTERDAM-002", 70, 24)));

	assertEquals("Stock must match when replacing a warehouse", exception.getMessage());
	assertNull(existing.archivedAt);
	assertEquals(1, warehouseStore.getAll().size());
  }

  @Test
  void replaceShouldRejectWhenNewCapacityCannotHandleExistingStock() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	Warehouse existing = warehouse("MWH.004", "AMSTERDAM-001", 50, 25);
	warehouseStore.create(existing);

	ReplaceWarehouseUseCase useCase = new ReplaceWarehouseUseCase(warehouseStore);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.replace(warehouse("MWH.004", "AMSTERDAM-002", 24, 25)));

	assertEquals("New warehouse capacity cannot accommodate existing stock", exception.getMessage());
	assertNull(existing.archivedAt);
	assertEquals(1, warehouseStore.getAll().size());
  }

  private static Warehouse warehouse(String businessUnitCode, String location, int capacity, int stock) {
	Warehouse warehouse = new Warehouse();
	warehouse.businessUnitCode = businessUnitCode;
	warehouse.location = location;
	warehouse.capacity = capacity;
	warehouse.stock = stock;
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
