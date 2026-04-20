package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentEndpointTest {

  @Test
  public void shouldCreateFulfilmentAssignmentWhenRequestIsValid() {
    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-001", "PRODUCT-ENDPOINT-001", "MWH.001"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(204);
  }

  @Test
  public void shouldRejectThirdDifferentWarehouseForSameProductInStore() {
    assign("STORE-ENDPOINT-002", "PRODUCT-ENDPOINT-002", "MWH.001");
    assign("STORE-ENDPOINT-002", "PRODUCT-ENDPOINT-002", "MWH.012");

    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-002", "PRODUCT-ENDPOINT-002", "MWH.023"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(400);
  }

  @Test
  public void shouldRejectFourthDifferentWarehouseForStore() {
    assign("STORE-ENDPOINT-003", "PRODUCT-ENDPOINT-003", "MWH.001");
    assign("STORE-ENDPOINT-003", "PRODUCT-ENDPOINT-004", "MWH.012");
    assign("STORE-ENDPOINT-003", "PRODUCT-ENDPOINT-005", "MWH.023");

    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-003", "PRODUCT-ENDPOINT-006", "MWH.999"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(400);
  }

  @Test
  public void shouldRejectSixthDifferentProductTypeForWarehouse() {
    assign("STORE-ENDPOINT-101", "PRODUCT-ENDPOINT-101", "MWH.LIMIT-001");
    assign("STORE-ENDPOINT-102", "PRODUCT-ENDPOINT-102", "MWH.LIMIT-001");
    assign("STORE-ENDPOINT-103", "PRODUCT-ENDPOINT-103", "MWH.LIMIT-001");
    assign("STORE-ENDPOINT-104", "PRODUCT-ENDPOINT-104", "MWH.LIMIT-001");
    assign("STORE-ENDPOINT-105", "PRODUCT-ENDPOINT-105", "MWH.LIMIT-001");

    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-106", "PRODUCT-ENDPOINT-106", "MWH.LIMIT-001"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(400);
  }

  @Test
  public void shouldAllowReusingExistingWarehouseWhenStoreAlreadyHasThreeDistinctWarehouses() {
    assign("STORE-ENDPOINT-004", "PRODUCT-ENDPOINT-007", "MWH.001");
    assign("STORE-ENDPOINT-004", "PRODUCT-ENDPOINT-008", "MWH.012");
    assign("STORE-ENDPOINT-004", "PRODUCT-ENDPOINT-009", "MWH.023");

    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-004", "PRODUCT-ENDPOINT-010", "MWH.023"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(204);
  }

  @Test
  public void shouldAllowReusingExistingProductTypeWhenWarehouseAlreadyHasFiveDistinctTypes() {
    assign("STORE-ENDPOINT-201", "PRODUCT-ENDPOINT-201", "MWH.REUSE-001");
    assign("STORE-ENDPOINT-202", "PRODUCT-ENDPOINT-202", "MWH.REUSE-001");
    assign("STORE-ENDPOINT-203", "PRODUCT-ENDPOINT-203", "MWH.REUSE-001");
    assign("STORE-ENDPOINT-204", "PRODUCT-ENDPOINT-204", "MWH.REUSE-001");
    assign("STORE-ENDPOINT-205", "PRODUCT-ENDPOINT-205", "MWH.REUSE-001");

    given()
        .contentType(ContentType.JSON)
        .body(request("STORE-ENDPOINT-206", "PRODUCT-ENDPOINT-205", "MWH.REUSE-001"))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(204);
  }

  private static void assign(String storeId, String productId, String warehouseBusinessUnitCode) {
    given()
        .contentType(ContentType.JSON)
        .body(request(storeId, productId, warehouseBusinessUnitCode))
        .when()
        .post("fulfilment")
        .then()
        .statusCode(204);
  }

  private static Map<String, String> request(
      String storeId, String productId, String warehouseBusinessUnitCode) {
    return Map.of(
        "storeId", storeId,
        "productId", productId,
        "warehouseBusinessUnitCode", warehouseBusinessUnitCode);
  }
}
