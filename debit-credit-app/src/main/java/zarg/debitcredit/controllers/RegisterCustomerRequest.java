package zarg.debitcredit.controllers;

import java.math.BigDecimal;

public record RegisterCustomerRequest(
        String givenName,
        String surname,
        String emailAddress,
        String password,
        BigDecimal initialBalance) {

    public static final class Builder {
        private String givenName;
        private String surname;
        private String emailAddress;
        private String password;
        private BigDecimal initialBalance;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder givenName(String givenName) {
            this.givenName = givenName;
            return this;
        }

        public Builder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public Builder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder initialBalance(BigDecimal initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public RegisterCustomerRequest build() {
            return new RegisterCustomerRequest(givenName, surname, emailAddress, password, initialBalance);
        }
    }
}
