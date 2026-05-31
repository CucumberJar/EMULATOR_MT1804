package gui;

import app.CPU;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Arrays;

public class MicrocodeController {

    // =========================
    // ПОЛЯ ДАННЫХ
    // =========================

    private static final CPU cpu = new CPU();
    private int value = 0;
    private final int[] addressBits = new int[4];
    private final int[] dataBits = new int[4];
    private final int[] muxBits = new int[3];
    private final int[] microAddress = new int[4];
    private final int[] bits = new int[32];
    private final Label[] registerLabels = new Label[16];
    private final Label[][] indicators = new Label[3][4];
    private boolean isLoad = true;

    private Label qLabel;
    private Label fLabel;
    private Label zLabel;
    private Label f3Label;
    private Label ovrLabel;
    private Label c4Label;

    // =========================
    // FXML КОМПОНЕНТЫ
    // =========================

    @FXML
    private HBox indicatorRow;
    @FXML
    private VBox statusBar;
    @FXML
    private ToggleButton modeSwitch;
    @FXML
    private Button startButton;
    @FXML
    private Button loadButtonOnPanel;
    @FXML
    private HBox addressRow;
    @FXML
    private HBox dataRow;
    @FXML
    private HBox muxRow;
    @FXML
    private HBox bitRow;
    @FXML
    private HBox microAddressRow;
    @FXML
    private Button loadButton;
    @FXML
    private Label microHexLabel;
    @FXML
    private Label statusLabel;

    // =========================
    // ИНИЦИАЛИЗАЦИЯ
    // =========================

    @FXML
    public void initialize() {
        cpu.start();
        setupModeSwitch();
        createIndicators();
        createStatusBar();
        createAddressButtons();
        createDataButtons();
        createMuxButtons();
        createMicroAddressButtons();
        createBitTetrads();
        updateMicroHex();

        updateLed(indicators[0][0], true);
        updateLed(indicators[0][1], false);
    }

    // =========================
    // ОБРАБОТКА ТАКТА (СИНХРОННЫЙ ВЫВОД)
    // =========================

    @FXML
    private void handleStart() {
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-font-size: 14px; -fx-padding: 10 20;");

        if (!isLoad) {
            System.out.println("ТАКТ СИМУЛЯЦИИ");
            cpu.tact();

            // Принудительно заставляем JavaFX мгновенно обновить экран
            javafx.application.Platform.runLater(() -> {
                try {
                    // Обновление РОН R0 - R15 на экране через рабочий toString() класса Register
                    for (int i = 0; i < 16; i++) {
                        if (cpu.getRegister(i) != null && registerLabels[i] != null) {
                            registerLabels[i].setText("R" + i + ": " + cpu.getRegister(i).toString());
                        }
                    }

                    // Обновление системных регистров и шины АЛУ (F)
                    if (cpu.getQ() != null && qLabel != null) qLabel.setText("Q: " + cpu.getQ().toString());
                    if (cpu.getAlu() != null && fLabel != null) fLabel.setText("F: " + cpu.getAlu().getOut());

                    // Обновление триггерных флагов состояния [C4, F3, Z, OVR]
                    if (cpu.getFlags() != null && cpu.getFlags().length >= 4) {
                        if (c4Label != null)  c4Label.setText("C4: " + cpu.getFlags()[0]);
                        if (f3Label != null) f3Label.setText("F3: " + cpu.getFlags()[1]);
                        if (zLabel != null)   zLabel.setText("Z: " + cpu.getFlags()[2]);
                        if (ovrLabel != null) ovrLabel.setText("OVR: " + cpu.getFlags()[3]);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка перерисовки экрана: " + e.getMessage());
                }
            });

        } else {
            statusLabel.setText("● ОШИБКА: РЕЖИМ ЗАГРУЗКИ");
        }

        PauseTransition pause = new PauseTransition(Duration.millis(120));
        pause.setOnFinished(e -> startButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;"));
        pause.play();
    }

    // =========================
    // РЕЖИМ РАБОТЫ/ЗАГРУЗКИ
    // =========================

    private void setupModeSwitch() {
        modeSwitch.setMinSize(150, 20);
        modeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isLoad = !newVal;
            if (!isLoad) {
                modeSwitch.setText("РАБОТА");
                statusLabel.setText("● ГОТОВ К РАБОТЕ");
                // Аппаратный сброс счётчика команд в 0 при переходе в режим симуляции
                cpu.setMpc(0);
            } else {
                modeSwitch.setText("ЗАГРУЗКА");
                statusLabel.setText("● ОЖИДАНИЕ КОМАНДЫ");
            }
        });
    }

