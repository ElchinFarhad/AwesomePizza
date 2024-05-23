package adesso.it.awesomepizzaworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AwesomePizzaWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwesomePizzaWorkerApplication.class, args);
	}

}
