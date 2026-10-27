Feature: Retrieve product price with failure scenarios

  # Test when the brand does not exist
  Scenario: Request for a non-existent brand
    Given the system has price configurations set
    When I request the price for product "35455" from brand "999" at "2020-06-14T10:00:00Z"
    Then the system should return a "Brand not found" error

  # Test when the product does not exist
  Scenario: Request for a non-existent product
    Given the system has price configurations set
    When I request the price for product "99999" from brand "1" at "2020-06-14T10:00:00Z"
    Then the system should return a "Product not found" error

  # Test when the date format is invalid
  Scenario: Request with incorrect date format
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "14-06-2020 10:00:00"
    Then the system should return a "Invalid date format" error

  # Test when a required parameter is omitted
  Scenario: Request with missing parameters
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at ""
    Then the system should return a "Missing parameter" error
