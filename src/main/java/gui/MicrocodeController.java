package gui;

import alu.MicroInstruction;
import app.CPU;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Arrays;

public class MicrocodeController {

    private static final CPU cpu = new CPU();
    private int value = 0;
    private final int[] addressBits = new int[4];
    private final int[] dataBits = new int[4];
    private final int[] muxBits = new int[3];
    private final int[] microAddress = new int[4];

    // Порядок битов строго визуальный (слева направо):
    // 0..3   -> Тетрада 7 (BR)
    // 4..7   -> Тетрада 6 (P_CODE)
    // ...
    // 28..31 -> Тетрада 0 (D)
    private final int[] bits = new int[32];
    private final int[] rgmkBits = new int[32];

    private final Label[] registerLabels = new Label[16];
    private final Label[][] indicators = new Label[3][4];
    private final Label[][] bitLabels = new Label[3][4];
    private boolean isLoad = true;

    private Label qLabel; private Label fLabel; private Label zLabel;
    private Label f3Label; private Label ovrLabel; private Label c4Label;

    @FXML private ListView<String> microInstructionList;
    @FXML private HBox indicatorRow;
    @FXML private VBox statusBar;
    @FXML private ToggleButton modeSwitch;
    @FXML private Button startButton;
    @FXML private HBox addressRow;
    @FXML private HBox dataRow;
    @FXML private HBox muxRow;
    @FXML private HBox bitRow;
    @FXML private HBox microAddressRow;
    @FXML private Label microHexLabel;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        cpu.start();
        setupModeSwitch();
        createIndicators();
        createStatusBar();
        createMuxButtons();
        createMicroAddressButtons();
        createBitTetrads();
        updateMicroHex();
        updateInstructionListView(); // Добавлен вызов при старте
    }

    @FXML
    private void handleStart() {
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-font-size: 14px; -fx-padding: 10 20;");
        if (!isLoad) {
            int currentMpc = cpu.getMpc();
            if (currentMpc >= 0 && currentMpc < 16) {
                MicroInstruction currentInstr = cpu.getMicroInstruction()[currentMpc];
                if (currentInstr != null) {
                    System.arraycopy(currentInstr.getInstructionBits(), 0, rgmkBits, 0, 32);
                }
            }

            cpu.tact();

            javafx.application.Platform.runLater(() -> {
                try {
                    for (int i = 0; i < 16; i++) {
                        if (cpu.getRegister(i) != null && registerLabels[i] != null) {
                            registerLabels[i].setText("R" + i + ": " + cpu.getRegister(i).toString());
                        }
                    }
                    if (cpu.getQ() != null && qLabel != null) qLabel.setText("Q: " + cpu.getQ().toString());
                    if (cpu.getAlu() != null && fLabel != null) fLabel.setText("F: " + cpu.getAlu().getOuStr());

                    int[] flags = cpu.getFlags();
                    if (flags != null && flags.length >= 4) {
                        c4Label.setText("C4: " + flags[0]);
                        f3Label.setText("F3: " + flags[1]);
                        zLabel.setText("Z: " + flags[2]);
                        ovrLabel.setText("OVR: " + flags[3]);
                    }

                    refreshAllIndicators();
                    updateInstructionListView(); // Обновление списка после такта
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            statusLabel.setText("● ОШИБКА: РЕЖИМ ЗАГРУЗКИ");
        }
        PauseTransition pause = new PauseTransition(Duration.millis(120));
        pause.setOnFinished(e -> startButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20;"));
        pause.play();
    }

    private void setupModeSwitch() {
        modeSwitch.setMinSize(150, 20);
        modeSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            isLoad = !newVal;
            if (!isLoad) {
                modeSwitch.setText("РАБОТА");
                statusLabel.setText("● ГОТОВ К РАБОТЕ");
                cpu.setMpc(0);
                MicroInstruction zeroInstruction = cpu.getMicroInstruction()[0];
                if (zeroInstruction != null) {
                    System.arraycopy(zeroInstruction.getInstructionBits(), 0, rgmkBits, 0, 32);
                }
            } else {
                modeSwitch.setText("ЗАГРУЗКА");
                statusLabel.setText("● ОЖИДАНИЕ КОМАНДЫ");
            }
            refreshAllIndicators();
            updateInstructionListView(); // Обновление списка при смене режима
        });
    }

    @FXML
    private void handleLoad() {
        if (isLoad) {
            cpu.load(value, Arrays.copyOf(bits, bits.length));
            System.arraycopy(bits, 0, rgmkBits, 0, 32);
            statusLabel.setText("● ЗАГРУЖЕНА");
            refreshAllIndicators();
            updateInstructionListView(); // Обновление списка после загрузки команды
        } else {
            statusLabel.setText("● ОТКАЗАНО: РЕЖИМ РАБОТЫ");
        }
    }

    private void updateMicroHex() {
        value = 0;
        for (int i = 0; i < 4; i++) {
            value <<= 1;
            value |= microAddress[i];
        }
        microHexLabel.setText("Адрес RAM: " + value);
        cpu.updateHardwareAddressBus(value);
        refreshAllIndicators();
    }

    // =========================================================================
    // ВОЗВРАЩЕННЫЙ КЛАССИЧЕСКИЙ ВЫВОД МИКРОКОМАНД В ВИДЕ БИТ
    // =========================================================================
    private void updateInstructionListView() {
        if (microInstructionList == null) return;

        ObservableList<String> items = FXCollections.observableArrayList();
        MicroInstruction[] instructions = cpu.getMicroInstruction();

        for (int i = 0; i < 16; i++) {
            MicroInstruction instr = (i < instructions.length) ? instructions[i] : null;
            if (instr == null || instr.getInstructionBits() == null) {
                items.add(String.format("0x%02X: [ пустая ячейка ]", i));
            } else {
                int[] b = instr.getInstructionBits();
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < 32; j++) {
                    sb.append(b[j]);
                    // Добавляем разделительный пробел после каждой тетрады (каждые 4 бита)
                    if ((j + 1) % 4 == 0 && j < 31) {
                        sb.append(" ");
                    }
                }
                items.add(String.format("0x%02X: %s", i, sb.toString()));
            }
        }

        microInstructionList.setItems(items);
        if (!isLoad) {
            microInstructionList.getSelectionModel().select(cpu.getMpc());
        }
    }

    private void refreshAllIndicators() {
        int muxCode = 0;
        if (muxBits[0] == 1) muxCode |= 1;
        if (muxBits[1] == 1) muxCode |= 2;
        if (muxBits[2] == 1) muxCode |= 4;

        updateBitLabelsValue(muxCode);

        // ТЕТРАДА 0: ДАННЫЕ (Вывод системных шин)
        boolean[] dataLedStates = new boolean[4];
        switch (muxCode) {
            case 0:
                int mpcVal = cpu.getMpc();
                for (int b = 0; b < 4; b++) dataLedStates[b] = ((mpcVal >> (3 - b)) & 1) == 1;
                break;
            case 1:
                int aluOut = cpu.getAlu() != null ? cpu.getAlu().getOutInt() : 0;
                for (int b = 0; b < 4; b++) dataLedStates[b] = ((aluOut >> (3 - b)) & 1) == 1;
                break;
            case 2:
                int[] flags = cpu.getFlags();
                if (flags != null && flags.length >= 4) {
                    dataLedStates[0] = flags[0] == 1;
                    dataLedStates[1] = flags[3] == 1;
                    dataLedStates[2] = flags[1] == 1;
                    dataLedStates[3] = flags[2] == 1;
                }
                break;
            case 7:
                if (cpu.getQ() != null) {
                    int qVal = cpu.getQ().getValue();
                    for (int b = 0; b < 4; b++) dataLedStates[b] = ((qVal >> (3 - b)) & 1) == 1;
                }
                break;
            default:
                Arrays.fill(dataLedStates, false);
                break;
        }
        for (int b = 0; b < 4; b++) updateLed(indicators[0][b], dataLedStates[b]);

        int startBit = muxCode * 4;
        for (int b = 0; b < 4; b++) {
            int bitIndex = startBit + b;
            boolean mkState = (bitIndex < rgmkBits.length) && (rgmkBits[bitIndex] == 1);
            updateLed(indicators[1][b], mkState);
        }

        MicroInstruction ramInstructionObject = cpu.getMicroInstruction()[value];
        if (ramInstructionObject != null && ramInstructionObject.getInstructionBits() != null) {
            int[] ramInstruction = ramInstructionObject.getInstructionBits();
            for (int b = 0; b < 4; b++) {
                int bitIndex = startBit + b;
                boolean ramState = (bitIndex < ramInstruction.length) && (ramInstruction[bitIndex] == 1);
                updateLed(indicators[2][b], ramState);
            }
        } else {
            for (int b = 0; b < 4; b++) updateLed(indicators[2][b], false);
        }
    }

    private void updateBitLabelsValue(int currentMuxCode) {
        int startBit = currentMuxCode * 4;
        for (int group = 0; group < 3; group++) {
            for (int bit = 0; bit < 4; bit++) {
                if (bitLabels[group][bit] != null) {
                    int actualBitNumber = startBit + bit;
                    bitLabels[group][bit].setText(String.valueOf(actualBitNumber));
                }
            }
        }
    }

    private void updateLed(Label led, boolean value) {
        if (led != null) {
            led.setStyle(value ?
                    "-fx-background-color: #2fff00; -fx-background-radius: 50%; -fx-effect: dropshadow(three-pass-box, rgba(34,255,0,0.6), 5, 0, 0, 0);" :
                    "-fx-background-color: #4a4a4a; -fx-background-radius: 50%;");
        }
    }

    private void createStatusBar() {
        statusBar.getChildren().clear();
        statusBar.setSpacing(8);
        HBox registersRow = new HBox(4); registersRow.setAlignment(Pos.CENTER_LEFT); registersRow.setPadding(new Insets(4));
        for (int i = 0; i < 8; i++) {
            Label reg = new Label("R" + i + ": 0"); reg.setPadding(new Insets(8, 16, 8, 16)); reg.setMinWidth(100); reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
            registerLabels[i] = reg; registersRow.getChildren().add(reg);
        }
        HBox registersRowLow = new HBox(4); registersRowLow.setAlignment(Pos.CENTER_LEFT); registersRowLow.setPadding(new Insets(4));
        for (int i = 8; i < 16; i++) {
            Label reg = new Label("R" + i + ": 0"); reg.setPadding(new Insets(8, 16, 8, 16)); reg.setMinWidth(100); reg.setAlignment(Pos.CENTER);
            reg.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
            registerLabels[i] = reg; registersRowLow.getChildren().add(reg);
        }
        HBox specialRow = new HBox(10); specialRow.setAlignment(Pos.CENTER_LEFT); specialRow.setPadding(new Insets(4));
        qLabel = new Label("Q: 0"); qLabel.setPadding(new Insets(8, 16, 8, 16)); qLabel.setMinWidth(100);
        qLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        fLabel = new Label("F: 0"); fLabel.setPadding(new Insets(8, 16, 8, 16)); fLabel.setMinWidth(100);
        fLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        Region spacer1 = new Region(); spacer1.setPrefWidth(20);
        zLabel = new Label("Z: 0"); zLabel.setPadding(new Insets(8, 16, 8, 16)); zLabel.setMinWidth(70);
        zLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        f3Label = new Label("F3: 0"); f3Label.setPadding(new Insets(8, 16, 8, 16)); f3Label.setMinWidth(70);
        f3Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        ovrLabel = new Label("OVR: 0"); ovrLabel.setPadding(new Insets(8, 16, 8, 16)); ovrLabel.setMinWidth(80);
        ovrLabel.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        c4Label = new Label("C4: 0"); c4Label.setPadding(new Insets(8, 16, 8, 16)); c4Label.setMinWidth(70);
        c4Label.setStyle("-fx-border-color: #cccccc; -fx-background-color: #f8f8f8; -fx-font-size: 14px;");
        specialRow.getChildren().addAll(qLabel, fLabel, spacer1, zLabel, f3Label, ovrLabel, c4Label);
        statusBar.getChildren().addAll(registersRow, registersRowLow, specialRow);
    }

    private void createIndicators() {
        indicatorRow.getChildren().clear();
        indicatorRow.setSpacing(35);
        indicatorRow.setAlignment(Pos.CENTER_LEFT);

        String[] groupNames = { "ДАННЫЕ (Выходной мультиплексор)", "МИКРОКОМАНДА (Регистр РГМК)", "ПАМЯТЬ (ОЗУ Микропрограмм)" };

        for (int group = 0; group < 3; group++) {
            VBox groupContainer = new VBox(8);
            groupContainer.setAlignment(Pos.CENTER);

            Label groupLabel = new Label(groupNames[group]);
            groupLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
            groupContainer.getChildren().add(groupLabel);

            HBox ledRow = new HBox(15);
            ledRow.setAlignment(Pos.CENTER);
            HBox bitLabelsRow = new HBox(15);
            bitLabelsRow.setAlignment(Pos.CENTER);

            for (int bit = 0; bit < 4; bit++) {
                Label led = new Label();
                led.setMinSize(22, 22); led.setMaxSize(22, 22);
                led.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 50%; -fx-effect: inset 0 1px 3px rgba(0,0,0,0.5);");
                indicators[group][bit] = led;
                ledRow.getChildren().add(led);

                Label bitLabel = new Label("0");
                bitLabel.setMinWidth(22); bitLabel.setAlignment(Pos.CENTER);
                bitLabel.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
                bitLabels[group][bit] = bitLabel;
                bitLabelsRow.getChildren().add(bitLabel);
            }
            groupContainer.getChildren().addAll(ledRow, bitLabelsRow);
            indicatorRow.getChildren().add(groupContainer);
        }
    }

    private void createBitTetrads() {
        bitRow.getChildren().clear();
        bitRow.setSpacing(8);

        bitRow.getChildren().add(createTetrad("Тетрада 7", createBitBlock(4, 0, "Адрес перехода")));
        bitRow.getChildren().add(createTetrad("Тетрада 6", createBitBlock(4, 4, "Тип перехода")));
        bitRow.getChildren().add(createTetrad("Тетрада 5", createBitBlock(1, 8, "MS2"), createBitBlock(3, 9, "ПРИОП")));
        bitRow.getChildren().add(createTetrad("Тетрада 4", createBitBlock(1, 12, "MS1"), createBitBlock(3, 13, "ИСТОП")));
        bitRow.getChildren().add(createTetrad("Тетрада 3", createBitBlock(1, 16, "C0"), createBitBlock(3, 17, "АЛУ")));
        bitRow.getChildren().add(createTetrad("Тетрада 2", createBitBlock(4, 20, "А")));
        bitRow.getChildren().add(createTetrad("Тетрада 1", createBitBlock(4, 24, "В")));
        bitRow.getChildren().add(createTetrad("Тетрада 0", createBitBlock(4, 28, "D")));
    }

    private VBox createBitBlock(int size, int startIndex, String title) {
        VBox block = new VBox(4);
        block.setAlignment(Pos.CENTER); block.setPadding(new Insets(4));
        Label label = new Label(title);
        HBox row = new HBox(2);
        row.setAlignment(Pos.CENTER);

        for (int i = 0; i < size; i++) {
            int idx = startIndex + i;
            Button btn = new Button("0");
            btn.setPrefSize(24, 24); btn.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");
            btn.setOnAction(e -> {
                bits[idx] ^= 1;
                btn.setText(String.valueOf(bits[idx]));
                refreshAllIndicators();
            });
            row.getChildren().add(btn);
        }
        block.getChildren().addAll(label, row);
        return block;
    }

    private VBox createTetrad(String title, VBox... groups) {
        VBox wrapper = new VBox(4);
        wrapper.setPadding(new Insets(6)); wrapper.setAlignment(Pos.CENTER);
        Label label = new Label(title);
        HBox row = new HBox(4); row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(groups);
        wrapper.getChildren().addAll(label, row);
        return wrapper;
    }

    private void createMicroAddressButtons() {
        microAddressRow.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            int idx = i;
            Button btn = new Button("0"); btn.setPrefSize(30, 30);
            btn.setOnAction(e -> {
                microAddress[idx] ^= 1;
                btn.setText(String.valueOf(microAddress[idx]));
                updateMicroHex();
            });
            microAddressRow.getChildren().add(btn);
        }
    }

    private void createMuxButtons() {
        muxRow.getChildren().clear();
        String[] muxNames = {"SA1 (1)", "SA2 (2)", "SA3 (4)"};
        for (int i = 0; i < 3; i++) {
            int idx = i;
            Button btn = new Button("0");
            btn.setPrefSize(70, 30); btn.setStyle("-fx-font-size: 11px;");
            btn.setText(muxNames[idx] + ": 0");
            btn.setOnAction(e -> {
                muxBits[idx] ^= 1;
                btn.setText(muxNames[idx] + ": " + muxBits[idx]);
                refreshAllIndicators();
            });
            muxRow.getChildren().add(btn);
        }
    }
}