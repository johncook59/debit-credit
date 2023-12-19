Feature: Teller API scenarios

  Background:
    * def local_port = karate.properties['local_port']
    * def customer_1_id = karate.properties['customer_1_bid']
    * def account_1_id = karate.properties['account_1_bid']
    * def customer_2_id = karate.properties['customer_2_bid']
    * def account_2_id = karate.properties['account_2_bid']
    * def customer_3_id = karate.properties['customer_3_bid']
    * def account_3_id = karate.properties['account_3_bid']
    * def base_url = 'http://localhost:' + local_port + '/teller/'
    * header Content-Type = 'application/json'

  Scenario: Successful credit
    * def request_url = base_url + customer_1_id + '/credit'
    * def body =
    """
    {
      "accountId": "#(account_1_id)",
      "amount": 1.00
    }
    """
    * print 'Request URL: ', request_url
    * print 'Request body: ', body
    Given url request_url
    And request body
    When method put
    Then status 200
    * print 'Response: ', response

  Scenario: Successful debit by customer 1 from account 2
    * def request_url = base_url + customer_1_id + '/' + account_1_id + '/debit'
    * def body =
    """
    {
      "amount": 1.00
    }
    """
    * print 'Request URL: ', request_url
    * print 'Request body: ', body
    Given url request_url
    And request body
    When method put
    Then status 200
    * print 'Response: ', response

  Scenario: Illegal debit by customer 1 from account 2
    * def request_url = base_url + customer_1_id + '/' + account_2_id + '/debit'
    * def body =
    """
    {
      "amount": 100.00
    }
    """
    * print 'Request URL: ', request_url
    * print 'Request body: ', body
    Given url request_url
    And request body
    When method put
    Then status 404
    * print 'Response: ', response

  Scenario: Balance enquiry from owned account
    * def request_url = base_url + customer_2_id + '/' + account_2_id + '/balance'
    * print 'Request URL: ', request_url
    Given url request_url
    When method get
    Then status 200
    * print 'Response: ', response

  Scenario: Illegal balance enquiry from account not owned by customer
    * def request_url = base_url + customer_1_id + '/' + account_2_id + '/balance'
    * print 'Request URL: ', request_url
    Given url request_url
    When method get
    Then status 404
    * print 'Response: ', response

  Scenario: Transaction history
    * def body =
    """
    {
      "accountId": "#(account_3_id)",
      "amount": 1.00
    }
    """
    * url base_url + customer_3_id + '/credit'
    * request body
    * method put
    * print 'Test transaction response: ', response
    * status 200
    * def transaction = response.id

    * def request_url = base_url + customer_3_id + '/transactions'
    * print 'Request URL: ', request_url
    Given url request_url
    When method get
    Then status 200
    * print 'Response: ', response
    * def ac1 = response[account_3_id]
    * match ac1[0].id == transaction
    * match ac1[0].accountId == account_3_id
    * match ac1[0].direction == 'CREDIT'
    * match ac1[0].amount == 1.0
    * match ac1[0].balance == 11.0
    * match ac1[0].userId == customer_3_id
    * match ac1[0].processed == '#present'