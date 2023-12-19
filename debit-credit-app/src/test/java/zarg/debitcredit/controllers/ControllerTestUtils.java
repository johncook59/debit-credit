package zarg.debitcredit.controllers;

public class ControllerTestUtils {
    public static final String OWNER = "owner";
    public static final String NOT_OWNER = "not-owner";
    public static final String REGISTER_CUSTOMER_REQUEST = """
            {
                "givenName": "%s",
                "surname": "%s",
                "emailAddress": "%s",
                "password": "letmein",
                "initialBalance": 10.00
            }
            """;

    public static final String DEBIT_REQUEST = """
            {
                "amount": 1.00
            }
            """;

    public static final String CREDIT_REQUEST = """
            {
                "accountId": "%s",
                "amount": 1.00}"
            }
            """;
}