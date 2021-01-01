package zarg.bank.loader.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "generator", name = "type", havingValue = "data", matchIfMissing = true)
@Slf4j
public class RandomLoader implements Loader {

    private static final String INSERT_CUSTOMER_SQL = "INSERT INTO public.customer(" +
            "   given_name, surname, password, email_address, version)" +
            "   VALUES (?, ?, 'letmein', ?, 0)";
    private static final String INSERT_ACCOUNT_SQL = "INSERT INTO public.account(" +
            "   balance, name, version)" +
            "   VALUES (10, 'Test Account', 0)";
    private static final String INSERT_CUSTOMER_ACCOUNT_SQL = "INSERT INTO public.customer_account(" +
            "   customer_id, account_id)" +
            "   VALUES (?, ?)";

    private final DataSource dataSource;

    private final Random random = new Random();
    private final List<String> forenames;
    private final List<String> surnames;
    private final List<String> domains;
    private final Set<String> emails;
    private final long count;
    private final long batch;

    public RandomLoader(DataSource dataSource,
                        @Value("${customer.data:classpath:forename.txt}") Resource forenameResource,
                        @Value("${customer.data:classpath:surname.txt}") Resource surnameResource,
                        @Value("${customer.data:classpath:domain.txt}") Resource domainResource,
                        @Value("${customer.count:1000000}") long count,
                        @Value("${customer.batch:10000}") long batch) {
        this.dataSource = dataSource;
        this.forenames = new ArrayList<>(loadNameSet(forenameResource));
        this.surnames = new ArrayList<>(loadNameSet(surnameResource));
        this.domains = new ArrayList<>(loadNameSet(domainResource));

        if (count < batch) {
            batch = count;
        }
        this.count = count;
        this.batch = batch;

        this.emails = new HashSet<>();
    }

    @Override
    public void loadData() {

        long remaining = count;
        while (remaining > 0) {
            long batchCount = Math.min(remaining, batch);
            remaining = (remaining - batch) > 0? remaining - batch : 0;
            log.info("loading {} of {}. {} remaining", batchCount, count, remaining);
            loadBatch(batchCount);
        }
    }

    private void loadBatch(long batchSize) {
        try (Connection connection = dataSource.getConnection()) {
            List<Long> customerIds = loadCustomers(connection, batchSize);
            List<Long> accountIds = loadAccounts(connection, customerIds.size());
            linkCustomersAndAccounts(connection, customerIds, accountIds);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Long> loadCustomers(Connection connection, long batchSize) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CUSTOMER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < batchSize; i++) {
                Person person = createPerson();
                statement.setString(1, person.getGivenName());
                statement.setString(2, person.getSurname());
                statement.setString(3, person.getEmailAddress());
                statement.addBatch();
            }
            statement.executeBatch();

            return getKeys(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Long> loadAccounts(Connection connection, int size) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < size; i++) {
                statement.addBatch();
            }
            statement.executeBatch();

            return getKeys(statement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void linkCustomersAndAccounts(Connection connection, List<Long> customerIds, List<Long> accountIds) {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_CUSTOMER_ACCOUNT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < customerIds.size(); i++) {
                statement.setLong(1, customerIds.get(i));
                statement.setLong(2, accountIds.get(i));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Long> getKeys(PreparedStatement statement) throws SQLException {
        List<Long> keys = new ArrayList<>();
        try (ResultSet rs = statement.getGeneratedKeys()) {
            while (rs.next()) {
                keys.add(rs.getLong(1));
            }
        }
        return keys;
    }

    private Person createPerson() {
        String givenName = forenames.get(random.nextInt(forenames.size()));
        String surname = surnames.get(random.nextInt(surnames.size()));
        return Person.builder()
                .givenName(givenName)
                .surname(surname)
                .emailAddress(createEmailAddress(givenName, surname))
                .build();
    }

    private String createEmailAddress(String givenName, String surname) {
        String domain = domains.get(random.nextInt(domains.size()));
        String emailName = givenName + surname;
        String address = String.format("%s@%s", emailName, domain);
        int suffix = 1;

        while (emails.contains(address)) {
            address = String.format("%s%d@%s", emailName, suffix++, domain);
        }
        emails.add(address);

        return address;
    }

    private Set<String> loadNameSet(Resource resource) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream()))) {
            return reader.lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + resource);
        }
    }
}
