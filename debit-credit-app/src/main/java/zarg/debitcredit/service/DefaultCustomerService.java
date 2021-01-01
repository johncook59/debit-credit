package zarg.debitcredit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import zarg.debitcredit.controllers.CustomerDetails;
import zarg.debitcredit.controllers.RegisterCustomerRequest;
import zarg.debitcredit.dao.CustomerDao;
import zarg.debitcredit.domain.Account;
import zarg.debitcredit.domain.Customer;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;

@Service
class DefaultCustomerService implements CustomerService {

    private final CustomerDao customerDao;

    DefaultCustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    @Transactional
    @Override
    public Customer registerCustomer(RegisterCustomerRequest request) {

        Account account = Account.builder()
                .balance(request.getInitialBalance())
                .name("Current")
                .build();
        Customer customer = Customer.builder()
                .givenName(request.getGivenName())
                .surname(request.getSurname())
                .emailAddress(request.getEmailAddress())
                .password(request.getPassword())
                .accounts(Collections.singletonList(account))
                .build();

        return customerDao.save(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDetails findCustomerByBid(String customerBid) {
        return new CustomerDetails(customerDao.findByBid(customerBid)
                .orElseThrow(() -> new EntityNotFoundException(customerBid)));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public boolean isAccountOwner(String customerBid, String accountBid) {
        return customerDao.findByBidAndAccountBid(customerBid, accountBid).isPresent();
    }
}
