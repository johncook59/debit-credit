package zarg.bank.loader.generator;

public record Person(
    String givenName,
    String surname,
    String emailAddress) {

    public static final class Builder {
        private String givenName;
        private String surname;
        private String emailAddress;

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

        public Person build() {
            return new Person(givenName, surname, emailAddress);
        }
    }
}
