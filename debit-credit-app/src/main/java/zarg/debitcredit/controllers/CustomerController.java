package zarg.debitcredit.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import zarg.debitcredit.service.CustomerService;

@RestController
@RequestMapping("customer")
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerDetails findByCustomerId(@RequestParam("customerId") String bid) {
        log.info("Finding " + bid);
        Assert.hasText(bid, "Empty bid");

        return customerService.findCustomerByBid(bid);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CustomerDetails register(@RequestBody RegisterCustomerRequest request) {
        log.info("Registering {} {}", request.getGivenName(), request.getSurname());

        return new CustomerDetails(customerService.registerCustomer(request));
    }
}
