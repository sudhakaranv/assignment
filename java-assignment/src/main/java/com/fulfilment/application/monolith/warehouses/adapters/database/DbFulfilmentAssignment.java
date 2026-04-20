package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.FulfilmentAssignment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fulfilment_assignment")
public class DbFulfilmentAssignment {

    @Id
    @GeneratedValue
    public Long id;

    public String storeId;
    public String productId;
    public String warehouseBusinessUnitCode;

    public DbFulfilmentAssignment() {}

    public FulfilmentAssignment toDomain() {
        return new FulfilmentAssignment(
                storeId, productId, warehouseBusinessUnitCode);
    }

    public static DbFulfilmentAssignment fromDomain(
            FulfilmentAssignment assignment) {

        var db = new DbFulfilmentAssignment();
        db.storeId = assignment.storeId;
        db.productId = assignment.productId;
        db.warehouseBusinessUnitCode =
                assignment.warehouseBusinessUnitCode;
        return db;
    }
}