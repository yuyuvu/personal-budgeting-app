package com.github.yuyuvu.personalbudgetingapp.appservices;

import com.github.yuyuvu.personalbudgetingapp.exceptions.CheckedIllegalArgumentException;
import com.github.yuyuvu.personalbudgetingapp.model.User;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.*;

public class DataPersistenceService {
    static Path relationalPathToUserdataFiles = Path.of("userdata_files");
    static Path relationalPathToAnalyticsReportsFiles = Path.of("analytics_reports");
    static Path relationalPathToSnapshotsFiles = Path.of("userdata_snapshots");
    static String dataFileExtension = ".json";
    //static String reportsFilesExtension = ".txt";
    static ObjectMapper jsonObjectMapper = new ObjectMapper();

    static {
        try {
            Files.createDirectories(relationalPathToUserdataFiles);
            Files.createDirectories(relationalPathToAnalyticsReportsFiles);
            Files.createDirectories(relationalPathToSnapshotsFiles);
        } catch (IOException e) {
            printlnRed("Проблемы с созданием директорий для хранения пользовательских данных и сохранения отчётов.");
            throw new RuntimeException(e);
        }
    }

    public static User loadUserdataFromFile(String user) {
        User readUser;
        try (FileReader fr = new FileReader(relationalPathToUserdataFiles.resolve(user+dataFileExtension).toFile())) {
            readUser = jsonObjectMapper.readValue(fr, User.class);
            // println(readUser.toString());
            // println(readUser.getWallet().toString());
        } catch (IOException e) {
            printlnRed("Проблемы с чтением информации из файла пользователя " + user + ".");
            throw new RuntimeException(e);
        }
        return readUser;
    }

    public static void saveUserdataToFile(User user) {
        try (FileWriter fw = new FileWriter(relationalPathToUserdataFiles.resolve(user.getUsername()+dataFileExtension).toFile())) {
            jsonObjectMapper.writeValue(fw, user);
        } catch (IOException e) {
            printlnRed("Проблемы с сохранением информации в файл пользователя.");
            throw new RuntimeException(e);
        }
    }

    public static HashMap<String, String> getRegisteredUsernamesAndPasswords() {
        try (Stream<Path> files = Files.list(relationalPathToUserdataFiles)) {
            return (HashMap<String, String>) files.map(file -> {
                        try {
                            return Files.readString(file);
                        } catch (IOException e) {
                            printlnRed("Проблемы с чтением файлов с данными зарегистрированных пользователей.");
                            throw new RuntimeException(e);
                        }
                    })
                    .map(fileContents -> {
                        try {
                            return Map.entry(jsonObjectMapper.readTree(fileContents).get("username").asString(), jsonObjectMapper.readTree(fileContents).get("password").asString());
                        } catch (JacksonException | NullPointerException e) {
                            printlnRed("Проблемы с парсингом json-файла отдельного пользователя для получения имён и паролей зарегистрированных пользователей. \nФайлы пользователей могли быть не сохранены при экстренном завершении программы. Удалите пустые файлы в ./userdata_files. \nМожно продолжать работу.");
                        }
                        return Map.entry("app_placeholder", "sdhjaksduqieKFDSJHV91yOIAd81ljFOIAs123");
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (IOException e) {
            printlnRed("Проблемы с получением имён зарегистрированных пользователей.");
            throw new RuntimeException(e);
        } catch (ClassCastException e) {
            printlnRed("Проблемы с преобразованием Map в HashMap при загрузке имён и паролей зарегистрированных пользователей.");
            throw new RuntimeException(e);
        }
    }

    public static void makeNewUserWalletFile(String inputNewUsername) {
        try {
            Path potentialPath = relationalPathToUserdataFiles.resolve(inputNewUsername+dataFileExtension);
            if (!Files.exists(potentialPath)) {
                Files.createFile(potentialPath);
            }
        } catch (IOException e) {
            printlnRed("Проблемы с созданием файла нового пользователя.");
            throw new RuntimeException(e);
        }
    }

    public static String saveAnalyticsReportToFile(String fileContent, String fileName, String fileExtension) throws IOException {
        Path pathWhereSave = relationalPathToAnalyticsReportsFiles.resolve(fileName+fileExtension);
        try {
            Files.write(pathWhereSave, fileContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Проблемы с сохранением отчёта в файл.");
        }
        return pathWhereSave.toString();
    }

    public static String saveSnapshotToFile(String fileContent, String fileName) throws IOException {
        Path pathWhereSave = relationalPathToSnapshotsFiles.resolve(fileName+dataFileExtension);
        try {
            Files.write(pathWhereSave, fileContent.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IOException("Проблемы с сохранением отчёта в файл: " + e.getMessage());
        }
        return pathWhereSave.toString();
    }

    public static String loadSnapshotFromFile(String filePath) throws CheckedIllegalArgumentException, IOException {
        String result = "";
        try {
            Path pathToSnapshot = Path.of(filePath);
            if (!Files.exists(pathToSnapshot)) {
                throw new CheckedIllegalArgumentException("Файла по указанному пути не существует.");
            }
            result = Files.readString(pathToSnapshot);
            // println(result);
        } catch (IOException e) {
            throw new IOException("Проблемы с чтением информации из файла снапшота: " + e.getMessage() + ".");
        }
        return result;
    }
}
