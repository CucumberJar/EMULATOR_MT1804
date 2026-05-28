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

    private static CPU cpu = new CPU();
    private int value = 0;
    private final int[] addressBits = new int[4];
    private final int[] dataBits = new int[4];
    private final int[] muxBits = new int[3];
    private final int[] microAddress = new int[4];
    private final int[] bits = new int[32];
    private final Label[] registerLabels = new Label[16];
    private final Label[][] indicators = new Label[3][4];
    boolean isLoad = true;

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

    @FXML
    private void handleStart() {
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-font-size: 14px; -fx-padding: 10 20;");
        if (!isLoad) {
            System.out.println("ТАКТ");
            cpu.tact();
            for (int i = 0; i < 16; i++) {
                registerLabels[i].setText("R:" + i+":" + cpu.getRegister(i).toString());
            }
            qLabel.setText("Q:" + cpu.getQ());
            fLabel.setText("F:" + cpu.getAlu().getOvr());
            zLabel.setText(String.valueOf(cpu.getFlags()[2]));
            ovrLabel.setText(String.valueOf(cpu.getFlags()[3]));
            f3Label.setText(String.valueOf(cpu.getFlags()[1]));
            c4Label.setText(String.valueOf(cpu.getFlags()[0]));

        }
        PauseTransition pause = new PauseTransition(Duration.millis(120));
        pause.setOnFinished(e -> {
            startButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;");
        });
        pause.play();

    }
    // =========================
// РЕЖИМ РАБОТЫ/ЗАГРУЗКИ
// =========================

    private void setupModeSwitch() {
        modeSwitch.setMinSize(150, 20);

        modeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isLoad = !isLoad;
            if (!isLoad) {
                modeSwitch.setText("РАБОТА");
            } else {
                modeSwitch.setText("ЗАГРУЗКА");
            }
        });
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


    private void createBitTetrads() {
        bitRow.setSpacing(8);
        int index = 0;

        bitRow.getChildren().add(createTetrad("Тетрада 7", createBitBlock(4, index, "Адрес перехода")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 6", createBitBlock(4, index, "Тип перехода")));
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

        bitRow.getChildren().add(createTetrad("Тетрада 2", createBitBlock(4, index, "Адрес A")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 1", createBitBlock(4, index, "Адрес B")));
        index += 4;

        bitRow.getChildren().add(createTetrad("Тетрада 0", createBitBlock(4, index, "Данные")));
    }

    private VBox createBitBlock(int size, int startIndex, String title) {
        VBox block = new VBox(4);
        block.setAlignment(Pos.CENTER);
        block.setPadding(new Insets(4));

        Label label = new Label(title);

        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);

        for (int i = 0; i < size; i++) {
            int idx = startIndex + i;
            Button btn = new Button("0");
            btn.setPrefSize(24, 24);

            btn.setOnAction(e -> {
                bits[idx] ^= 1;
                btn.setText(String.valueOf(bits[idx]));
            });

            row.getChildren().add(btn);
        }

        block.getChildren().addAll(label, row);
        return block;
    }

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
        // Очищаем statusBar и делаем его VBox для двух строк
        statusBar.getChildren().clear();
        statusBar.setSpacing(8);

        // Первая строка: R0-R7
        HBox registersRow = new HBox(4);
        registersRow.setAlignment(Pos.CENTER_LEFT);
        registersRow.setPadding(new Insets(4));

        for (int i = 0; i < 8; i++) {
            Label reg = new Label("R" + i + " 0000");
            reg.setPadding(new Insets(8, 16, 8, 16));
            reg.setMinWidth(100);
            reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");
            registerLabels[i] = reg;
            registersRow.getChildren().add(reg);
        }

        // Вторая строка: R8-R15
        HBox registersRowLow = new HBox(4);
        registersRowLow.setAlignment(Pos.CENTER_LEFT);
        registersRowLow.setPadding(new Insets(4));

        for (int i = 8; i < 16; i++) {
            Label reg = new Label("R" + i + " 0000");
            reg.setPadding(new Insets(8, 16, 8, 16));
            reg.setMinWidth(100);
            reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");
            registerLabels[i] = reg;
            registersRowLow.getChildren().add(reg);
        }

        // Третья строка: Q, F | Z и флаги
        HBox specialRow = new HBox(10);
        specialRow.setAlignment(Pos.CENTER_LEFT);
        specialRow.setPadding(new Insets(4));

        qLabel = new Label("Q: 0000");
        qLabel.setPadding(new Insets(8, 16, 8, 16));
        qLabel.setMinWidth(100);
        qLabel.setAlignment(Pos.CENTER);
        qLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        fLabel = new Label("F: 0000");
        fLabel.setPadding(new Insets(8, 16, 8, 16));
        fLabel.setMinWidth(100);
        fLabel.setAlignment(Pos.CENTER);
        fLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        Region spacer1 = new Region();
        spacer1.setPrefWidth(20);

        zLabel = new Label("Z: 0");
        zLabel.setPadding(new Insets(8, 16, 8, 16));
        zLabel.setMinWidth(70);
        zLabel.setAlignment(Pos.CENTER);
        zLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        f3Label = new Label("F3: 0");
        f3Label.setPadding(new Insets(8, 16, 8, 16));
        f3Label.setMinWidth(70);
        f3Label.setAlignment(Pos.CENTER);
        f3Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        ovrLabel = new Label("OVR: 0");
        ovrLabel.setPadding(new Insets(8, 16, 8, 16));
        ovrLabel.setMinWidth(80);
        ovrLabel.setAlignment(Pos.CENTER);
        ovrLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        c4Label = new Label("C4: 0");
        c4Label.setPadding(new Insets(8, 16, 8, 16));
        c4Label.setMinWidth(70);
        c4Label.setAlignment(Pos.CENTER);
        c4Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px; -fx-border-width: 1;");

        specialRow.getChildren().addAll(qLabel, fLabel, spacer1, zLabel, f3Label, ovrLabel, c4Label);

        // Добавляем все три строки
        statusBar.getChildren().addAll(registersRow, registersRowLow, specialRow);
    }

    private void updateMicroHex() {
        value = 0;
        for (int i = 0; i < 4; i++) {
            value <<= 1;
            value |= microAddress[i];
        }
        microHexLabel.setText("Адрес: " + value);
    }


    @FXML
    private void handleLoad() {
        if (isLoad){
        cpu.load(value, bits);
        statusLabel.setText("● ЗАГРУЖЕНА");
        System.out.println("ЗАГРУЗКА МИКРОКОМАНДЫ");
        System.out.println(Arrays.toString(bits));
        cpu.getMicroInstruction()[value].decode();
        System.out.println(cpu.getMicroInstruction()[value]);}
        System.out.println("Ошибка, включен режим загрузки");
    }
}