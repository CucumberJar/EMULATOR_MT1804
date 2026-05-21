package gui;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
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

public class MicrocodeController {

    // =========================
    // ПОЛЯ ДАННЫХ
    // =========================

    private final int[] addressBits = new int[4];
    private final int[] dataBits = new int[4];
    private final int[] muxBits = new int[3];
    private final int[] microAddress = new int[4];
    private final int[] bits = new int[32];
    private final Label[] registerLabels = new Label[16];
    private final Label[][] indicators = new Label[3][4];

    private Label qLabel;
    private Label zLabel;
    private Label f3Label;
    private Label ovrLabel;
    private Label c4Label;

    // =========================
    // FXML КОМПОНЕНТЫ
    // =========================

    @FXML private HBox indicatorRow;
    @FXML private HBox statusBar;
    @FXML private ToggleButton modeSwitch;
    @FXML private Button startButton;
    @FXML private Button loadButtonOnPanel;
    @FXML private HBox addressRow;
    @FXML private HBox dataRow;
    @FXML private HBox muxRow;
    @FXML private HBox bitRow;
    @FXML private HBox microAddressRow;
    @FXML private Button loadButton;
    @FXML private Label microHexLabel;
    @FXML private Label statusLabel;

    // =========================
    // ИНИЦИАЛИЗАЦИЯ
    // =========================

    @FXML
    public void initialize() {
        createIndicators();
        createStatusBar();
        createAddressButtons();
        createDataButtons();
        createMuxButtons();
        setupModeSwitch();
        createMicroAddressButtons();
        createBitTetrads();
        updateMicroHex();

        // Тестовые индикаторы
        updateLed(indicators[0][0], true);
        updateLed(indicators[0][1], false);
    }

    // =========================
    // СОЗДАНИЕ КНОПОК УПРАВЛЕНИЯ
    // =========================

