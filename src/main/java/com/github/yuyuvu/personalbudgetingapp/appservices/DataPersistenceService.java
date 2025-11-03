package com.github.yuyuvu.personalbudgetingapp.appservices;

import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnGreen;
import static com.github.yuyuvu.personalbudgetingapp.presentation.ColorPrinter.printlnRed;

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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Класс отвечает за сохранение данных в файловую систему и их загрузку из неё. Например, так
 * сохраняются и загружаются данные кошелька пользователя после выхода из аккаунта и при авторизации
 * в аккаунт. Здесь же сохраняются и загружаются файлы снимков состояния кошелька и отчёты. Также
 * реализовано полное сохранение состояния кошельков после перезапуска приложения. После перезапуска
 * можно снова заходить в уже имеющиеся аккаунты и видеть те же данные. Тем не менее во время работы
 * приложения всё так же хранится в оперативной памяти, как и требуется по ТЗ.
 */
public class DataPersistenceService {
  // Переменные для хранения путей до директорий с файлами приложения
  private static final Path relationalPathToAppdata = Path.of("personal_budgeting_appdata");
  private static final Path relationalPathToUserdataFiles =
      relationalPathToAppdata.resolve(Path.of("userdata_wallets"));
  private static final Path relationalPathToAnalyticsReportsFiles =
      relationalPathToAppdata.resolve(Path.of("analytics_reports"));
  private static final Path relationalPathToSnapshotsFiles =
      relationalPathToAppdata.resolve(Path.of("userdata_snapshots"));

  // Данные кошельков сохраняются в формате json
  private static final String dataFileExtension = ".json";
  private static final ObjectMapper jsonObjectMapper = new ObjectMapper();

  // При загрузке класса создаются директории, где хранятся файлы приложения
  static {
    try {
      Files.createDirectories(relationalPathToAppdata);
      Files.createDirectories(relationalPathToUserdataFiles);
      Files.createDirectories(relationalPathToAnalyticsReportsFiles);
      Files.createDirectories(relationalPathToSnapshotsFiles);
    } catch (IOException e) {
      printlnRed(
          "Проблемы с созданием директорий для хранения данных приложения. "
              + "Проверьте права доступа.");
      throw new RuntimeException(e);
    }
  }

  /**
   * Метод считывает данные кошелька пользователя, а также данные для авторизации из ранее
   * сохранённого файла с данными пользователя. Метод десериализует считанные из json-файла данные в
   * объект класса User. Используется при авторизации (в AuthorizationService) и при переводах.
   */
  public static User loadUserdataFromFile(String user) throws IOException {
    User readUser;
    try (FileReader fr =
        new FileReader(relationalPathToUserdataFiles.resolve(user + dataFileExtension).toFile())) {
      readUser = jsonObjectMapper.readValue(fr, User.class);
    } catch (IOException e) {
      throw new IOException("Проблемы с чтением информации из файла пользователя " + user + ".");
    }
    return readUser;
  }

  /**
   * Метод сохраняет данные кошелька пользователя, а также данные для авторизации в ранее созданный
   * файл для данных пользователя. Метод сериализует объект класса User в json-формат. Используется
   * при регистрации (в AuthorizationService), выходе из аккаунта (Menu) и при переводах.
   */
  public static void saveUserdataToFile(User user) throws IOException {
    try (FileWriter fw =
        new FileWriter(
            relationalPathToUserdataFiles
                .resolve(user.getUsername() + dataFileExtension)
                .toFile())) {
      jsonObjectMapper.writeValue(fw, user);
    } catch (IOException e) {
      throw new IOException(
          "Проблемы с сохранением информации в файл пользователя " + user.getUsername() + ".");
    }
  }

