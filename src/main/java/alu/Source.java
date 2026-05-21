package alu;

import register.Register;

public class Source {
    private final Register zero = new Register();
    private Register r;
    private Register s;

    public Register getR() {
        return r;
    }

    public Register getS() {
        return s;
    }

    public void select(int code, Register a, Register b, Register d, Register q) {
        zero.clear();
        switch (code) {
            // 000 -> R=A , S=B
            case 0:
                r = a;
                s = q;
                break;

            // 001 -> R=A , S=B
            case 1:
                r = a;
                s = b;
                break;

            // 010 -> R=0 , S=Q
            case 2:
                r = zero;
                s = q;
                break;

            // 011 -> R=0 , S=B
            case 3:
                r = zero;
                s = b;
                break;

            // 100 -> R=0 , S=A
            case 4:
                r = zero;
                s = a;
                break;

            // 101 -> R=D , S=A
            case 5:
                r = d;
                s = a;
                break;

            // 110 -> R=D , S=Q
            case 6:
                r = d;
                s = q;
                break;

            // 111 -> R=D , S=0
            case 7:
                r = d;
                s = zero;
                break;

            default:
                throw new IllegalArgumentException("Unknown source code: " + code);
        }
    }


}