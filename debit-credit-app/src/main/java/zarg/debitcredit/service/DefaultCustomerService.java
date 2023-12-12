package zarg.debitcredit.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zarg.debitcredit.controllers.CustomerDetails;
import zarg.debitcredit.controllers.RegisterCustomerRequest;
import zarg.debitcredit.dao.CustomerDao;
import zarg.debitcredit.domain.Account;
import zarg.debitcredit.domain.Customer;

import java.util.List;

@Service
class DefaultCustomerService implements CustomerService {

    private static final String FAILED_TO_FIND_CUSTOMER = "Failed to find customer %s";

    private final CustomerDao customerDao;

    DefaultCustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    @Transactional
    @Override
    public Customer registerCustomer(RegisterCustomerRequest request) {

        Account account = Account.builder()
                .balance(request.initialBalance())
                .name("Current")
                .build();
        Customer customer = Customer.builder()
                .givenName(request.givenName())
                .surname(request.surname())
                .emailAddress(request.emailAddress())
                .password(request.password())
                .accounts(List.of(account))
                .build();

        return customerDao.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDetails findCustomerByBid(String customerBid) {
        return new CustomerDetails(customerDao.findByBid(customerBid)
                .orElseThrow(() -> new EntityNotFoundException(FAILED_TO_FIND_CUSTOMER.formatted(customerBid))));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public boolean isAccountOwner(String customerBid, String accountBid) {
        return customerDao.findByBidAndAccountBid(customerBid, accountBid).isPresent();
    }
}