  /**
   * Метод, который используется для получения уже имеющихся имён пользователей, хэшей и солей
   * паролей из файлов пользователей при загрузке класса AuthorizationService, чтобы можно было
   * входить в имеющиеся аккаунты после полного перезапуска приложения.
   */
  public static HashMap<String, String[]> getRegisteredUsernamesAndHashesAndSalts()
      throws Exception {
    // Перебор файлов с данными кошельков, где также хранятся данные для авторизации
    try (Stream<Path> files = Files.list(relationalPathToUserdataFiles)) {
      return (HashMap<String, String[]>)
          files
              .map(
                  file -> {
                    try {
                      return Files.readString(file);
                    } catch (IOException e) {
                      throw new RuntimeException(
                          "Проблемы с чтением файлов с данными зарегистрированных пользователей.");
                    }
                  })
              .map(
                  fileContents -> {
                    try {
                      String[] passwordData = {
                        jsonObjectMapper
                            .readTree(fileContents)
                            .get("passwordData")
                            .get(AuthorizationService.PasswordData.HASH.ordinal())
                            .asString(),
                        jsonObjectMapper
                            .readTree(fileContents)
                            .get("passwordData")
                            .get(AuthorizationService.PasswordData.SALT.ordinal())
                            .asString()
                      };
                      return Map.entry(
                          jsonObjectMapper.readTree(fileContents).get("username").asString(),
                          passwordData);
                    } catch (JacksonException | NullPointerException e) {
                      printlnRed(
                          "Проблемы с десериализацией json-файла отдельного пользователя "
                              + "при получении имён и паролей зарегистрированных пользователей."
                              + "\nФайл пользователя представлен в некорректном формате. "
                              + "Удалите пустые и лишние файлы в ./appdata/userdata_wallets.");
                      printlnGreen("Можно продолжать работу.");
                    }
                    return null;
                  })
              .filter(Objects::nonNull)
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    } catch (ClassCastException e) {
      throw new Exception(
          "Проблемы с преобразованием Map в HashMap "
              + "при загрузке имён и паролей зарегистрированных пользователей.");
    } catch (RuntimeException e) {
      throw new Exception(
          "Проблемы с получением имён зарегистрированных пользователей: " + e.getMessage());
    } catch (IOException e) {
      throw new Exception(
          "Проблемы с получением имён зарегистрированных пользователей. "
              + "Невозможно прочитать директорию с данными кошельков.");
    }
  }

  /**
   * Метод, создающий файл для конкретного пользователя, для хранения данных кошелька и данных для
   * авторизации. Используется при регистрации. Выделение данного метода отдельно также позволяет
   * легче тестировать переводы между пользователями.
   */
  public static String makeNewUserWalletFile(String inputNewUsername) throws IOException {
    String pathWhereSaved = null;
    try {
      Path potentialPath =
          relationalPathToUserdataFiles.resolve(inputNewUsername + dataFileExtension);
      if (!Files.exists(potentialPath)) {
        Files.createFile(potentialPath);
        pathWhereSaved = potentialPath.toString();
      }
    } catch (IOException e) {
      throw new IOException("Проблемы с созданием файла нового пользователя.");
    }
    return pathWhereSaved;
  }

  /**
   * Метод, сохраняющий данные отчёта из AnalyticsService в текстовый файл. Используется в
   * AnalyticsMenu и AnalyticsExtendedMenu.
   */
  public static String saveAnalyticsReportToFile(
      String fileContent, String fileName, String fileExtension) throws IOException {
    Path pathWhereSave = relationalPathToAnalyticsReportsFiles.resolve(fileName + fileExtension);
    try {
      Files.write(
          pathWhereSave,
          fileContent.getBytes(),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new IOException("Проблемы с сохранением отчёта в файл.");
    }
    return pathWhereSave.toString();
  }

  /**
   * Метод, сохраняющий данные снимка состояния кошелька пользователя, подготовленные
   * SnapshotsService, в json-файл. Используется в CheckpointsSaveAndLoadMenu.
   */
  public static String saveSnapshotToFile(String fileContent, String fileName) throws IOException {
    Path pathWhereSave = relationalPathToSnapshotsFiles.resolve(fileName + dataFileExtension);
    try {
      Files.write(
          pathWhereSave,
          fileContent.getBytes(),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new IOException("Проблемы с сохранением отчёта в файл: " + e.getMessage());
    }
    return pathWhereSave.toString();
  }

  /**
   * Метод, загружающий данные из ранее созданного снимка состояния кошелька пользователя из
   * json-файла. Используется в CheckpointsSaveAndLoadMenu.
   */
  public static String loadSnapshotFromFile(String filePath)
      throws CheckedIllegalArgumentException, IOException {
    String result;
    try {
      Path pathToSnapshot = Path.of(filePath);
      if (!Files.exists(pathToSnapshot)) {
        throw new CheckedIllegalArgumentException("Файла по указанному пути не существует.");
      }
      result = Files.readString(pathToSnapshot);
      // println(result);
    } catch (IOException e) {
      throw new IOException(
          "Проблемы с чтением информации из файла снапшота: " + e.getMessage() + ".");
    }
    return result;
  }
}
