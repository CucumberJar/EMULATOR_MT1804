package register;

import java.util.Arrays;

public class Register {


    private int[] data = new int[]{0,0,0,0};

    public Register() {
    }

    public int[] getData() {
        return data;
    }

    public void setData(int b0, int b1, int b2, int b3) {
        data[0] = b0;
        data[1] = b1;
        data[2] = b2;
        data[3] = b3;
    }

    public void clear() {
        Arrays.fill(data, 0);
    }

    public void write(Register source) {
        for (int i = 0; i < 4 ; i++) {
            data[i]=source.getData()[i];
        }
    }

}