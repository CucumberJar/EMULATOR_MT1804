package alu;

import register.Register;
import java.util.Arrays;

public class MicroInstruction {
    private final Register d = new Register(); // Поле ввода констант / Direct Data

    // Хранилище для 32 битов команды в инвертированном (визуальном) порядке:
    // Индексы 0..3   -> Тетрада 7 (Адрес перехода)
    // Индексы 28..31 -> Тетрада 0 (Данные D)
    private final int[] bits = new int[32];

    // Поля БИС К1804ВС1 (АЛУ)
    private int aCode;           // Адрес регистра А (4 бита)
    private int bCode;           // Адрес регистра B (4 бита)
    private int sourceCode;      // Код источников Source (3 бита)
    private int aluCode;         // Код операции АЛУ (3 бита)
    private int destinationCode; // Код назначения Destination (3 бита)
    private int c0;              // Перенос C0 (1 бит)
    private int ms1, ms2;        // Управление сдвигами (по 1 биту)

    // Поля БМУ К1804ВУ4 (Управление переходами)
    private int pCode;           // Тип ветвления P3-P0 (4 бита)
    private int brAddress;       // Адрес ветвления BR (4 бита)

    public void setInstruction(int[] incomingBits) {
        if (incomingBits == null || incomingBits.length < 32) return;
        System.out.println("Входящие биты: " + Arrays.toString(incomingBits));
        System.arraycopy(incomingBits, 0, bits, 0, 32);




        // Тетрада 7: Адрес ветвления BR БМУ К1804ВУ4 (биты 0..3)
        this.brAddress       = parseBits(bits, 0, 4);

        // Тетрада 6: Тип перехода P3-P0 БМУ К1804ВУ4 (биты 4..7)
        this.pCode           = parseBits(bits, 4, 4);

        // Тетрада 5: Сдвиг MS2 (бит 8) + Приемник ПРИОП (биты 9..11)
        this.ms2             = bits[8];
        this.destinationCode = parseBits(bits, 9, 3);

        // Тетрада 4: Сдвиг MS1 (бит 12) + Источник ИСТОП (биты 13..15)
        this.ms1             = bits[12];
        this.sourceCode      = parseBits(bits, 13, 3);

        // Тетрада 3: Перенос C0 (бит 16) + Код операции АЛУ (биты 17..19)
        this.c0              = bits[16];
        this.aluCode         = parseBits(bits, 17, 3);

        // Тетрада 2: Адрес A (биты 20..23)
        this.aCode           = parseBits(bits, 20, 4);

        // Тетрада 1: Адрес B (биты 24..27)
        this.bCode           = parseBits(bits, 24, 4);

        // Тетрада 0: Входные данные D (биты 28..31)
        this.d.setValue(parseBits(bits, 28, 4));
        System.out.println(this);
    }

    private int parseBits(int[] bits, int start, int length) {
        int val = 0;
        for (int i = 0; i < length; i++) {
            val = (val << 1) | (bits[start + i] & 1);
        }
        return val;
    }

    public void clear() {
        Arrays.fill(this.bits, 0);
        this.aCode = 0;
        this.bCode = 0;
        this.sourceCode = 0;
        this.aluCode = 0;
        this.destinationCode = 0;
        this.c0 = 0;
        this.ms1 = 0;
        this.ms2 = 0;
        this.pCode = 0;
        this.brAddress = 0;
        this.d.clear();
    }

    public int[] getInstructionBits() {
        return this.bits;
    }

    // Геттеры
    public int getACode() { return aCode; }
    public int getBCode() { return bCode; }
    public int getSourceCode() { return sourceCode; }
    public int getAluCode() { return aluCode; }
    public int getDestinationCode() { return destinationCode; }
    public int getC0() { return c0; }
    public int getMs1() { return ms1; }
    public int getMs2() { return ms2; }
    public int getPCode() { return pCode; }
    public int getBRAddress() { return brAddress; }
    public Register getD() { return d; }

    @Override
    public String toString() {
        return "MicroInstruction{" +
                "d=" + d +
                ", bits=" + Arrays.toString(bits) +
                ", aCode=" + aCode +
                ", bCode=" + bCode +
                ", sourceCode=" + sourceCode +
                ", aluCode=" + aluCode +
                ", destinationCode=" + destinationCode +
                ", c0=" + c0 +
                ", ms1=" + ms1 +
                ", ms2=" + ms2 +
                ", pCode=" + pCode +
                ", brAddress=" + brAddress +
                '}';
    }
}