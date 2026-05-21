package app;

import alu.ALU;
import alu.MicroInstruction;
import register.Register;

public class CPU {
    private MicroInstruction microInstruction;
    private final Register[] registers = new Register[16];
    private final ALU alu = new ALU();

    public ALU getAlu() {
        return alu;
    }

    public CPU() {
        for (int i = 0; i < registers.length; i++) {
            //registers[i] = new Register(i);
        }
        registers[0].setData(0,1,1,0);
        registers[1].setData(1,0,1,0);
        alu.setSource(microInstruction.getAluCode(), microInstruction.getC0(),registers[0],registers[1]);

    }

    public void setRegisters(int microcode){




    }

    public Register step(int microcode) {

        Register result = alu.operation();

        registers[2].setData(
                result.getData()[0],
                result.getData()[1],
                result.getData()[2],
                result.getData()[3]
        );

        return result;
    }

    public Register getRegister(int i) {
        return registers[i];
    }
}