package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.databind.ObjectMapper;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class DataPersistenceService {
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
        User readUser;
        try (FileReader fr = new FileReader(relationalPathToUserdataFiles.resolve(user+".json").toFile())) {
            readUser = jsonObjectMapper.readValue(fr, User.class);
        } catch (IOException e) {
            printlnRed("Проблемы с чтением информации из файла пользователя.");
            throw new RuntimeException(e);
        }
        return readUser;
    }


    public static void saveUserdataToFile(User user) {
        try (FileWriter fw = new FileWriter(relationalPathToUserdataFiles.resolve(user.getUsername()+".json").toFile())) {
            jsonObjectMapper.writeValue(fw, user);
        } catch (IOException e) {
            printlnRed("Проблемы с сохранением информации в файл пользователя.");
            throw new RuntimeException(e);
        }
    }

    public static HashSet<String> getRegisteredUsernames() {
        try (Stream<Path> files = Files.list(relationalPathToUserdataFiles)) {
            return files.map(Path::getFileName).map(Path::toString).map(s -> s.substring(0, s.lastIndexOf(".")))
                    .collect(Collectors.toCollection(HashSet::new));
        } catch (IOException e) {
            printlnRed("Проблемы с получением имён зарегистрированных пользователей.");
            throw new RuntimeException(e);
        }
    }

    public static void makeNewUserWalletFile(String inputNewUsername) {
        try {
            Path potentialPath = relationalPathToUserdataFiles.resolve(inputNewUsername+".json");
            if (!Files.exists(potentialPath)) {
                Files.createFile(potentialPath);
            }
        } catch (IOException e) {
            printlnRed("Проблемы с созданием файла нового пользователя.");
            throw new RuntimeException(e);
        }
    }
}
