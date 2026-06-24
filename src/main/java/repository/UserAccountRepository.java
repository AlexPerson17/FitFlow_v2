/*
 * File: UserAccountRepository.java
 * Editor: Travisha Mills
 * Project: FitFlow
 * Date: June 22, 2026
 * Version: 1.1
 * Description:
 * Saves and loads user account login data from CSV storage.
 */

package repository;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserAccountRepository {

    private static final String DATA_FOLDER = "data";
    private static final String FILE_PATH = DATA_FOLDER + "/users.csv";
    private static final String HEADER = "userId,username,passwordHash,email,createdAt";

    public static class AccountRecord {

        public String userId;
        public String username;
        public String passwordHash;
        public String email;
        public LocalDateTime createdAt;

        public AccountRecord(String userId, String username, String passwordHash,
                             String email, LocalDateTime createdAt) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.email = email;
            this.createdAt = createdAt;
        }
    }

    private void makeSureFileExists() {
        // Creates the data folder and users CSV file if missing.
        try {
            Files.createDirectories(Paths.get(DATA_FOLDER));

            if (!Files.exists(Paths.get(FILE_PATH)) || Files.size(Paths.get(FILE_PATH)) == 0) {
                FileWriter writer = new FileWriter(FILE_PATH);
                writer.write(HEADER + "\n");
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("Could not create users file: " + e.getMessage());
        }
    }

    public void saveUser(AccountRecord account) {
        // Appends one user account to users.csv.
        makeSureFileExists();

        try {
            FileWriter writer = new FileWriter(FILE_PATH, true);
            writer.write(account.userId + ","
                    + account.username + ","
                    + account.passwordHash + ","
                    + account.email + ","
                    + account.createdAt + "\n");
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving user: " + e.getMessage());
        }
    }

    public List<AccountRecord> loadUsers() {
        // Loads saved user accounts from users.csv.
        makeSureFileExists();

        List<AccountRecord> users = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

            for (String line : lines) {
                if (line.trim().isEmpty() || line.equals(HEADER)) {
                    continue;
                }

                String[] parts = line.split(",", -1);

                if (parts.length >= 5) {
                    users.add(new AccountRecord(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            LocalDateTime.parse(parts[4])
                    ));
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading users: " + e.getMessage());
        }

        return users;
    }
}