package com.assignment.springjava.customerdetailsstorage.controller;

//import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import com.assignment.springjava.customerdetailsstorage.customer.Customer;

import reactor.core.publisher.Mono;

@RestController
public class CustomerController {

    private final WebClient webClient;

    public CustomerController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://qa2.sunbasedata.com/sunbase/portal/api").build();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("customerList", getCustomerList());
        return "index";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute Customer customer, Model model) {
        String token = authenticate(customer.getUsername(), customer.getPassword());
        if (token != null) {
            model.addAttribute("customer", customer);
            model.addAttribute("customerList", getCustomerList(token));
            return "index";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/createCustomer")
    public String createCustomer(@ModelAttribute Customer customer) {
        String token = customer.getToken();
        if (token != null) {
            createCustomerApi(customer, token);
        }
        return "redirect:/";
    }

    @PostMapping("/updateCustomer")
    public String updateCustomer(@ModelAttribute Customer customer) {
        String token = customer.getToken();
        if (token != null) {
            updateCustomerApi(customer, token);
        }
        return "redirect:/";
    }

    @PostMapping("/deleteCustomer")
    public String deleteCustomer(@ModelAttribute Customer customer) {
        String token = customer.getToken();
        if (token != null) {
            deleteCustomerApi(customer.getUuid(), token);
        }
        return "redirect:/";
    }

    private String authenticate(String username, String password) {
        Mono<String> response = webClient.post()
                .uri("/assignment_auth.jsp")
                .bodyValue("{\"login_id\":\"" + username + "\", \"password\":\"" + password + "\"}")
                .retrieve()
                .bodyToMono(String.class);

        return response.block();
    }

    private void createCustomerApi(Customer customer, String token) {
        webClient.post()
                .uri("/assignment.jsp")
                .header("Authorization", "Bearer " + token)
                .bodyValue(getRequestBody("create", customer))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private void updateCustomerApi(Customer customer, String token) {
        webClient.post()
                .uri("/assignment.jsp")
                .header("Authorization", "Bearer " + token)
                .bodyValue(getRequestBody("update", customer))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private void deleteCustomerApi(String uuid, String token) {
        webClient.post()
                .uri("/assignment.jsp")
                .header("Authorization", "Bearer " + token)
                .bodyValue("{\"cmd\":\"delete\",\"uuid\":\"" + uuid + "\"}")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    private String getCustomerList() {
        return webClient.get()
                .uri("/assignment.jsp?cmd=get_customer_list")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getCustomerList(String token) {
        return webClient.get()
                .uri("/assignment.jsp?cmd=get_customer_list")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private String getRequestBody(String command, Customer customer) {
        return String.format("{\"cmd\":\"%s\",\"uuid\":\"%s\",\"first_name\":\"%s\",\"last_name\":\"%s\",\"street\":\"%s\",\"address\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
                command, customer.getUuid(), customer.getFirstName(), customer.getLastName(), customer.getStreet(),
                customer.getAddress(), customer.getCity(), customer.getState(), customer.getEmail(), customer.getPhone());
    }
}

