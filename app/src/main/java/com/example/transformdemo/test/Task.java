package com.example.transformdemo.test;

/**
 * 测试移除类成员
 */
public class Task {

    private int isTask = 0;

    private long tell = 0;

    public void isTask(boolean test){
        System.out.println("call isTask");
    }
    public void tellMe(){
        System.out.println("call tellMe");
    }

    class TaskInner{
        int inner;
    }

}
