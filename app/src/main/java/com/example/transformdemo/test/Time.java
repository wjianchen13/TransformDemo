package com.example.transformdemo.test;

public class Time {
    public void myCount() throws Exception {
        int i = 5;
        int j = 10;
        System.out.println(j - i);
    }

    public void myDeal() {
        try {
            int[] myInt = {1, 2, 3, 4, 5};
            int f = myInt[10];
            System.out.println(f);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
