package app;

import alu.ALU;
import alu.Destination;
import alu.MicroInstruction;
import alu.Source;
import register.Register;

import java.util.Arrays;

public class CPU {
    private MicroInstruction[] microInstruction = new MicroInstruction[16];
    private final Register[] registers = new Register[16];
    private Source source = new Source();
    private Destination destination = new Destination();
    private final Register q = new Register();
    private final Register f = new Register();
    private int[] flags = new int[4];

    public MicroInstruction[] getMicroInstruction() {
        return microInstruction;
    }

    public void setMicroInstruction(MicroInstruction[] microInstruction) {
        this.microInstruction = microInstruction;
    }

    public void load(int address, int[] microInstruction) {
        for (int i = 0; i < 32; i++) {
            this.microInstruction[address].setInstruction(microInstruction);
        }
    }

    private final ALU alu = new ALU();

    public ALU getAlu() {
        return alu;
    }

    public void start() {
        q.clear();
        f.clear();
        for (int i = 0; i < 16; i++) {
            registers[i] = new Register();
            microInstruction[i] = new MicroInstruction();
        }
    }

    public void tact() {
        Register b= registers[microInstruction[0].getBCode()];
        System.out.println("B регистр: "+microInstruction[0].getBCode()+" -> "+ b.toString());
        Register d =microInstruction[0].getD();
        Register a =registers[microInstruction[0].getACode()];
        alu.setC0(microInstruction[0].getC0());
        source.select(microInstruction[0].getSourceCode(), a, b , d, q);
        alu.setSource(microInstruction[0].getAluCode(), microInstruction[0].getC0(), source.getR(), source.getS());
        alu.operation();
        destination.write(microInstruction[0].getDestinationCode(),q,b,alu.getOut(),a,microInstruction[0].getMs2(),microInstruction[0].getMs1());
        System.out.println(this);
        flags[0] = alu.getC4();
        flags[1] = alu.getF3();
        flags[2] = alu.getZ();
        flags[3] = alu.getOvr();
    }

    public Register[] getRegisters() {
        return registers;
    }

    public int[] getFlags() {
        return flags;
    }

    public void setFlags(int[] flags) {
        this.flags = flags;
    }

    public Register getQ() {
        return q;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Register getF() {
        return f;
    }

    public Register getRegister(int i) {
        return registers[i];
    }

    @Override
    public String toString() {
        return "CPU{" +
                "microInstruction=" + microInstruction[0] +
                ", registers=" + Arrays.toString(registers) +
                ", source=" + source +
                ", destination=" + destination +
                ", q=" + q +
                ", f=" + f +
                ", flags=" + Arrays.toString(flags) +
                ", alu=" + alu +
                '}';
    }
}