    private void createAddressButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = createControlBitButton();
            btn.setOnAction(e -> {
                addressBits[idx] ^= 1;
                updateBitButton(btn, addressBits[idx]);
            });
            addressRow.getChildren().add(btn);
        }
    }

    private void createDataButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = createControlBitButton();
            btn.setOnAction(e -> {
                dataBits[idx] ^= 1;
                updateBitButton(btn, dataBits[idx]);
            });
            dataRow.getChildren().add(btn);
        }
    }

    private void createMuxButtons() {
        for (int i = 0; i < 3; i++) {
            int idx = i;
            Button btn = createControlBitButton();
            btn.setOnAction(e -> {
                muxBits[idx] ^= 1;
                updateBitButton(btn, muxBits[idx]);
            });
            muxRow.getChildren().add(btn);
        }
    }

    private void createMicroAddressButtons() {
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = createBitButton();
            btn.setOnAction(e -> {
                microAddress[idx] ^= 1;
                updateBitButton(btn, microAddress[idx]);
                updateMicroHex();
            });
            microAddressRow.getChildren().add(btn);
        }
    }

    // =========================
    // СОЗДАНИЕ ТЕТРАД (ГРУПП БИТОВ)
    // =========================

    private void createBitTetrads() {
        bitRow.setSpacing(10);
        int index = 0;

        bitRow.getChildren().add(createTetrad("Тетрада 7",
                createBitBlock(4, index, "Адрес перехода")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 6",
                createBitBlock(4, index, "Тип перехода")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 5",
                createBitBlock(1, index, "MS2"),
                createBitBlock(3, index + 1, "Приемник")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 4",
                createBitBlock(1, index, "MS1"),
                createBitBlock(3, index + 1, "Источник")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 3",
                createBitBlock(1, index, "C0"),
                createBitBlock(3, index + 1, "АЛУ")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 2",
                createBitBlock(4, index, "Адрес A")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 1",
                createBitBlock(4, index, "Адрес B")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 0",
                createBitBlock(4, index, "Данные")));
    }

    private VBox createBitBlock(int size, int startIndex, String title) {
        VBox block = new VBox(4);
        block.setAlignment(Pos.CENTER);
        block.getStyleClass().add("bit-block");
        block.setPadding(new Insets(6));

        Label label = new Label(title);
        label.getStyleClass().add("bit-block-title");

        HBox row = new HBox(4);
        row.setSpacing(4);
        row.setAlignment(Pos.CENTER);

        for (int i = 0; i < size; i++) {
            int idx = startIndex + i;
            Button btn = new Button("0");
            btn.getStyleClass().add("bit-button");
            btn.setPrefSize(26, 26);

            btn.setOnAction(e -> {
                bits[idx] ^= 1;
                btn.setText(String.valueOf(bits[idx]));
                if (bits[idx] == 1) {
                    setOnStyle(btn);
                } else {
                    setOffStyle(btn);
                }
            });

            row.getChildren().add(btn);
        }

        block.getChildren().addAll(label, row);
        return block;
    }

    private VBox createTetrad(String title, VBox... groups) {
        VBox wrapper = new VBox(6);
        wrapper.setPadding(new Insets(8));
        wrapper.setAlignment(Pos.CENTER);
        wrapper.getStyleClass().add("tetrad");

        Label label = new Label(title);
        label.getStyleClass().add("tetrad-title");

        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(groups);

        wrapper.getChildren().addAll(label, row);

        return wrapper;
    }

    // =========================
    // ИНДИКАТОРЫ
    // =========================

    private void createIndicators() {
        for (int group = 0; group < 3; group++) {
            HBox tetrad = new HBox(8);
            tetrad.setAlignment(Pos.CENTER_LEFT);
            tetrad.getStyleClass().add("indicator-tetrad");

            for (int bit = 0; bit < 4; bit++) {
                Label led = createLed(false);
                indicators[group][bit] = led;
                tetrad.getChildren().add(led);
            }

            indicatorRow.getChildren().add(tetrad);
        }
    }

    private Label createLed(boolean value) {
        Label led = new Label();
        led.setMinSize(18, 18);
        led.setMaxSize(18, 18);
        led.getStyleClass().add("led");
        updateLed(led, value);
        return led;
    }

    private void updateLed(Label led, boolean value) {
        if (value) {
            led.getStyleClass().removeAll("led-off");
            if (!led.getStyleClass().contains("led-on")) {
                led.getStyleClass().add("led-on");
            }
        } else {
            led.getStyleClass().removeAll("led-on");
            if (!led.getStyleClass().contains("led-off")) {
                led.getStyleClass().add("led-off");
            }
        }
    }

    // =========================
    // СТАТУС БАР (РЕГИСТРЫ И ФЛАГИ)
    // =========================

    private void createStatusBar() {
        // Регистры R0-R15
        for (int i = 0; i < 16; i++) {
            Label reg = createRegisterLabel("R" + i, "0000");
            registerLabels[i] = reg;
            statusBar.getChildren().add(reg);
        }

        // Регистр Q
        qLabel = createRegisterLabel("Q", "0");
        statusBar.getChildren().add(qLabel);

        // Разделитель
        Region spacer = new Region();
        spacer.setPrefWidth(20);
        statusBar.getChildren().add(spacer);

        // Флаги
        zLabel = createFlagLabel("Z", false);
        f3Label = createFlagLabel("F3", false);
        ovrLabel = createFlagLabel("OVR", false);
        c4Label = createFlagLabel("C4", false);

        statusBar.getChildren().addAll(zLabel, f3Label, ovrLabel, c4Label);
    }

    private Label createRegisterLabel(String name, String value) {
        Label label = new Label(name + " " + value);
        label.getStyleClass().add("register-label");
        return label;
    }

    private Label createFlagLabel(String name, boolean value) {
        Label label = new Label();
        updateFlag(label, name, value);
        return label;
    }

    private void updateFlag(Label label, String name, boolean value) {
        if (value) {
            label.setText(name + ":1");
            label.getStyleClass().removeAll("flag-off");
            if (!label.getStyleClass().contains("flag-on")) {
                label.getStyleClass().add("flag-on");
            }
        } else {
            label.setText(name + ":0");
            label.getStyleClass().removeAll("flag-on");
            if (!label.getStyleClass().contains("flag-off")) {
                label.getStyleClass().add("flag-off");
            }
        }
    }

    // =========================
    // ВСПОМОГАТЕЛЬНЫЕ КНОПКИ И БИТЫ
    // =========================

    private Button createControlBitButton() {
        Button btn = new Button("0");
        btn.getStyleClass().add("control-bit-button");
        btn.setPrefSize(30, 30);
        setOffStyle(btn);
        return btn;
    }

    private Button createBitButton() {
        Button btn = new Button("0");
        btn.getStyleClass().add("microaddr-bit-button");
        btn.setPrefSize(30, 30);
        return btn;
    }

    private void updateBitButton(Button btn, int value) {
        btn.setText(String.valueOf(value));
        if (value == 1) {
            setOnStyle(btn);
        } else {
            setOffStyle(btn);
        }
    }

    // =========================
    // СТИЛИ КНОПОК
    // =========================

    private void setOnStyle(Button btn) {
        btn.getStyleClass().removeAll("bit-off");
        if (!btn.getStyleClass().contains("bit-on")) {
            btn.getStyleClass().add("bit-on");
        }
    }

    private void setOffStyle(Button btn) {
        btn.getStyleClass().removeAll("bit-on");
        if (!btn.getStyleClass().contains("bit-off")) {
            btn.getStyleClass().add("bit-off");
        }
    }

    // =========================
    // РЕЖИМ РАБОТЫ/ЗАГРУЗКИ
    // =========================

    private void setupModeSwitch() {
        modeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                modeSwitch.setText("РАБОТА");
                modeSwitch.getStyleClass().removeAll("mode-load");
                if (!modeSwitch.getStyleClass().contains("mode-run")) {
                    modeSwitch.getStyleClass().add("mode-run");
                }
            } else {
                modeSwitch.setText("ЗАГРУЗКА");
                modeSwitch.getStyleClass().removeAll("mode-run");
                if (!modeSwitch.getStyleClass().contains("mode-load")) {
                    modeSwitch.getStyleClass().add("mode-load");
                }
            }
        });
    }

    // =========================
    // ОБНОВЛЕНИЕ HEX
    // =========================

    private void updateMicroHex() {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value <<= 1;
            value |= microAddress[i];
        }
        microHexLabel.setText("Адрес: " + value);

    }

    // =========================
    // ОБРАБОТЧИКИ СОБЫТИЙ
    // =========================

    @FXML
    private void onStart() {
        startButton.getStyleClass().add("start-pressed");

        PauseTransition pause = new PauseTransition(Duration.millis(120));
        pause.setOnFinished(e -> {
            startButton.getStyleClass().remove("start-pressed");
        });
        pause.play();

        System.out.println("ТАКТ");
    }

    @FXML
    public void onLoad(ActionEvent actionEvent) {
        // Здесь будет логика загрузки микрокоманды
        statusLabel.setText("● ЗАГРУЖЕНА");
        statusLabel.getStyleClass().removeAll("status-not-loaded");
        if (!statusLabel.getStyleClass().contains("status-loaded")) {
            statusLabel.getStyleClass().add("status-loaded");
        }
        System.out.println("ЗАГРУЗКА МИКРОКОМАНДЫ");
    }

    @FXML
    public void onStart(ActionEvent actionEvent) {
        onStart();
    }
}
