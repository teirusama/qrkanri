package org.qrkanri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QrkanriApplication {

	public static void main(String[] args) {
		SpringApplication.run(QrkanriApplication.class, args);
	}

}
