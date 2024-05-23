package adesso.it.awesomepizza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AwesomePizzaApplication {
	public static void main(String[] args) {
		SpringApplication.run(AwesomePizzaApplication.class, args);
	}
}
