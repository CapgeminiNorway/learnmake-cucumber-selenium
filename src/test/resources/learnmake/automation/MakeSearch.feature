Feature: Basic Searching
  As a web surfer,
  I want to search using different search engines.

  @web @chrome @google
  Scenario: Simple search with Google
    Given a web browser is on the search engine page
    When the search phrase "aristophanes" is entered
    Then results for "aristophanes" are shown

  @web @chrome @duckduckgo
  Scenario: Simple search with DuckDuckGo
    Given a web browser is on the search engine page
    When the search phrase "aristophanes" is entered
    Then results for "aristophanes" are shown

  @web @firefox @duckduckgo
  Scenario Outline: Multiple search with DuckDuckGo
    Given a web browser is on the search engine page
    When the search phrase "<search_keyword>" is entered
    Then results for "<search_keyword>" are shown

    Examples:
      | search_keyword |
      | Ovidius        |
      | Seneca         |
