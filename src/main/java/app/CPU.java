package app;

import alu.ALU;
import alu.Destination;
import alu.MicroInstruction;
import alu.Source;
import register.Register;

import java.util.Arrays;

public class CPU {
    // Память микрокоманд (16 ячеек)
    private MicroInstruction[] microInstruction = new MicroInstruction[16];
    // Регистровый файл БИС (16 рабочих регистров)
    private final Register[] registers = new Register[16];

    private Source source = new Source();
    private Destination destination = new Destination();
    private final Register q = new Register();
    private final Register f = new Register();

    // Массив флагов: [0]=C4, [1]=F3, [2]=Z, [3]=OVR
    private int[] flags = new int[4];

    // --- Новые элементы для микропрограммного управления ---
    private int mpc = 0;                     // Счётчик микрокоманд (Microprogram Counter)
    private final int[] stack = new int[16]; // Стек БМУ на 16 уровней
    private int stackPointer = -1;           // Указатель стека (-1 — пуст)

    public MicroInstruction[] getMicroInstruction() {
        return microInstruction;
    }

    public void setMicroInstruction(MicroInstruction[] microInstruction) {
        this.microInstruction = microInstruction;
    }

    // Метод загрузки массива бит в конкретный адрес памяти микрокоманд
    public void load(int address, int[] microInstructionBits) {
        if (address >= 0 && address < 16) {
            this.microInstruction[address].setInstruction(microInstructionBits);
        }
    }

    private final ALU alu = new ALU();

    public ALU getAlu() {
        return alu;
    }

    // Инициализация процессора при старте
    public void start() {
        this.mpc = 0;
        this.stackPointer = -1;
        Arrays.fill(stack, 0);
        Arrays.fill(flags, 0);

        q.clear();
        f.clear();
        for (int i = 0; i < 16; i++) {
            registers[i] = new Register();
            microInstruction[i] = new MicroInstruction();
        }
    }

    // Один такт работы процессора
    public void tact() {
        // 1. Извлекаем текущую микрокоманду по адресу из счётчика MPC
        MicroInstruction current = microInstruction[this.mpc];
        System.out.println("--- ТАКТ ЦП --- Текущий адрес MPC: " + this.mpc);

        // 2. Выборка операндов из регистров
        Register b = registers[current.getBCode()];
        System.out.println("B регистр: индекс " + current.getBCode() + " -> " + b.toString());
        Register a = registers[current.getACode()];
        Register d = current.getD(); // Входные внешние данные (Директ)

        // 3. Настройка источников и выполнение операции в АЛУ
        alu.setC0(current.getC0());
        source.select(current.getSourceCode(), a, b, d, q);

        alu.setSource(current.getAluCode(), current.getC0(), source.getR(), source.getS());
        alu.operation();

        // 4. Запись результата (Destination)
        destination.write(current.getDestinationCode(), q, b, alu.getOut(), a, current.getMs2(), current.getMs1());

        // 5. Обновление флагов состояния АЛУ К1804ВС1
        flags[0] = alu.getC4();
        flags[1] = alu.getF3();
        flags[2] = alu.getZ();
        flags[3] = alu.getOvr();

        // 6. Вычисление адреса следующей микрокоманды (Логика БМУ К1804ВУ4)
        int nextMpc = calculateNextAddress(current);

        // Отображение состояния процессора в консоль
        System.out.println(this);
        System.out.println("Адрес следующей микрокоманды: " + nextMpc);
        System.out.println("----------------------------------------");

        // Переход на новый адрес
        this.mpc = nextMpc;
    }

