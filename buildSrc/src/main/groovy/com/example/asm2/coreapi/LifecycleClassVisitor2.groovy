package com.example.asm2.coreapi

import com.example.asm1.LifecycleOnCreateMethodVisitor
import com.example.asm1.LifecycleOnDestroyMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * 在onCreate和onDestroy打印输出
 * Log.i("TAG", "-------> onCreate : " + this.getClass().getSimpleName());
 * 可以查看ASM修改之后的MainActivity.class
 *  xx\app\build\intermediates\transforms\AsmTestTransform\debug\27\com\example\transformdemo\MainActivity.class
 */
public class LifecycleClassVisitor2 extends ClassVisitor implements Opcodes {

    private String mClassName;

    public LifecycleClassVisitor2(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        //System.out.println("LifecycleClassVisitor : visit -----> started ：" + name);
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        //System.out.println("LifecycleClassVisitor : visitMethod : " + name);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //匹配FragmentActivity
        println "===================> mClassName: " + mClassName
        if ("com/example/transformdemo/MainActivity".equals(this.mClassName)) {
            if ("onCreate".equals(name) ) {
                //处理onCreate
                System.out.println("LifecycleClassVisitor : change method ----> " + name);
                return new LifecycleOnCreateMethodVisitor(mv);
            } else if ("onDestroy".equals(name)) {
                //处理onDestroy
                System.out.println("LifecycleClassVisitor : change method ----> " + name);
                return new LifecycleOnDestroyMethodVisitor(mv);
            }
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        //System.out.println("LifecycleClassVisitor : visit -----> end");
        super.visitEnd();
    }
}
