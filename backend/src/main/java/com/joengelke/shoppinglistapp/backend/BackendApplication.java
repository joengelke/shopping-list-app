package com.joengelke.shoppinglistapp.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}


	@Bean
	public CommandLineRunner clearDatabase(MongoTemplate mongoTemplate) {
		return args -> {
			// Clear all collections or drop the database
			mongoTemplate.getDb().drop();  // This will drop the whole database

			// Optionally, you can delete from specific collections instead:
			// mongoTemplate.remove(new Query(), "yourCollectionName");
			// mongoTemplate.remove(new Query(), "anotherCollection");

			System.out.println("Database cleared.");
		};
	}

}
