Feature: Basic Searching
  As a web surfer,
  I want to search using different search engines.

  @automated @web @chrome @google
  Scenario: Simple Google search
    Given a web browser is on the search engine page
    When the search phrase "aristophanes" is entered
    Then results for "aristophanes" are shown

  @automated @web @chrome @duckduckgo
  Scenario: Simple DuckDuckGo search
    Given a web browser is on the search engine page
    When the search phrase "aristophanes" is entered
    Then results for "aristophanes" are shown