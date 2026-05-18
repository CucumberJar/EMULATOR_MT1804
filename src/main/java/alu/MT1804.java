package alu;

import register.Register;

public class MT1804 {
    private static Register[] registers = new Register[16];
    private static ALU alu;
    public Register getRegister(int i) {
        return registers[i];
    }
    public static void init(){
        for (int i = 0; i < 15; i++) {
            registers[i]=new Register(i);

        }
    }

    public static void clear(){
        for (int i = 0; i < 15; i++) {
            registers[i].clear();

        }
    }

    static void main() {
        init();
        clear();
        registers[0].setData(1,1,1,1);
        registers[1].setData(1,1,1,1);
        alu = new ALU();
        alu.setSource(registers[0], registers[1]);
        Register register =  alu.operation();
        System.out.println(register);
        System.out.println(alu);

    }

}
