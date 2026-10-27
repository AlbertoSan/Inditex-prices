Feature: Retrieve product price with failure scenarios

  # Test cuando la marca no existe
  Scenario: Request for a non-existent brand
    Given the system has price configurations set
    When I request the price for product "35455" from brand "999" at "2020-06-14T10:00:00Z"
    Then the system should return a "Brand not found" error

  # Test cuando el producto no existe
  Scenario: Request for a non-existent product
    Given the system has price configurations set
    When I request the price for product "99999" from brand "1" at "2020-06-14T10:00:00Z"
    Then the system should return a "Product not found" error

  # Test cuando el formato de fecha es incorrecto
  Scenario: Request with incorrect date format
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "14-06-2020 10:00:00"
    Then the system should return a "Invalid date format" error

  # Test cuando se omite un parámetro obligatorio
  Scenario: Request with missing parameters
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at ""
    Then the system should return a "Missing parameter" error