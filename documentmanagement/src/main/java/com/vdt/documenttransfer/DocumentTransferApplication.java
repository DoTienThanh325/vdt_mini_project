package com.vdt.documenttransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class DocumentTransferApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentTransferApplication.class, args);
	}

}
