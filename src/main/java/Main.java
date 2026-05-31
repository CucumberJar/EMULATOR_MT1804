import app.CPU;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        CPU cpu = new CPU();
        cpu.start();

        System.out.println("=== АВТОГЕНЕРАЦИЯ ВЫЧИСЛИТЕЛЬНОГО МИКРОКОДА ===");

        for (int regIndex = 0; regIndex < 16; regIndex++) {
            int[] instr = new int[32];

            if (regIndex == 0) {
                // КОМАНДА 0: Обнуление R0. Нам нужно получить 0 на выходе АЛУ.
                writeValToBits(instr, 0, 4, 0);  // ACode = 0
                writeValToBits(instr, 4, 4, 0);  // BCode = 0 (пишем в R0)
                writeValToBits(instr, 8, 3, 4);  // Source = 4 (Выбор 0 и A -> R=0, S=R0)
                writeValToBits(instr, 11, 3, 4); // ALU = 4 (Поразрядное И: R & S -> 0 & R0 = 0)
                writeValToBits(instr, 14, 3, 3); // Destination = 3 (Запись в RAM[B])
                instr[17] = 0;                   // C0 = 0
            } else {
                // КОМАНДЫ 1-15: Аппаратный инкремент значения предыдущего регистра
                writeValToBits(instr, 0, 4, regIndex - 1); // ACode = индекс предыдущего регистра
                writeValToBits(instr, 4, 4, regIndex);     // BCode = индекс текущего регистра
                writeValToBits(instr, 8, 3, 4);             // Source = 4 (Выбор 0 и A -> R=0, S=R_prev)
                writeValToBits(instr, 11, 3, 0);            // ALU = 0 (Сложение: R + S + C0)
                writeValToBits(instr, 14, 3, 3);            // Destination = 3 (Запись в RAM[B])
                instr[17] = 1;                              // АППАРАТНЫЙ ПЕРЕНОС C0 = 1 (результат: 0 + R_prev + 1)
            }

            // Системная часть (биты сдвигов, переходы БМУ и поле данных)
            instr[18] = 0; // ms1 = 0
            instr[19] = 0; // ms2 = 0
            writeValToBits(instr, 20, 4, 2); // PCode = 2 (Продолжить выполнение)
            writeValToBits(instr, 24, 4, 0); // BRAddress = 0
            writeValToBits(instr, 28, 4, 0); // Поле D = 0000 (НЕ ИСПОЛЬЗУЕТСЯ!)

            cpu.load(regIndex, instr);
        }

        System.out.println("\n=== ВЫПОЛНЕНИЕ ВЫЧИСЛЕНИЙ НА ЦП (16 ТАКТОВ) ===");
        for (int t = 0; t < 16; t++) {
            cpu.tact();
        }

        System.out.println("\n=== РЕЗУЛЬТАТ ЧЕСТНОГО РАСЧЕТА РОН ===");
        System.out.println(Arrays.toString(cpu.getRegisters()));
    }

    private static void writeValToBits(int[] bits, int start, int length, int value) {
        for (int i = length - 1; i >= 0; i--) {
            bits[start + i] = value & 1;
            value >>= 1;
        }
    }
}