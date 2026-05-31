package register;

public class Register {
    private int value = 0; // 4-битное значение (0-15)

    public int getValue() {
        return value & 0xF; // Гарантируем 4 бита
    }

    public void setValue(int value) {
        this.value = value & 0xF;
    }

    public void clear() {
        this.value = 0;
    }

    @Override
    public String toString() {

        return String.format("%4s", Integer.toBinaryString(value & 0xF)).replace(' ', '0');
    }
}