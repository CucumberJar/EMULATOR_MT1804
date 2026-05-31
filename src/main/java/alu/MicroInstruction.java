package alu;

import register.Register;

public class MicroInstruction {
    private final Register d = new Register(); // Поле ввода констант / Direct Data

    // Поля БИС К1804ВС1 (АЛУ)
    private int aCode;           // Адрес регистра А (4 бита)
    private int bCode;           // Адрес регистра B (4 бита)
    private int sourceCode;      // Код источников Source (3 бита)
    private int aluCode;         // Код операции АЛУ (3 бита)
    private int destinationCode; // Код назначения Destination (3 бита)
    private int c0;              // Перенос C0 (1 бит)
    private int ms1, ms2;        // Управление сдвигами (по 1 биту)

    // Поля БМУ К1804ВУ4 (Управление переходами из таблицы)
    private int pCode;           // Тип ветвления P3-P0 (4 бита)
    private int brAddress;       // Адрес ветвления BR (4 бита)

    public void setInstruction(int[] bits) {
        if (bits == null || bits.length < 32) return;

        // Тетрада 2: Адрес A (биты 8..11)
        this.aCode           = parseBits(bits, 8, 4);

        // Тетрада 1: Адрес B (биты 4..7)
        this.bCode           = parseBits(bits, 4, 4);

        // Тетрада 4: Источник ИСТОП (биты 16..18)
        this.sourceCode      = parseBits(bits, 16, 3);

        // Тетрада 3: Код операции АЛУ (биты 12..14)
        this.aluCode         = parseBits(bits, 12, 3);

        // Тетрада 5: Приемник ПРИОП (биты 20..22)
        this.destinationCode = parseBits(bits, 20, 3);

        // Одиночные управляющие биты из Тетрад 3, 4, 5
        this.c0              = bits[15]; // Бит 15 (Тетрада 3)
        this.ms1             = bits[19]; // Бит 19 (Тетрада 4)
        this.ms2             = bits[23]; // Бит 23 (Тетрада 5)

        // Тетрада 6: Тип перехода P3-P0 БМУ К1804ВУ4 (биты 24..27)
        this.pCode           = parseBits(bits, 24, 4);

        // Тетрада 7: Адрес ветвления BR БМУ К1804ВУ4 (биты 28..31)
        this.brAddress       = parseBits(bits, 28, 4);

        // Тетрада 0: Входные данные D (биты 0..3)
        this.d.setValue(parseBits(bits, 0, 4));
    }

    private int parseBits(int[] bits, int start, int length) {
        int val = 0;
        for (int i = 0; i < length; i++) {
            val = (val << 1) | (bits[start + i] & 1);
        }
        return val;
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
        return "Microcode{A=" + aCode + ", B=" + bCode + ", ALU=" + aluCode + ", P_CODE=" + pCode + ", BR=" + brAddress + "}";
    }
}