package ld.sa_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// component scan pour inclure les classes de configuration dans le package "config" et ses sous-packages
@SpringBootApplication
@ComponentScan(basePackages = {"ld.sa_backend", "ld.sa_backend.config"})
public class SaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaBackendApplication.class, args);
	}

}
