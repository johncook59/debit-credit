package zarg.debitcredit.service;

import zarg.debitcredit.controllers.CustomerDetails;
import zarg.debitcredit.controllers.RegisterCustomerRequest;
import zarg.debitcredit.domain.Customer;

public interface CustomerService {
    Customer registerCustomer(RegisterCustomerRequest request);

    CustomerDetails findCustomerByBid(String customerBid);

    boolean isAccountOwner(String customerBid, String accountBid);
}
