package zarg.debitcredit.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import zarg.debitcredit.domain.TransactionDirection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static zarg.debitcredit.controllers.ControllerTestUtils.NOT_OWNER;
import static zarg.debitcredit.controllers.ControllerTestUtils.OWNER;
import static zarg.debitcredit.controllers.ControllerTestUtils.REGISTER_CUSTOMER_REQUEST;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TellerControllerTest {

    private static final String DEBIT_REQUEST = "{\"amount\": 1.00}";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final Random random = new Random();

    @Test
    public void shouldReadBalanceForOwnedAccount() throws Exception {
        CustomerDetails customer = createCustomer(OWNER);
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get(String.format("/teller/%s/%s/balance", customer.getBid(), customer.getAccounts().get(0))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        BalanceDetails balanceDetails = objectMapper.readValue(result.getResponse().getContentAsString(), BalanceDetails.class);
        assertThat(balanceDetails.getAccountId()).isEqualTo(customer.getAccounts().get(0));
        assertThat(balanceDetails.getBalance()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    public void shouldFailReadBalanceForAccountByAnotherCustomer() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        CustomerDetails mal = createCustomer(NOT_OWNER);
        this.mvc.perform(MockMvcRequestBuilders.get(String.format("/teller/%s/%s/balance", mal.getBid(), owner.getAccounts().get(0))))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldCreditAccountWhenOwnerRequests() throws Exception {
        CustomerDetails customer = createCustomer(OWNER);
        String request = createCreditRequest(customer.getAccounts().get(0));
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.put(
                String.format("/teller/%s/credit", customer.getBid()))
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.getDirection()).isEqualTo(TransactionDirection.CREDIT.name());
        assertThat(transactionDetails.getUserId()).isEqualTo(customer.getBid());
        assertThat(transactionDetails.getAccountId()).isEqualTo(customer.getAccounts().get(0));
        assertThat(transactionDetails.getBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactionDetails.getAmount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldCreditAccountWhenAnyoneRequests() throws Exception {
        CustomerDetails customer = createCustomer(OWNER);
        CustomerDetails anyone = createCustomer(NOT_OWNER);
        String request = createCreditRequest(customer.getAccounts().get(0));

        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.put(String.format("/teller/%s/credit", anyone.getBid()))
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.getDirection()).isEqualTo(TransactionDirection.CREDIT.name());
        assertThat(transactionDetails.getUserId()).isEqualTo(anyone.getBid());
        assertThat(transactionDetails.getAccountId()).isEqualTo(customer.getAccounts().get(0));
        assertThat(transactionDetails.getBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactionDetails.getAmount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldDebitAccountWhenOwnerRequests() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);

        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.put(String.format("/teller/%s/%s/debit", owner.getBid(), owner.getAccounts().get(0)))
                .content(DEBIT_REQUEST)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        TransactionDetails transactionDetails = objectMapper.readValue(result.getResponse().getContentAsString(), TransactionDetails.class);
        assertThat(transactionDetails.getDirection()).isEqualTo(TransactionDirection.DEBIT.name());
        assertThat(transactionDetails.getUserId()).isEqualTo(owner.getBid());
        assertThat(transactionDetails.getAccountId()).isEqualTo(owner.getAccounts().get(0));
        assertThat(transactionDetails.getBalance()).isEqualTo(new BigDecimal("9.00"));
        assertThat(transactionDetails.getAmount()).isEqualTo(new BigDecimal("1.00"));
    }

    @Test
    public void shouldFailToDebitAccountWhenAnyoneRequests() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        CustomerDetails mal = createCustomer(NOT_OWNER);

        this.mvc.perform(MockMvcRequestBuilders.put(String.format("/teller/%s/%s/debit", mal.getBid(), owner.getAccounts().get(0)))
                .content(DEBIT_REQUEST)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void shouldFindTransactions() throws Exception {
        CustomerDetails owner = createCustomer(OWNER);
        String request = createCreditRequest(owner.getAccounts().get(0));
        this.mvc.perform(MockMvcRequestBuilders.put(
                String.format("/teller/%s/credit", owner.getBid()))
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.get("/teller/" + owner.getBid() + "/transactions")
                .param("from", dateTimeFormatter.format(now.minus(1, ChronoUnit.DAYS)))
                .param("to", dateTimeFormatter.format(now)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        Map<String, List<TransactionDetails>> transactions = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {});

        String expectedAccountId = owner.getAccounts().get(0);

        assertThat(transactions.size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).size()).isEqualTo(1);
        assertThat(transactions.get(expectedAccountId).get(0).getUserId()).isEqualTo(owner.getBid());
        assertThat(transactions.get(expectedAccountId).get(0).getBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).get(0).getAmount()).isEqualTo(new BigDecimal("1.00"));
        assertThat(transactions.get(expectedAccountId).get(0).getBalance()).isEqualTo(new BigDecimal("11.00"));
        assertThat(transactions.get(expectedAccountId).get(0).getDirection()).isEqualTo(TransactionDirection.CREDIT.name());
    }

    private CustomerDetails createCustomer(String name) throws Exception {
        String request = String.format(REGISTER_CUSTOMER_REQUEST, name, name, name + random.nextInt(1000) + "@somewhere.com");
        MvcResult result = this.mvc.perform(MockMvcRequestBuilders.post("/customer")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), CustomerDetails.class);
    }

    private String createCreditRequest(String accountId) {
        return String.format("{\"accountId\": \"%s\", \"amount\": 1.00}", accountId);
    }
}