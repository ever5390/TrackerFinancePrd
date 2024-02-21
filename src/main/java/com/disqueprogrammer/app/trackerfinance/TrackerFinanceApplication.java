package com.disqueprogrammer.app.trackerfinance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
public class TrackerFinanceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackerFinanceApplication.class, args);
	}

}
