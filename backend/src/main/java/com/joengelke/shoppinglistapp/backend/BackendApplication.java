package com.joengelke.shoppinglistapp.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joengelke.shoppinglistapp.backend.model.ItemSet;
import com.joengelke.shoppinglistapp.backend.model.ShoppingItem;
import com.joengelke.shoppinglistapp.backend.model.ShoppingList;
import com.joengelke.shoppinglistapp.backend.model.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    private final boolean clearDB = false;
    private final boolean backupDB = false;
    private final boolean loadDB = true;
    @Autowired
    private MongoTemplate mongoTemplate;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner clearDatabase() {

        return args -> {
            if (clearDB) {
                // Clear all collections or drop the database
                mongoTemplate.getDb().drop();  // This will drop the whole database

                // Optionally, you can delete from specific collections instead:
                // mongoTemplate.remove(new Query(), "yourCollectionName");
                // mongoTemplate.remove(new Query(), "anotherCollection");

                System.out.println("Database cleared.");
            }
            if (backupDB) {
                performBackup();
            }
            if (loadDB) {
                loadBackup();
            }
        };

    }

    @Scheduled(fixedRate = 2 * 60 * 1000) // 2 min
    public void performBackup() {
        if (backupDB) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                System.out.println("Starting backup...");

                // Fetch the shopping lists and items from MongoDB
                List<User> users = mongoTemplate.findAll(User.class);
                List<ShoppingList> shoppingLists = mongoTemplate.findAll(ShoppingList.class);
                List<ShoppingItem> shoppingItems = mongoTemplate.findAll(ShoppingItem.class);
                List<ItemSet> itemSets = mongoTemplate.findAll(ItemSet.class);

                // Serialize the data to a file (as an example)
                String backupFilePath = "backup/shopping_backup.json";  // Modify the path as needed
                File backupFile = new File(backupFilePath);
                backupFile.getParentFile().mkdirs();  // Ensure the folder exists

                // Write the data to a file (you can use a more sophisticated serialization if needed)
                try (FileWriter writer = new FileWriter(backupFile)) {
                    // Create a map to hold the data
                    String backupData = "{\n";
                    backupData += "\"users\": " + objectMapper.writeValueAsString(users) + ",\n";
                    backupData += "\"shoppingLists\": " + objectMapper.writeValueAsString(shoppingLists) + ",\n";
                    backupData += "\"shoppingItems\": " + objectMapper.writeValueAsString(shoppingItems) + ",\n";
                    backupData += "\"itemSets\": " + objectMapper.writeValueAsString(itemSets) + "\n";
                    backupData += "}";

                    writer.write(backupData);
                }

                System.out.println("Backup completed: " + backupFilePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Method to load the backup and restore the data into MongoDB
    public void loadBackup() {
        if (loadDB) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                System.out.println("Loading backup...");

                // Define the backup file path
                String backupFilePath = "backup/shopping_backup.json";  // Modify the path as needed
                File backupFile = new File(backupFilePath);

                if (backupFile.exists()) {
                    // Deserialize the JSON backup file
                    FileReader fileReader = new FileReader(backupFile);
                    BackupData backupData = objectMapper.readValue(fileReader, BackupData.class);

                    // Restore the shopping lists and items into MongoDB
                    mongoTemplate.dropCollection(User.class);
                    mongoTemplate.dropCollection(ShoppingList.class);  // Optionally clear existing data before restoring
                    mongoTemplate.dropCollection(ShoppingItem.class);
                    mongoTemplate.dropCollection(ItemSet.class);


                    mongoTemplate.insertAll(backupData.getUsers());
                    mongoTemplate.insertAll(backupData.getShoppingLists());
                    mongoTemplate.insertAll(backupData.getShoppingItems());
                    mongoTemplate.insertAll(backupData.getItemSets());

                    System.out.println("Backup loaded successfully!");

                } else {
                    System.out.println("No backup file found to load.");
                }

            } catch (IOException e) {
                System.err.println("Error loading backup: " + e.getMessage());
            }
        }
    }

    // Helper class to map the backup JSON structure
    @Setter
    @Getter
    private static class BackupData {
        private List<User> users;
        private List<ShoppingList> shoppingLists;
        private List<ShoppingItem> shoppingItems;
        private List<ItemSet> itemSets;
    }


}
