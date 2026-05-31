package alu;

import register.Register;

public class Destination {

    public void write(int destCode, Register q, Register b, int aluOut, Register a, int ms2, int ms1) {
        destCode = destCode & 0x7; // 3 бита (0-7)

        switch (destCode) {
            case 0: // Q <- F (Запись в рабочий регистр Q)
                q.setValue(aluOut);
                break;
            case 1: // NOP (Никуда не писать)
                break;
            case 2: // RAM[B] <- A, Y <- A
                b.setValue(a.getValue());
                break;
            case 3: // RAM[B] <- F
                b.setValue(aluOut);
                break;
            case 4: // Сдвиг RAM вправо, Q вправо
                int ramRight = (aluOut >> 1) | (ms2 << 3);
                b.setValue(ramRight);
                int qRight = (q.getValue() >> 1) | (ms1 << 3);
                q.setValue(qRight);
                break;
            case 5: // Сдвиг RAM вправо
                b.setValue((aluOut >> 1) | (ms2 << 3));
                break;
            case 6: // Сдвиг RAM влево, Q влево
                int ramLeft = (aluOut << 1) | ms2;
                b.setValue(ramLeft);
                int qLeft = (q.getValue() << 1) | ms1;
                q.setValue(qLeft);
                break;
            case 7: // Сдвиг RAM влево
                b.setValue((aluOut << 1) | ms2);
                break;
        }
    }
}