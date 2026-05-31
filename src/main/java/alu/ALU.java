package alu;

public class ALU {
    private int aluCode;
    private int c0; // Входной перенос
    private int r;
    private int s;
    private int out; // Выход АЛУ

    // Флаги
    private int c4, f3, z, ovr;

    public void setSource(int aluCode, int c0, int r, int s) {
        this.aluCode = aluCode & 0x7;
        this.c0 = c0 & 0x1;
        this.r = r & 0xF;
        this.s = s & 0xF;
    }

    public void operation() {
        int result = 0;
        int tempOvr = 0;

        switch (aluCode) {
            case 0: // R + S + C0 (Сложение)
                result = r + s + c0;
                // Переполнение знака для 4 бит
                int rSign = (r >> 3) & 1;
                int sSign = (s >> 3) & 1;
                int outSign = ((result & 0xF) >> 3) & 1;
                if (rSign == sSign && outSign != rSign) tempOvr = 1;
                break;
            case 1: // S - R - 1 + C0 (Вычитание)
                result = s + (~r & 0xF) + c0;
                break;
            case 2: // R - S - 1 + C0 (Вычитание)
                result = r + (~s & 0xF) + c0;
                break;
            case 3: // R | S (Поразрядное ИЛИ)
                result = r | s;
                break;
            case 4: // R & S (Поразрядное И)
                result = r & s;
                break;
            case 5: // ~R & S (Маскирование)
                result = (~r & 0xF) & s;
                break;
            case 6: // R ^ S (Исключающее ИЛИ)
                result = r ^ s;
                break;
            case 7: // ~(R ^ S) (Инверсия XOR)
                result = ~(r ^ s) & 0xF;
                break;
        }

        this.out = result & 0xF;

        // Расчет флагов
        this.c4 = (result > 15 || result < 0) ? 1 : 0;
        this.f3 = (this.out >> 3) & 1;
        this.z  = (this.out == 0) ? 1 : 0;
        this.ovr = tempOvr;
    }

    public int getOut() { return out; }
    public int getC4() { return c4; }
    public int getF3() { return f3; }
    public int getZ() { return z; }
    public int getOvr() { return ovr; }
    public void setC0(int c0) { this.c0 = c0; }
}