Feature: Retrieve product price

  Scenario: Request at 10:00 a.m. on the 14th for product 35455 for brand 1 (XYZ)
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "2020-06-14T10:00:00Z"
    Then the system should return a price of "35.50" EUR

  Scenario: Request at 4:00 p.m. on the 14th for product 35455 for brand 1 (XYZ)
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "2020-06-14T16:00:00Z"
    Then the system should return a price of "25.45" EUR

  Scenario: Request at 9:00 p.m. on the 14th for product 35455 for brand 1 (XYZ)
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "2020-06-14T21:00:00Z"
    Then the system should return a price of "35.50" EUR

  Scenario: Request at 10:00 a.m. on the 15th for product 35455 for brand 1 (XYZ)
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "2020-06-15T10:00:00Z"
    Then the system should return a price of "30.50" EUR

  Scenario: Request at 9:00 p.m. on the 16th for product 35455 for brand 1 (XYZ)
    Given the system has price configurations set
    When I request the price for product "35455" from brand "1" at "2020-06-16T21:00:00Z"
    Then the system should return a price of "38.95" EUR
