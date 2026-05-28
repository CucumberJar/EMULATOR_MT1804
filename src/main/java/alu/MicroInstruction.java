package alu;

import register.Register;

public class MicroInstruction {
    private  int[] instruction = new int[32];


    public int[] getInstruction() {
        return instruction;
    }
public  void clear(){
    for (int i = 0; i < 32; i++) {
    instruction[i]=0;
    }
}
    public void setInstruction(int[] instruction) {
        this.instruction = instruction;
    }

    // 0-3
    private Register d = new Register();

    // 4-7
    private int bCode;

    // 8-11
    private int aCode;

    // 12-14
    private int aluCode;

    // 15
    private int c0;

    // 16-18
    private int sourceCode;

    // 19
    private int ms1;

    // 20-22
    private int destinationCode;

    // 23
    private int ms2;

    // 24-27
    private int controlCode;

    // 28-31
    private int address;


    public void decode() {
        d.setData(instruction[31],instruction[30],instruction[29],instruction[28]);
        bCode = bitsToInt(27, 24);
        aCode = bitsToInt(23, 20);
        aluCode = bitsToInt(19, 17);
        c0 = instruction[16];
        sourceCode = bitsToInt(15, 13);
        ms1 = instruction[12];
        destinationCode = bitsToInt(11, 9);
        ms2 = instruction[8];
        controlCode = bitsToInt(7, 4);
        address = bitsToInt(3, 0);
    }

    private int bitsToInt(int from, int to) {
        int value = 0;
        for (int i = to; i <= from; i++
        ) {
            value <<= 1;
            value |= instruction[i];
        }
        return value;
    }


    public Register getD() {
        return d;
    }

    public int getBCode() {
        return bCode;
    }

    public int getACode() {
        return aCode;
    }

    public int getAluCode() {
        return aluCode;
    }

    public int getC0() {
        return c0;
    }

    public int getSourceCode() {
        return sourceCode;
    }

    public int getDestinationCode() {
        return destinationCode;
    }

    public int getMs1() {
        return ms1;
    }

    public int getMs2() {
        return ms2;
    }

    public int getControlCode() {
        return controlCode;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String toString() {

        return "MicroInstruction{" +
                "d=" + d +
                ", bCode=" + bCode +
                ", aCode=" + aCode +
                ", aluCode=" + aluCode +
                ", c0=" + c0 +
                ", sourceCode=" + sourceCode +
                ", destinationCode=" + destinationCode +
                ", ms1=" + ms1 +
                ", ms2=" + ms2 +
                ", controlCode=" + controlCode +
                ", address=" + address +
                '}';
    }
}