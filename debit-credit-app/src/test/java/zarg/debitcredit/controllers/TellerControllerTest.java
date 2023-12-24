package zarg.debitcredit.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import zarg.debitcredit.domain.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static zarg.debitcredit.controllers.ControllerTestUtils.CREDIT_REQUEST;
import static zarg.debitcredit.controllers.ControllerTestUtils.DEBIT_REQUEST;
import static zarg.debitcredit.controllers.ControllerTestUtils.NOT_OWNER;
import static zarg.debitcredit.controllers.ControllerTestUtils.OWNER;
import static zarg.debitcredit.controllers.ControllerTestUtils.REGISTER_CUSTOMER_REQUEST;

@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Testcontainers
class TellerControllerTest {

    public static final String TRANSACTION_URI = "/teller/%s/%s";
    public static final String STATEMENT_URI = "/teller/%s/transactions";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:15-alpine")
            .withDatabaseName("debit_credit")
            .withInitScript("prereq-sequences.sql");

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final Random random = new Random();

    @Test
    public void shouldReadBalanceForOwnedAccount() throws Exception {
        CustomerDetails customer = createCustomer(OWNER);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get(
                        TRANSACTION_URI.formatted(customer.bid(), customer.accounts().getFirst())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        BalanceDetails balanceDetails = objectMapper.readValue(result.getResponse().getContentAsString(), BalanceDetails.class);
        assertThat(balanceDetails.accountId()).isEqualTo(customer.accounts().getFirst());
        assertThat(balanceDetails.balance()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    public void shouldFailReadBalanceForAccountByAnotherCustomer() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        CustomerDetails mal = createCustomer(NOT_OWNER);
        mvc.perform(MockMvcRequestBuilders.get(
                        TRANSACTION_URI.formatted(mal.bid(), owner.accounts().getFirst())))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldCreditAccountWhenOwnerRequests() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(owner.bid(), owner.accounts().getFirst()))
                        .content(CREDIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.type()).isEqualTo(TransactionType.CREDIT.name());
        assertThat(transactionDetails.userId()).isEqualTo(owner.bid());
        assertThat(transactionDetails.accountId()).isEqualTo(owner.accounts().getFirst());
        assertThat(transactionDetails.balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactionDetails.amount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldCreditAccountWhenAnyoneRequests() throws Exception {
        CustomerDetails customer = createCustomer(OWNER);
        CustomerDetails anyone = createCustomer(NOT_OWNER);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(anyone.bid(), customer.accounts().getFirst()))
                        .content(CREDIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.type()).isEqualTo(TransactionType.CREDIT.name());
        assertThat(transactionDetails.userId()).isEqualTo(anyone.bid());
        assertThat(transactionDetails.accountId()).isEqualTo(customer.accounts().getFirst());
        assertThat(transactionDetails.balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactionDetails.amount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldDebitAccountWhenOwnerRequests() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);

        MvcResult result = mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(owner.bid(), owner.accounts().getFirst()))
                        .content(DEBIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.type()).isEqualTo(TransactionType.DEBIT.name());
        assertThat(transactionDetails.userId()).isEqualTo(owner.bid());
        assertThat(transactionDetails.accountId()).isEqualTo(owner.accounts().getFirst());
        assertThat(transactionDetails.balance()).isEqualTo(new BigDecimal("9.00"));
        assertThat(transactionDetails.amount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldFailToDebitAccountWhenAnyoneRequests() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        CustomerDetails mal = createCustomer(NOT_OWNER);

        mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(mal.bid(), owner.accounts().getFirst()))
                        .content(DEBIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void shouldFindTransactions() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(owner.bid(), owner.accounts().getFirst()))
                        .content(CREDIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get(STATEMENT_URI
                                .formatted(owner.bid()))
                        .param("from", DATE_TIME_FORMATTER.format(now.minusDays(1)))
                        .param("to", DATE_TIME_FORMATTER.format(now)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Map<String, List<TransactionDetails>> transactions = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        String expectedAccountId = owner.accounts().getFirst();

        assertThat(transactions.size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).getFirst().userId()).isEqualTo(owner.bid());
        assertThat(transactions.get(expectedAccountId).getFirst().balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().amount()).isEqualTo(new BigDecimal("1.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().type()).isEqualTo(TransactionType.CREDIT.name());
    }

    @Test
    public void shouldFindAllTransactions() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        this.mvc.perform(MockMvcRequestBuilders.put(
                                TRANSACTION_URI.formatted(owner.bid(), owner.accounts().getFirst()))
                        .content(CREDIT_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get(STATEMENT_URI.formatted(owner.bid())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Map<String, List<TransactionDetails>> transactions = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });

        String expectedAccountId = owner.accounts().getFirst();

        assertThat(transactions.size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).getFirst().userId()).isEqualTo(owner.bid());
        assertThat(transactions.get(expectedAccountId).getFirst().balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().amount()).isEqualTo(new BigDecimal("1.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().balance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).getFirst().type()).isEqualTo(TransactionType.CREDIT.name());
    }

    private CustomerDetails createCustomer(String name) throws Exception {
        String request = REGISTER_CUSTOMER_REQUEST.formatted(name, name, name + random.nextInt(1000) + "@somewhere.com");
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.post("/customer")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDetails.class);
    }
}