package zarg.bank.loader.generator;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
class Person {
    private final String givenName;
    private final String surname;
    private final String emailAddress;

    private Person(Builder builder) {
        givenName = builder.givenName;
        surname = builder.surname;
        emailAddress = builder.emailAddress;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String givenName;
        private String surname;
        private String emailAddress;

        private Builder() {
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
            return new Person(this);
        }
    }
}
