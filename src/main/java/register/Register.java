package register;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class Register {
    private int address;
    private int value = 0;
    private int[] data = new int[]{0,0,0,0};

    public Register(int address) {
        this.address = address;
    }

    public void write() {
        value = 0;
        for (int i = 0; i < 4; i++) {
            value += data[i] * (1 << i);
        }
    }

    public void setData(int bit0, int bit1, int bit2, int bit3) {
        data[0] = bit0;
        data[1] = bit1;
        data[2] = bit2;
        data[3] = bit3;

    }

    public void clear() {
        for (int i = 0; i < 4; i++) {
            data[i] = 0;
        }
    }

    public int compare(Register register) {
        return Integer.compare(value, register.value);

    }

    @Override
    public String toString() {
        return "Register{" +
                "address=" + address +
                ", value=" + value +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
