Feature: Casekaro iPhone 16 Pro Covers

  Scenario: Search and scrape available iPhone 16 Pro covers
    Given I open the Casekaro website
    When I navigate to Mobile Covers and search for Apple
    And I search and open the iPhone 16 Pro page
    And I apply the In Stock filter
    Then I scrape the product data and save it as JSON
    And I sort the products by price and print them
