package alu;

import register.Register;


public class ALU {
    private Register out = new Register();
    private int c0;
    private int c4;
    private int ovr;
    private int z;
    private int f3;
    private int code;
    private Register r;
    private Register s;

    public Register getOut() {
        return out;
    }

    public void setOut(Register out) {
        this.out = out;
    }

    public int getC0() {
        return c0;
    }

    public void setC0(int c0) {
        this.c0 = c0;
    }

    public int getC4() {
        return c4;
    }

    public void setC4(int c4) {
        this.c4 = c4;
    }

    public int getOvr() {
        return ovr;
    }

    public void setOvr(int ovr) {
        this.ovr = ovr;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public int getF3() {
        return f3;
    }

    public void setF3(int f3) {
        this.f3 = f3;
    }

    public Register getR() {
        return r;
    }

    public void setR(Register r) {
        this.r = r;
    }

    public Register getS() {
        return s;
    }

    public void setS(Register s) {
        this.s = s;
    }

    public void setSource(int code, int c0, Register r, Register s) {
        this.code=code;
        this.c0=c0;
        this.r = r;
        this.s = s;
    }

    public Register operation() {
        out.clear();
        z = 1;
        f3 = 0;
        c4 = 0;
        ovr = 0;
        int[] r = this.r.getData();
        int[] s = this.s.getData();
        int[] out = this.out.getData();
        switch (code) {

            // 000  R + S + C0
            case 0:
                arithmetic(r, s, false, out);
                break;

            // 001  S - R - 1 + C0
            // S + ~R + C0
            case 1:
                arithmetic(s, r, true, out);
                break;

            // 010  R - S - 1 + C0
            // R + ~S + C0
            case 2:
                arithmetic(r, s, true, out);
                break;

            // 011  R OR S
            case 3:
                for (int i = 0; i < 4; i++) {
                    out[i] = (byte) (r[i] | s[i]);
                }
                break;

            // 100  R AND S
            case 4:
                for (int i = 0; i < 4; i++) {
                    out[i] = (byte) (r[i] & s[i]);
                }
                break;

            // 101  ~R AND S
            case 5:
                for (int i = 0; i < 4; i++) {
                    out[i] = (byte) ((r[i] ^ 1) & s[i]);
                }
                break;

            // 110  R XOR S
            case 6:
                for (int i = 0; i < 4; i++) {
                    out[i] = (byte) (r[i] ^ s[i]);
                }
                break;

            // 111  ~(R XOR S)
            case 7:
                for (int i = 0; i < 4; i++) {
                    out[i] = (byte) ((r[i] ^ s[i]) ^ 1);
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown microcode: " + code
                );
        }

        for (int i = 0; i < 4; i++) {
            if (out[i] == 1) {
                z = 0;
                break;
            }
        }

        f3 = out[0];
        System.out.println("РЕЗУЛТАТ АЛУ: "+ this.out.toString());
        return this.out;

    }


    private void arithmetic(int[] a, int[] b, boolean invertB, int[] out) {
        int carry = c0;
        int c3 = 0;
        for (int i = 3; i >= 0; i--) {
            int ai = a[i];
            int bi = b[i];
            if (invertB) {
                bi ^= 1;
            }
            int sum = ai + bi + carry;
            out[i] = (byte) (sum & 1);
            carry = (sum >> 1) & 1;
            if (i == 1) {
                c3 = carry;
            }
        }
        c4 = carry;
        ovr = c4 ^ c3;
    }

    @Override
    public String toString() {
        return "ALU{" +
                "OUT=" + out +
                ", C0=" + c0 +
                ", C4=" + c4 +
                ", OVR=" + ovr +
                ", Z=" + z +
                ", F3=" + f3 +
                ", R=" + r +
                ", S=" + s +
                '}';
    }
}
