# First stage: JDK with GraalVM
FROM container-registry.oracle.com/graalvm/native-image:21-ol8 AS builder

# Set the working directory to /home/app
WORKDIR /build

# Copy the source code into the image for building
COPY . /build

RUN ./mvnw -Pnative -DskipTests native:compile

# Second stage:
FROM container-registry.oracle.com/os/oraclelinux:8-slim

WORKDIR /app

# Copy the native binary from the build stage
COPY --from=builder build/target/debit-credit-app /app/debit-credit-app

# Run the application
ENTRYPOINT ["/app/debit-credit-app"]
