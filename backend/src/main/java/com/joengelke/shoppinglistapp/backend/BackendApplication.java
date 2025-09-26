package com.joengelke.shoppinglistapp.backend;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.joengelke.shoppinglistapp.backend.model.*;
import com.joengelke.shoppinglistapp.backend.repository.UserRepository;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    @Value("${clearDB:false}")
    private boolean clearDB;
    @Value("${backupDB:false}")
    private boolean backupDB;
    @Value("${loadDB:false}")
    private boolean loadDB;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private GridFsTemplate gridFsTemplate;

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

            setupInitialAdmin();
        };
    }

    @Scheduled(cron = "0 0 * * * *") // backup at the beginning of every hour
    public void performBackup() {
        if (backupDB) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            try {
                System.out.print("Starting hourly backup ... ");

                // Fetch the shopping lists and items from MongoDB
                List<User> users = mongoTemplate.findAll(User.class);
                List<ShoppingList> shoppingLists = mongoTemplate.findAll(ShoppingList.class);
                List<ShoppingItem> shoppingItems = mongoTemplate.findAll(ShoppingItem.class);
                List<ItemSet> itemSets = mongoTemplate.findAll(ItemSet.class);
                List<Recipe> recipes = mongoTemplate.findAll(Recipe.class);

                //Latest backup path
                String latestBackupFilePath = "backup/shopping_backup.json";  // Modify the path as needed
                File latestBackupFile = new File(latestBackupFilePath);
                latestBackupFile.getParentFile().mkdirs();  // Ensure the folder exists

                // Write the data to both the latest and archive backup files
                writeBackupToFile(latestBackupFile, users, shoppingLists, shoppingItems, itemSets, recipes, objectMapper);

                // Backup GridFS files
                saveGridFSFiles();
                System.out.println("Backup completed successfully!");

            } catch (Exception e) {
                throw new RuntimeException("Backup failed",e);
            }
        }
    }

    // Method to load the backup and restore the data into MongoDB
    public void loadBackup() {
        if (loadDB) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                System.out.println("Loading backup...");

                // Define the backup file path
                String backupFilePath = "backup/shopping_backup.json";  // Modify the path as needed
                File backupFile = new File(backupFilePath);

                if (backupFile.exists()) {
                    // Deserialize the JSON backup file
                    FileReader fileReader = new FileReader(backupFile);
                    BackupData backupData = objectMapper.readValue(fileReader, BackupData.class);

                    // Post-process to normalize data
                    for (ShoppingItem item : backupData.getShoppingItems()) {
                        if (item.getTags() == null) item.setTags(new ArrayList<>());
                    }

                    // Initialize recipeFileIds for all recipes
                    for (Recipe recipe : backupData.getRecipes()) {
                        if (recipe.getRecipeFileIds() == null) {
                            recipe.setRecipeFileIds(new ArrayList<>());  // always empty list
                        }
                    }

                    // Restore the shopping lists and items into MongoDB
                    mongoTemplate.dropCollection(User.class);
                    mongoTemplate.dropCollection(ShoppingList.class);  // Optionally clear existing data before restoring
                    mongoTemplate.dropCollection(ShoppingItem.class);
                    mongoTemplate.dropCollection(ItemSet.class);
                    mongoTemplate.dropCollection(Recipe.class);

                    mongoTemplate.insertAll(backupData.getUsers());
                    mongoTemplate.insertAll(backupData.getShoppingLists());
                    mongoTemplate.insertAll(backupData.getShoppingItems());
                    mongoTemplate.insertAll(backupData.getItemSets());
                    mongoTemplate.insertAll(backupData.getRecipes());

                    // Restore GridFS files with original ObjectId
                    File filesDir = new File("backup/files");
                    if (filesDir.exists() && filesDir.isDirectory()) {
                        File[] files = filesDir.listFiles();
                        if (files != null) {
                            // Optional: clear GridFS first
                            mongoTemplate.getDb().getCollection("fs.files").drop();
                            mongoTemplate.getDb().getCollection("fs.chunks").drop();

                            // Map old ObjectId -> new ObjectId (String)
                            Map<String, String> backupIdToNewId = new HashMap<>();

                            for (File file : files) {
                                String name = file.getName(); // e.g., "650c9f2a12345_recipe.docx"
                                String[] parts = name.split("_", 2);
                                if (parts.length < 2) continue;

                                String oldId = parts[0];
                                String filename = parts[1];

                                try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                                    ObjectId newId = gridFsTemplate.store(inputStream, filename, Files.probeContentType(file.toPath()));
                                    backupIdToNewId.put(oldId, newId.toHexString());
                                }
                            }

                            // Update Recipe.recipeFileIds with new ObjectIds
                            for (Recipe recipe : backupData.getRecipes()) {
                                List<String> newFileIds = new ArrayList<>();
                                for (String oldFileId : recipe.getRecipeFileIds()) {
                                    String newFileId = backupIdToNewId.get(oldFileId);
                                    if (newFileId != null) newFileIds.add(newFileId);
                                }
                                recipe.setRecipeFileIds(newFileIds);
                            }

                            // Re-insert updated recipes
                            mongoTemplate.remove(new Query(), Recipe.class); // remove old insert
                            mongoTemplate.insertAll(backupData.getRecipes());
                        }
                    }

                    System.out.println("Backup loaded successfully!");
                } else {
                    System.out.println("No backup file found to load.");
                }

            } catch (IOException e) {
                System.err.println("Error loading backup: " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void onShutdown() {
        if (backupDB) {
            System.out.println("Application is shutting down. Performing final backup...");
            performBackup();
        }
    }

    // Helper method to write backup data to a file
    private void writeBackupToFile(File backupFile, List<User> users, List<ShoppingList> shoppingLists,
                                   List<ShoppingItem> shoppingItems, List<ItemSet> itemSets, List<Recipe> recipes,
                                   ObjectMapper objectMapper) throws IOException {
        try (FileWriter writer = new FileWriter(backupFile)) {
            String backupData = "{\n";
            backupData += "\"users\": " + objectMapper.writeValueAsString(users) + ",\n";
            backupData += "\"shoppingLists\": " + objectMapper.writeValueAsString(shoppingLists) + ",\n";
            backupData += "\"shoppingItems\": " + objectMapper.writeValueAsString(shoppingItems) + ",\n";
            backupData += "\"itemSets\": " + objectMapper.writeValueAsString(itemSets) + ",\n";
            backupData += "\"recipes\": " + objectMapper.writeValueAsString(recipes) + "\n";
            backupData += "}";

            writer.write(backupData);
        }
    }

    private void saveGridFSFiles() throws Exception {
        File backupDir = new File("backup/files");
        backupDir.mkdirs();

        Set<String> existingBackupFiles = new HashSet<>();
        File[] backupFiles = backupDir.listFiles();
        if (backupFiles != null) {
            for (File file : backupFiles) {
                existingBackupFiles.add(file.getName());
            }
        }

        GridFSFindIterable files = gridFsTemplate.find(new Query());
        Set<String> currentGridFSFileNames = new HashSet<>();

        for (GridFSFile file : files) {
            String backupFileName = file.getObjectId().toHexString() + "_" + file.getFilename();
            currentGridFSFileNames.add(backupFileName);

            if (!existingBackupFiles.contains(backupFileName)) {
                File backupFile = new File(backupDir, backupFileName);
                try (InputStream inputStream = gridFsTemplate.getResource(file).getInputStream()) {
                    Files.copy(inputStream, backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        if (backupFiles != null) {
            for (File file : backupFiles) {
                if (!currentGridFSFileNames.contains(file.getName())) {
                    file.delete();
                }
            }
        }
    }

    private void setupInitialAdmin() {
        if (userRepository.findByRolesContaining("ADMIN").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin")); // Replace with your password hashing
            admin.setRoles(List.of("ADMIN", "USER"));
            userRepository.save(admin);

            System.out.println("Initial admin user created: username=admin, password=admin");
        } else {
            System.out.println("Admin user already exists, skipping creation.");
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
        private List<Recipe> recipes;
    }
}
