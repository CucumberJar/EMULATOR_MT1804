package alu;

import register.Register;

public class Destination {
    public void write(int code, Register q, Register b, Register f, Register a, int ms2, int ms1) {

        Register register = new Register();
        switch (code) {
            case 0:
                q.write(f);
                break;

            case 1:
                break;


            case 2:
                b.write(f);
                f.write(a);
                break;

            case 3:
                b.write(f);
                break;

            case 4:
                shift(true,q,f,ms2,ms1);
                b.write(f);
                break;

            case 5:
                register.write(q);
                shift(true,register,f,ms2,ms1);
                b.write(f);
                break;

            case 6:
                shift(false,q,f,ms2,ms1);
                b.write(f);
                break;

            case 7:
                register.write(q);
                shift(false,register,f,ms2,ms1);
                b.write(f);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unknown destination code: " + code
                );
        }
  }
    private void shift(boolean isRight, Register q, Register f, int ms2, int ms1) {
        int key = ms2 * 2 + ms1;
        int[] fData = f.getData();
        int[] qData = q.getData();
        int carry = 0;
        switch (key) {
            case 0:
                if (isRight) {
                    f.setData(0, fData[0], fData[1], fData[2]);
                    q.setData(0, qData[0], qData[1], qData[2]);
                } else {
                    f.setData(fData[1], fData[2], fData[3], 0);
                    q.setData(qData[1], qData[2], qData[3], 0);
                }
                break;

            case 1:
                if (isRight) {
                    carry = fData[3];
                    f.setData(carry, fData[0], fData[1], fData[2]);
                    carry = qData[3];
                    q.setData(carry, qData[0], qData[1], qData[2]);
                } else {
                    carry = fData[0];
                    f.setData(fData[1], fData[2], fData[3], carry);
                    carry = qData[0];
                    q.setData(qData[1], qData[2], qData[3], carry);
                }
                break;

            case 2:
                if (isRight) {
                    carry = fData[3];

                    f.setData(qData[3], fData[0], fData[1], fData[2]);
                    q.setData(carry, qData[0], qData[1], qData[2]);

                } else {
                    carry = fData[0];

                    f.setData(fData[1], fData[2], fData[3], qData[0]);
                    q.setData(qData[1], qData[2], qData[3], carry);
                }
                break;

            case 3:
                if (isRight) {
                    carry = fData[3];

                    f.setData(qData[3], fData[0], fData[1], fData[2]);
                    q.setData(carry, qData[0], qData[1], qData[2]);

                } else {
                    f.setData(fData[1], fData[2], fData[3], qData[0]);
                    q.setData(qData[1], qData[2], qData[3], 0);
                }
                break;
        }
    }


}