    // =========================
    // ТВОЙ ПОРЯДОК БИТ С ПРАВИЛЬНЫМИ ИНДЕКСАМИ CPU
    // =========================

    private void createBitTetrads() {
        bitRow.getChildren().clear();
        bitRow.setSpacing(8);

        // Передаем размер поля, НАЧАЛЬНЫЙ (минимальный) индекс поля в массиве bits[] и название.
        // Метод createBitBlock создаст кнопки слева направо: от bits[start] до bits[start + size - 1].

        // Тетради БМУ (управляющая часть)
        bitRow.getChildren().add(createTetrad("Тетрада 7", createBitBlock(4, 28, "Адрес перехода"))); // Биты: 28, 29, 30, 31
        bitRow.getChildren().add(createTetrad("Тетрада 6", createBitBlock(4, 24, "Тип перехода")));  // Биты: 24, 25, 26, 27

        // Операционная часть по таблице 2.1 методички
        bitRow.getChildren().add(createTetrad("Тетрада 5",
                createBitBlock(1, 23, "MS2"),       // Бит 23
                createBitBlock(3, 20, "ПРИОП")));   // Биты: 20, 21, 22

        bitRow.getChildren().add(createTetrad("Тетрада 4",
                createBitBlock(1, 19, "MS1"),       // Бит 19
                createBitBlock(3, 16, "ИСТОП")));   // Биты: 16, 17, 18

        bitRow.getChildren().add(createTetrad("Тетрада 3",
                createBitBlock(1, 15, "C0"),        // Бит 15
                createBitBlock(3, 12, "АЛУ")));     // Биты: 12, 13, 14

        bitRow.getChildren().add(createTetrad("Тетрада 2", createBitBlock(4, 8, "А")));            // Биты: 8, 9, 10, 11
        bitRow.getChildren().add(createTetrad("Тетрада 1", createBitBlock(4, 4, "В")));            // Биты: 4, 5, 6, 7
        bitRow.getChildren().add(createTetrad("Тетрада 0", createBitBlock(4, 0, "D")));            // Биты: 0, 1, 2, 3
    }

    private VBox createBitBlock(int size, int startIndex, String title) {
        VBox block = new VBox(4);
        block.setAlignment(Pos.CENTER);
        block.setPadding(new Insets(4));

        Label label = new Label(title);
        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);

        // ПРЯМОЙ ПОРЯДОК: биты заполняются в массив bits слева направо.
        // Левая кнопка на экране -> bits[startIndex]
        // Вторая кнопка -> bits[startIndex + 1]
        // Правая кнопка -> bits[startIndex + size - 1]
        for (int i = 0; i < size; i++) {
            int idx = startIndex + i;
            Button btn = new Button("0");
            btn.setPrefSize(24, 24);
            btn.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");

            btn.setOnAction(e -> {
                bits[idx] ^= 1;
                btn.setText(String.valueOf(bits[idx]));
            });

            row.getChildren().add(btn);
        }

