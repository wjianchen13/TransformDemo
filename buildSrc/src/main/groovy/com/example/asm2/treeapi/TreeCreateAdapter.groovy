package com.example.asm2.treeapi

import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * 刚开始创建文件没有成功，目标目录没有自己创建的文件，把buildSrc目录下的build删除掉，重新编译就创建成功了
 * 生成的文件路径，每个版本路径不一样，使用的时候输出看一下路径有没有问题
 * E:\github\TransformDemo\app\build\intermediates\transforms\AsmTestTransform\debug\27\com\example\transformdemo\AsmCreateTest.class
 */
public class TreeCreateAdapter {

    final String PACKAGE_NAME = "\\com\\example\\transformdemo\\"
    
    String destName;
    
    public TreeCreateAdapter(String name) {
        this.destName = name + PACKAGE_NAME
        println("=========================> destName: " + destName);
    }

    /**
     * 创建一个文件
     * https://blog.csdn.net/iteye_6787/article/details/82637363?spm=1001.2014.3001.5501
     */
    public void createFile() {
        FileUtils.forceMkdir(new File(destName))
        ClassWriter cw = new ClassWriter(Opcodes.ASM5);
        ClassNode cn = gen();
        cn.accept(cw);
        File file = new File(destName + "TreeChildClass.class");
        FileOutputStream fout = new FileOutputStream(file);
        try {
            fout.write(cw.toByteArray());
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ClassNode gen() {
        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT;
        classNode.name = "asm/core/ChildClass";
        classNode.superName = "java/lang/Object";
        classNode.interfaces.add("asm/core/ParentInter");
        classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC, "zero", "I",
                null, new Integer(0)));
        classNode.methods.add(new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, "compareTo",
                "(Ljava/lang/Object;)I", null, null));
        return classNode;
    }

    /**
     * https://blog.csdn.net/iteye_6787/article/details/82637791?spm=1001.2014.3001.5501
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

        ClassNode classNode = new ClassNode();
        classNode.version = Opcodes.V1_8;
        classNode.access = Opcodes.ACC_PUBLIC;
        classNode.name = "bytecode/TreeMethodGenClass";
        classNode.superName = "java/lang/Object";
        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "espresso", "I", null, null));
        // public void addEspresso(int espresso) 方法生命
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, "addEspresso", "(I)V", null, null);
        classNode.methods.add(mn);
        InsnList il = mn.instructions;
        il.add(new VarInsnNode(Opcodes.ILOAD, 1));
        il.add(new InsnNode(Opcodes.ICONST_1));
        LabelNode label = new LabelNode();
        // if (espresso > 0) 跳转通过LabelNode标记跳转地址
        il.add(new JumpInsnNode(Opcodes.IF_ICMPLE, label));
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        il.add(new VarInsnNode(Opcodes.ILOAD, 1));
        // this.espresso = var1;
        il.add(new FieldInsnNode(Opcodes.PUTFIELD, "bytecode/TreeMethodGenClass", "espresso", "I"));
        LabelNode end = new LabelNode();
        il.add(new JumpInsnNode(Opcodes.GOTO, end));
        // label 后紧跟着下一个指令地址
        il.add(label);
        // java7之后对stack map frame 的处理
        il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        // throw new IllegalArgumentException();
        il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/IllegalArgumentException"));
        il.add(new InsnNode(Opcodes.DUP));
        il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "()V", false));
        il.add(new InsnNode(Opcodes.ATHROW));
        il.add(end);
        // stack map 的第二次偏移记录
        il.add(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));
        il.add(new InsnNode(Opcodes.RETURN));
        // 局部变量表和操作数栈大小的处理
        mn.maxStack = 2;
        mn.maxLocals = 2;
        mn.visitEnd();
        // 打印查看class的生成结果
        ClassWriter cw = new ClassWriter(Opcodes.ASM5);
        classNode.accept(cw);
        File file = new File(destName + "TreeMethodGenClass.class");
        FileOutputStream fout = new FileOutputStream(file);
        try {
            fout.write(cw.toByteArray());
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
}