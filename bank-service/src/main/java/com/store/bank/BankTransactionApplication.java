package com.store.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class BankTransactionApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BankTransactionApplication.class, args);
    }
}