        block.getChildren().addAll(label, row);
        return block;
    }

    // =========================
    // ГЕНЕРАЦИЯ ЭЛЕМЕНТОВ ИНТЕРФЕЙСА
    // =========================


    private VBox createTetrad(String title, VBox... groups) {
        VBox wrapper = new VBox(4);
        wrapper.setPadding(new Insets(6));
        wrapper.setAlignment(Pos.CENTER);

        Label label = new Label(title);
        HBox row = new HBox(4);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(groups);

        wrapper.getChildren().addAll(label, row);
        return wrapper;
    }

    private void createAddressButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = new Button("0");
            btn.setPrefSize(30, 30);
            btn.setOnAction(e -> {
                addressBits[idx] ^= 1;
                btn.setText(String.valueOf(addressBits[idx]));
            });
            addressRow.getChildren().add(btn);
        }
    }

    private void createDataButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = new Button("0");
            btn.setPrefSize(30, 30);
            btn.setOnAction(e -> {
                dataBits[idx] ^= 1;
                btn.setText(String.valueOf(dataBits[idx]));
            });
            dataRow.getChildren().add(btn);
        }
    }

    private void createMuxButtons() {
        for (int i = 0; i < 3; i++) {
            int idx = i;
            Button btn = new Button("0");
            btn.setPrefSize(30, 30);
            btn.setOnAction(e -> {
                muxBits[idx] ^= 1;
                btn.setText(String.valueOf(muxBits[idx]));
            });
            muxRow.getChildren().add(btn);
        }
    }

    private void createMicroAddressButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = new Button("0");
            btn.setPrefSize(30, 30);
            btn.setOnAction(e -> {
                microAddress[idx] ^= 1;
                btn.setText(String.valueOf(microAddress[idx]));
                updateMicroHex();
            });
            microAddressRow.getChildren().add(btn);
        }
    }

    private void createIndicators() {
        for (int group = 0; group < 3; group++) {
            HBox tetrad = new HBox(6);
            tetrad.setAlignment(Pos.CENTER_LEFT);

            for (int bit = 0; bit < 4; bit++) {
                Label led = new Label();
                led.setMinSize(16, 16);
                led.setMaxSize(16, 16);
                led.setStyle("-fx-background-color: #cccccc; -fx-background-radius: 50%;");
                indicators[group][bit] = led;
                tetrad.getChildren().add(led);
            }
            indicatorRow.getChildren().add(tetrad);
        }
    }

    private void updateLed(Label led, boolean value) {
        led.setStyle(value ?
                "-fx-background-color: #00ff00; -fx-background-radius: 50%;" :
                "-fx-background-color: #cccccc; -fx-background-radius: 50%;");
    }

    private void createStatusBar() {
        statusBar.getChildren().clear();
        statusBar.setSpacing(8);

        HBox registersRow = new HBox(4);
        registersRow.setAlignment(Pos.CENTER_LEFT);
        registersRow.setPadding(new Insets(4));

        for (int i = 0; i < 8; i++) {
            Label reg = new Label("R" + i + ": 0");
            reg.setPadding(new Insets(8, 16, 8, 16));
            reg.setMinWidth(100);
            reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");
            registerLabels[i] = reg;
            registersRow.getChildren().add(reg);
        }

        HBox registersRowLow = new HBox(4);
        registersRowLow.setAlignment(Pos.CENTER_LEFT);
        registersRowLow.setPadding(new Insets(4));

        for (int i = 8; i < 16; i++) {
            Label reg = new Label("R" + i + ": 0");
            reg.setPadding(new Insets(8, 16, 8, 16));
            reg.setMinWidth(100);
            reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");
            registerLabels[i] = reg;
            registersRowLow.getChildren().add(reg);
        }

        HBox specialRow = new HBox(10);
        specialRow.setAlignment(Pos.CENTER_LEFT);
        specialRow.setPadding(new Insets(4));

        qLabel = new Label("Q: 0");
        qLabel.setPadding(new Insets(8, 16, 8, 16));
        qLabel.setMinWidth(100);
        qLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        fLabel = new Label("F: 0");
        fLabel.setPadding(new Insets(8, 16, 8, 16));
        fLabel.setMinWidth(100);
        fLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        Region spacer1 = new Region();
        spacer1.setPrefWidth(20);

        zLabel = new Label("Z: 0");
        zLabel.setPadding(new Insets(8, 16, 8, 16));
        zLabel.setMinWidth(70);
        zLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        f3Label = new Label("F3: 0");
        f3Label.setPadding(new Insets(8, 16, 8, 16));
        f3Label.setMinWidth(70);
        f3Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        ovrLabel = new Label("OVR: 0");
        ovrLabel.setPadding(new Insets(8, 16, 8, 16));
        ovrLabel.setMinWidth(80);
        ovrLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        c4Label = new Label("C4: 0");
        c4Label.setPadding(new Insets(8, 16, 8, 16));
        c4Label.setMinWidth(70);
        c4Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        specialRow.getChildren().addAll(qLabel, fLabel, spacer1, zLabel, f3Label, ovrLabel, c4Label);
        statusBar.getChildren().addAll(registersRow, registersRowLow, specialRow);
    }

    private void updateMicroHex() {
        value = 0;
        for (int i = 0; i < 4; i++) {
            value <<= 1;
            value |= microAddress[i];
        }
        microHexLabel.setText("Адрес RAM: " + value);
    }

    @FXML
    private void handleLoad() {
        if (isLoad) {
            cpu.load(value, Arrays.copyOf(bits, bits.length));
            statusLabel.setText("● ЗАГРУЖЕНА");
            System.out.println("ЗАГРУЗКА МИКРОКОМАНДЫ ПО АДРЕСУ: " + value);
            System.out.println(cpu.getMicroInstruction()[value]);
        } else {
            statusLabel.setText("● ОТКАЗАНО: РЕЖИМ РАБОТЫ");
        }
    }
}