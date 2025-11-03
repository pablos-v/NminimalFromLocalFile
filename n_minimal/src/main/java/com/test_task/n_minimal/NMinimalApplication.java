package com.test_task.n_minimal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Приложение для вывода минимального значения N из 1 столбца указанной таблицы Excel
 * Работает в веб-интерфейсе Swagger
 */
@SpringBootApplication
public class NMinimalApplication {

	public static void main(String[] args) {
		SpringApplication.run(NMinimalApplication.class, args);
	}

}