    // Реализация логики ветвлений (Таблица 4.1)
    private int calculateNextAddress(MicroInstruction currentInstr) {
        int pCode = currentInstr.getPCode();       // Код перехода P3-P0 (0..15)
        int brAddress = currentInstr.getBRAddress(); // Адрес перехода BR из микрокоманды (0..15)

        boolean c4 = (flags[0] == 1);
        boolean f3 = (flags[1] == 1);
        boolean z  = (flags[2] == 1);
        boolean ovr = (flags[3] == 1);

        boolean fNotZero = !z; // Если результат АЛУ не равен нулю, флаг Z равен 0

        switch (pCode) {
            case 0b0000: // 0000: Переход на BR при F != 0
                return fNotZero ? brAddress : ((mpc + 1) % 16);

            case 0b0001: // 0001: Безусловный переход на BR
                return brAddress;

            case 0b0010: // 0010: Продолжить (переход на следующий адрес)
                return (mpc + 1) % 16;

            case 0b0011: // 0011: Переход по клавишам "АДРЕС"
                return readHardwareAddressBus();

            case 0b0100: // 0100: Переход к подпрограмме при F != 0
                if (fNotZero) {
                    push((mpc + 1) % 16);
                    return brAddress;
                }
                return (mpc + 1) % 16;

            case 0b0101: // 0101: Переход к подпрограмме (безусловный)
                push((mpc + 1) % 16);
                return brAddress;

            case 0b0110: // 0110: Возврат из подпрограммы
                return pop();

            case 0b0111: // 0111: Переход по стеку
                return (stackPointer >= 0) ? stack[stackPointer] : 0;

            case 0b1000: // 1000: Окончить цикл и вытолкнуть из стека при F == 0
                if (z) { // F == 0 означает Z == 1
                    pop();
                    return (mpc + 1) % 16;
                }
                return (stackPointer >= 0) ? stack[stackPointer] : 0;

            case 0b1001: // 1001: Загрузить стек и продолжить
                push((mpc + 1) % 16); // Теперь в стеке будет храниться начало тела цикла, а не команда инициализации
                return (mpc + 1) % 16;

            case 0b1010: // 1010: Вытолкнуть из стека и продолжить
                pop();
                return (mpc + 1) % 16;

            case 0b1011: // 1011: Окончить цикл и вытолкнуть из стека при C4 = 1
                if (c4) {
                    pop();
                    return (mpc + 1) % 16;
                }
                return (stackPointer >= 0) ? stack[stackPointer] : 0;

            case 0b1100: // 1100: Переход на BR при Z = 1
                return z ? brAddress : ((mpc + 1) % 16);

            case 0b1101: // 1101: Переход на BR при F3 = 1
                return f3 ? brAddress : ((mpc + 1) % 16);

            case 0b1110: // 1110: Переход на BR при OVR = 1
                return ovr ? brAddress : ((mpc + 1) % 16);

            case 0b1111: // 1111: Переход на BR при C4 = 1
                return c4 ? brAddress : ((mpc + 1) % 16);

            default:
                return (mpc + 1) % 16;
        }
    }

    // Хелперы работы со стеком
    private void push(int address) {
        if (stackPointer < 15) {
            stackPointer++;
            stack[stackPointer] = address;
        } else {
            System.out.println("Критическая ошибка: Переполнение стека микропрограмм!");
        }
    }

    private int pop() {
        if (stackPointer >= 0) {
            int val = stack[stackPointer];
            stackPointer--;
            return val;
        }
        System.out.println("Критическая ошибка: Стек микропрограмм пуст!");
        return 0;
    }

    // Имитация чтения адреса с физической панели тренажёра
    private int readHardwareAddressBus() {
        return 0;
    }

    // Геттеры и сеттеры управляющей логики
    public int getMpc() { return mpc; }
    public void setMpc(int mpc) { this.mpc = mpc; }
    public int[] getFlags() { return flags; }
    public void setFlags(int[] flags) { this.flags = flags; }
    public Register getQ() { return q; }
    public Register getF() { return f; }
    public Source getSource() { return source; }
    public void setSource(Source source) { this.source = source; }
    public Destination getDestination() { return destination; }
    public void setDestination(Destination destination) { this.destination = destination; }
    public Register getRegister(int i) { return registers[i]; }
    public Register[] getRegisters() { return registers; }

    @Override
    public String toString() {
        return "CPU Стан: {" +
                "MPC=" + mpc +
                ", Стек SP=" + stackPointer + ", Стек=" + Arrays.toString(Arrays.copyOfRange(stack, 0, Math.max(0, stackPointer+1))) +
                ", Флаги [C4, F3, Z, OVR]=" + Arrays.toString(flags) +
                ", Регистр Q=" + q +
                ", ALU Out=" + alu.getOut() +
                '}';
    }
}