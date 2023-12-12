package zarg.debitcredit.controllers;

import java.math.BigDecimal;

public record RegisterCustomerRequest(
        String givenName,
        String surname,
        String emailAddress,
        String password,
        BigDecimal initialBalance) {
    public static final class RegisterCustomerRequestBuilder {
        private String givenName;
        private String surname;
        private String emailAddress;
        private String password;
        private BigDecimal initialBalance;

        private RegisterCustomerRequestBuilder() {
        }

        public static RegisterCustomerRequestBuilder builder() {
            return new RegisterCustomerRequestBuilder();
        }

        public RegisterCustomerRequestBuilder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public RegisterCustomerRequestBuilder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public RegisterCustomerRequestBuilder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public RegisterCustomerRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public RegisterCustomerRequestBuilder initialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public RegisterCustomerRequest build() {
            return new RegisterCustomerRequest(givenName, surname, emailAddress, password, initialBalance);
        }
    }
}
