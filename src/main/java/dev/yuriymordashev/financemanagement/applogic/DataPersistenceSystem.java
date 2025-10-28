package dev.yuriymordashev.financemanagement.applogic;

import dev.yuriymordashev.financemanagement.userdata.User;
import dev.yuriymordashev.financemanagement.userdata.Wallet;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.databind.ObjectMapper;

import static dev.yuriymordashev.financemanagement.applogic.ColorPrinter.*;

public class DataPersistenceSystem {
    static Path relationalPathToUserdataFiles = Path.of("userdata_files");
    static ObjectMapper jsonObjectMapper = new ObjectMapper();

    static {
        try {
            Files.createDirectories(relationalPathToUserdataFiles);
        } catch (IOException e) {
            printlnRed("Проблемы с созданием директории для пользовательских данных.");
            throw new RuntimeException(e);
        }
    }

    public static User loadUserdataFromFile(String user) {
        User readUser = null;
        try (FileReader fr = new FileReader(relationalPathToUserdataFiles.resolve(user).toFile())) {
            readUser = jsonObjectMapper.readValue(fr, User.class);
        } catch (IOException e) {
            printlnRed("Проблемы с чтением информации из файла пользователя.");
            throw new RuntimeException(e);
        }
        return readUser;
    }


    public static void saveUserdataToFile(User user) {
        try (FileWriter fw = new FileWriter(relationalPathToUserdataFiles.resolve(user.getUsername()).toFile())) {
            jsonObjectMapper.writeValue(fw, user);
        } catch (IOException e) {
            printlnRed("Проблемы с сохранением информации в файл пользователя.");
            throw new RuntimeException(e);
        }
    }

    public static HashSet<String> getRegisteredUsernames() {
        try (Stream<Path> files = Files.list(relationalPathToUserdataFiles)) {
            return files.map(Path::toString).map(file -> {Files.})
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            printlnRed("Проблемы с получением имён зарегистрированных пользователей.");
            throw new RuntimeException(e);
        }
    }

    public static void makeNewUserWalletFile(String inputNewUsername) {
        try {
            Files.createFile(relationalPathToUserdataFiles.resolve(inputNewUsername));
        } catch (IOException e) {
            printlnRed("Проблемы с созданием файла нового пользователя.");
            throw new RuntimeException(e);
        }
    }
}
