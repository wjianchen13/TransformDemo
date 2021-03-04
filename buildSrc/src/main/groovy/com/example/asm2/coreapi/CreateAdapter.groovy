package com.example.asm2.coreapi

import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static org.objectweb.asm.Opcodes.*

/**
 * 刚开始创建文件没有成功，目标目录没有自己创建的文件，把buildSrc目录下的build删除掉，重新编译就创建成功了
 * 生成的文件路径，每个版本路径不一样，使用的时候输出看一下路径有没有问题
 * E:\github\TransformDemo\app\build\intermediates\transforms\AsmTestTransform\debug\27\com\example\transformdemo\AsmCreateTest.class
 */
public class CreateAdapter {

    final String PACKAGE_NAME = "\\com\\example\\transformdemo\\"
    
    String destName;
    
    public CreateAdapter(String name) {
        this.destName = name + PACKAGE_NAME
        println("=========================> destName: " + destName);
    }

    /**
     * 创建一个文件
     * @param destName 目标名称，绝对路径
     */
    public void createFile() {
        FileUtils.forceMkdir(new File(destName))
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_5, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,  "pkg/Comparable", null, "java/lang/Object", null);
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS", "I",  null, new Integer(-1)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL", "I",  null, new Integer(0)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER", "I",  null, new Integer(1)).visitEnd();
        cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "compareTo",  "(Ljava/lang/Object;)I", null, null).visitEnd();
        cw.visitEnd()
        byte[] b = cw.toByteArray();
        FileOutputStream fos = new FileOutputStream(new File(destName + "AsmCreateTest.class"))
        fos.write(b)
        fos.flush()
        fos.close()
    }

    /**
     * 例子
     * https://blog.csdn.net/iteye_6787/article/details/82635858?spm=1001.2014.3001.5501
     */
    public void createFile1() {
        FileUtils.forceMkdir(new File(destName))
        byte[] b = gen();
        FileOutputStream fos = new FileOutputStream(new File(destName + "ChildClass.class"))
        fos.write(b)
        fos.flush()
        fos.close()
    }

    private static byte[] gen() {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT ,
                "asm/core/ChildClass", null, "java/lang/Object", /*new String[]{"asm/core/ParentInter"}*/[ "asm/core/ParentInter" ] as String[]);

        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "zero", "I", null, new Integer(0))
                .visitEnd();

        cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, "compareTo", "(Ljava/lang/Object;)I", null, null)
                .visitEnd();
        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * https://blog.csdn.net/iteye_6787/article/details/82636693?spm=1001.2014.3001.5501
     * 创建方法，内容如下
     * xx\app\build\intermediates\transforms\AsmTestTransform\debug\27\com\example\transformdemo\MethodGenClass.class
     package bytecode;

     public class MethodGenClass {
         private int espresso;

         public void addEspresso(int var1) {
             if (var1 >= 0) {
                 this.espresso = var1;
             } else {
             throw new IllegalArgumentException();
             }
         }
     }
     * @throws IOException
     */
    public void createMethod() throws IOException {
        FileUtils.forceMkdir(new File(destName))
        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "bytecode/MethodGenClass", null, "java/lang/Object", null);
        cw.visitField(Opcodes.ACC_PRIVATE, "espresso", "I", null, null).visitEnd();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "addEspresso", "(I)V", null, null);
        // 方法访问开始
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        // label 代表跳转的字节码位置。
        Label label = new Label();
        mv.visitJumpInsn(Opcodes.IFLT, label);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, "bytecode/MethodGenClass", "espresso", "I");
        Label end = new Label();
        mv.visitJumpInsn(Opcodes.GOTO, end);
        mv.visitLabel(label);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        // 创建Exception对象指令
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
        mv.visitInsn(Opcodes.DUP);
        // 调用方法指令
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "()V", false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(end);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 2);
        // 方法访问结束
        mv.visitEnd();
        cw.visitEnd();

        byte[] b = cw.toByteArray();

        File file = new File(destName + "MethodGenClass.class");
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(b);
        fout.close();
    }

}