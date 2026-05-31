package alu;

import register.Register;

public class Source {
    private int r = 0;
    private int s = 0;

    public void select(int sourceCode, Register a, Register b, Register d, Register q) {
        sourceCode = sourceCode & 0x7; // 3-битный код (0-7)

        switch (sourceCode) {
            case 0: // А, Q
                this.r = a.getValue();
                this.s = q.getValue();
                break;
            case 1: // А, B
                this.r = a.getValue();
                this.s = b.getValue();
                break;
            case 2: // 0, Q
                this.r = 0;
                this.s = q.getValue();
                break;
            case 3: // 0, B
                this.r = 0;
                this.s = b.getValue();
                break;
            case 4: // 0, A
                this.r = 0;
                this.s = a.getValue();
                break;
            case 5: // D, A
                this.r = d.getValue();
                this.s = a.getValue();
                break;
            case 6: // D, Q
                this.r = d.getValue();
                this.s = q.getValue();
                break;
            case 7: // D, 0
                this.r = d.getValue();
                this.s = 0;
                break;
        }
    }

    public int getR() { return r & 0xF; }
    public int getS() { return s & 0xF; }

    @Override
    public String toString() {
        return "Source{R=" + r + ", S=" + s + "}";
    }
}