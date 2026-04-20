package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseDomainException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  @Test
  void createShouldPersistWarehouseAndSetCreationTimestamp() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	StaticLocationResolver locationResolver = new StaticLocationResolver();
	locationResolver.add(new Location("AMSTERDAM-001", 3, 100));

	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
	Warehouse warehouse = warehouse("MWH.100", "AMSTERDAM-001", 80, 45);

	useCase.create(warehouse);

	assertNotNull(warehouse.createdAt);
	assertEquals(1, warehouseStore.getAll().size());
	assertSame(warehouse, warehouseStore.getAll().get(0));
  }

  @Test
  void createShouldRejectDuplicateBusinessUnitCode() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	warehouseStore.create(warehouse("MWH.001", "AMSTERDAM-001", 50, 20));

	StaticLocationResolver locationResolver = new StaticLocationResolver();
	locationResolver.add(new Location("AMSTERDAM-001", 3, 100));

	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.create(warehouse("MWH.001", "AMSTERDAM-001", 70, 20)));

	assertEquals("Warehouse with business unit code already exists: MWH.001", exception.getMessage());
	assertEquals(1, warehouseStore.getAll().size());
  }

  @Test
  void createShouldRejectUnknownLocation() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, identifier -> null);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.create(warehouse("MWH.002", "UNKNOWN-001", 40, 10)));

	assertEquals("Invalid location: UNKNOWN-001", exception.getMessage());
	assertEquals(0, warehouseStore.getAll().size());
  }

  @Test
  void createShouldRejectWhenLocationReachedMaximumActiveWarehouses() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	warehouseStore.create(warehouse("MWH.001", "ZWOLLE-001", 20, 10));

	Warehouse archivedWarehouse = warehouse("MWH.ARCH", "ZWOLLE-001", 20, 10);
	archivedWarehouse.archivedAt = java.time.LocalDateTime.now();
	warehouseStore.create(archivedWarehouse);

	StaticLocationResolver locationResolver = new StaticLocationResolver();
	locationResolver.add(new Location("ZWOLLE-001", 1, 50));

	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.create(warehouse("MWH.003", "ZWOLLE-001", 20, 10)));

	assertEquals("Maximum number of warehouses reached for location ZWOLLE-001", exception.getMessage());
	assertEquals(2, warehouseStore.getAll().size());
  }

  @Test
  void createShouldRejectWhenCapacityExceedsLocationLimit() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	StaticLocationResolver locationResolver = new StaticLocationResolver();
	locationResolver.add(new Location("HELMOND-001", 2, 45));

	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.create(warehouse("MWH.004", "HELMOND-001", 46, 10)));

	assertEquals("Warehouse capacity exceeds location max capacity", exception.getMessage());
	assertEquals(0, warehouseStore.getAll().size());
  }

  @Test
  void createShouldRejectWhenStockExceedsWarehouseCapacity() {
	InMemoryWarehouseStore warehouseStore = new InMemoryWarehouseStore();
	StaticLocationResolver locationResolver = new StaticLocationResolver();
	locationResolver.add(new Location("EINDHOVEN-001", 2, 70));

	CreateWarehouseUseCase useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);

	WarehouseDomainException exception = assertThrows(
			WarehouseDomainException.class,
			() -> useCase.create(warehouse("MWH.005", "EINDHOVEN-001", 40, 41)));

	assertEquals("Warehouse stock exceeds its capacity", exception.getMessage());
	assertEquals(0, warehouseStore.getAll().size());
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

  private static class StaticLocationResolver implements LocationResolver {
	private final Map<String, Location> locations = new HashMap<>();

	void add(Location location) {
	  locations.put(location.identification, location);
	}

	@Override
	public Location resolveByIdentifier(String identifier) {
	  return locations.get(identifier);
	}
  }
}
