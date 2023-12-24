Feature: Teller API scenarios

  Background:
    * def local_port = karate.properties['local_port']
    * def customer_1_id = karate.properties['customer_1_bid']
    * def account_1_id = karate.properties['account_1_bid']
    * def customer_2_id = karate.properties['customer_2_bid']
    * def account_2_id = karate.properties['account_2_bid']
    * def customer_3_id = karate.properties['customer_3_bid']
    * def account_3_id = karate.properties['account_3_bid']
    * url 'http://localhost:' + local_port + '/teller'
    * header Content-Type = 'application/json'
    * def credit_request =
    """
    {
      "type": "CREDIT",
      "amount": 1.00
    }
    """
    * def debit_request =
    """
    {
      "type": "DEBIT",
      "amount": 1.00
    }
    """

  Scenario: Successful credit to account owned by customer
    * def request_uri = '/' + customer_1_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    * print 'Request body: ', credit_request
    Given path request_uri
    And request credit_request
    When method put
    Then status 200
    * print 'Response: ', response

  Scenario: Successful credit to account not owned by customer
    * def request_uri = '/' + customer_2_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    * print 'Request body: ', credit_request
    Given path request_uri
    And request credit_request
    When method put
    Then status 200
    * print 'Response: ', response

  Scenario: Successful debit by customer from account owned by customer
    * def request_uri = '/' + customer_1_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    * print 'Request body: ', debit_request
    Given path request_uri
    And request debit_request
    When method put
    Then status 200
    * print 'Response: ', response

  Scenario: Failed debit by customer from account not owned by customer
    * def request_uri = '/' + customer_2_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    * print 'Request body: ', debit_request
    Given path request_uri
    And request debit_request
    When method put
    Then status 404
    * print 'Response: ', response

  Scenario: Successful balance enquiry from account owned by customer
    * def request_uri = '/' + customer_1_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    Given path request_uri
    When method get
    Then status 200
    * print 'Response: ', response

  Scenario: Failed balance enquiry from account not owned by customer
    * def request_uri = '/' + customer_2_id + '/' + account_1_id
    * print 'Request URI: ', request_uri
    Given path request_uri
    When method get
    Then status 404
    * print 'Response: ', response

  Scenario: Successful transaction history for accounts owned by customer
    * path '/' + customer_3_id + '/' + account_3_id
    * request credit_request
    * method put
    * print 'Test transaction response: ', response
    * status 200
    * def transaction = response.id
    * print 'Test transaction ID: ', transaction

    * def request_uri = '/' + customer_3_id + '/transactions'
    * print 'Request URI: ', request_uri
    Given path request_uri
    When method get
    Then status 200
    * def ac1 = response[account_3_id]
    And match ac1[0].id == transaction
    And match ac1[0].accountId == account_3_id
    And match ac1[0].type == 'CREDIT'
    And match ac1[0].amount == 1.0
    And match ac1[0].balance == 11.0
    And match ac1[0].userId == customer_3_id
    And match ac1[0].processed == '#present'
    * print 'Response: ', response
