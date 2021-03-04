package com.example.asm2

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.example.asm2.coreapi.AddTimerAdapter
import com.example.asm2.coreapi.AddingClassesVisitor
import com.example.asm2.coreapi.ChangeAccessAdapter
import com.example.asm2.coreapi.CreateAdapter
import com.example.asm2.coreapi.LifecycleClassVisitor2
import com.example.asm2.coreapi.ModifyMethodAdapter
import com.example.asm2.coreapi.RemovingClassesVisitor
import com.example.asm2.treeapi.AddFieldTransformer
import com.example.asm2.treeapi.RemoveMethodTransformer
import com.example.asm2.treeapi.TreeCreateAdapter
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * Asm测试相关
 */
class AsmTestTransform extends Transform {
    
    private int type = TEST_TYPE_TREEAPI_MODIFY_FILE

    // coreapi
    private static final TEST_TYPE_CREATE_FILE = 1 // 创建文件
    private static final TEST_TYPE_INSERT = 2 // 在onCreate onDestory插入代码
    private static final TEST_TYPE_MODIFY = 3 // 修改文件
    private static final TEST_TYPE_REMOVE_MENBER = 4 // 移除类成员
    private static final TEST_TYPE_ADD_MENBER = 5 // 移除类成员
    private static final TEST_TYPE_CREATE_METHOD = 6 // 创建方法
    private static final TEST_TYPE_INJECT_METHOD = 7 // 无状态注入方法逻辑
    private static final TEST_TYPE_INJECT_METHOD1 = 8 // 有状态注入方法逻辑

    // treeapi
    private static final TEST_TYPE_TREEAPI_CREATE_FILE = 21 // 创建文件
    private static final TEST_TYPE_TREEAPI_MODIFY_FILE = 22 // 修改文件
    private static final TEST_TYPE_TREEAPI_CREATE_METHOD = 22 // 创建方法
    
    final String NAME =  "AsmTestTransform"

    @Override
    String getName() {
        return NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        log('transform')
        // OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        
        //删除之前的输出,否则每次生成的jar都会存在这里，导致下面步骤复制到另外文件夹耗时很长
        if (outputProvider != null)
            outputProvider.deleteAll()
        
        transformInvocation.inputs.each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                // 处理Jar
                processJarInput(jarInput, outputProvider)
            }
            input.directoryInputs.each { DirectoryInput directoryInput ->
                // 处理源码文件
                processDirectoryInputs(directoryInput, outputProvider)
            }
        }
    }

    /**
     * 扫描所有输入的文件夹，自己写的类编译成class在这里作响应处理
     * @param jarInput
     * @param outputProvider
     */
    void processDirectoryInputs(DirectoryInput directoryInput, TransformOutputProvider outputProvider) {

        log('processDirectoryInputs name:' + (directoryInput.getFile().getPath() + directoryInput.getFile().getName()))
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
                directoryInput.getContentTypes(), directoryInput.getScopes(),
                Format.DIRECTORY)

        // 建立文件夹        
        FileUtils.forceMkdir(dest)

        // 创建文件
        if (type == TEST_TYPE_CREATE_FILE) {
//            new CreateAdapter(dest.getPath()).createFile1()
//            createFile(dest)
        }
        if (type == TEST_TYPE_TREEAPI_CREATE_FILE) {
            new TreeCreateAdapter(dest.getPath()).createFile()
        }

        // 创建方法
        if (type == TEST_TYPE_CREATE_METHOD)
            new CreateAdapter(dest.getPath()).createMethod()

        if (type == TEST_TYPE_TREEAPI_CREATE_METHOD)
            new TreeCreateAdapter(dest.getPath()).createMethod()

        def startTime = System.currentTimeMillis()

        // to do some transform
        if (type <= 20) {
            if (directoryInput.file.isDirectory()) {
                // 列出目录所有文件（包含子文件夹，子文件夹内文件）
                directoryInput.file.eachFileRecurse { File file ->
                    def name = file.name
                    if (checkFile(name)) {
                        log("满足条件")
                        ClassReader classReader = new ClassReader(file.bytes)
                        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                        ClassVisitor cv = getClassVisitor(classWriter)
                        classReader.accept(cv, EXPAND_FRAMES)
                        byte[] code = classWriter.toByteArray()
                        String path = file.parentFile.absolutePath + File.separator + name
                        log("path:" + path)
                        FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name)
                        fos.write(code)
                        fos.close()
                    }
                }
            }
        } else { // tree api
            if (directoryInput.file.isDirectory()) {
                // 列出目录所有文件（包含子文件夹，子文件夹内文件）
                directoryInput.file.eachFileRecurse { File file ->
                    def name = file.name
                    if (checkTreeFile(name)) {
                        log("Tree满足条件")
                        ClassReader cr = new ClassReader(file.bytes)
                        ClassNode cn = new ClassNode()
                        cr.accept(cn,0)
                        RemoveMethodTransformer rt = new RemoveMethodTransformer("isNeedRemove","V")
                        rt.transform(cn)
                        AddFieldTransformer at= new AddFieldTransformer(Opcodes.ACC_PRIVATE,"addedField","I")
                        at.transform(cn)
                        ClassWriter cw = new ClassWriter(0)
                        cn.accept(cw)

                        byte[] code = cw.toByteArray()
                        String path = file.parentFile.absolutePath + File.separator + name
                        log("path:" + path)
                        FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + name) // 重新写入文件
                        fos.write(code)
                        fos.close()
                    }
                }
            }
        }
        def cost = (System.currentTimeMillis() - startTime) / 1000
        log("修改文件时长:  " + cost + " S")
        
        startTime = System.currentTimeMillis()

        // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了        
        // srcDir: directoryInput.getFile().getPath() xx\app\build\intermediates\javac\debug\classes
        // destDir: dest.getPath() xx\app\build\intermediates\transforms\AsmTestTransform\debug\27
        FileUtils.copyDirectory(directoryInput.getFile(), dest)

        cost = (System.currentTimeMillis() - startTime) / 1000
        log("复制文件时长:  " + cost + " S")
    }

    private ClassVisitor getClassVisitor(ClassWriter classWriter) {
        if(type == TEST_TYPE_INSERT) { // 在onCreate onDestory插入代码
            new LifecycleClassVisitor2(classWriter)
        } else if(type == TEST_TYPE_MODIFY) { // 修改文件
            new ChangeAccessAdapter(classWriter)
        } else if(type == TEST_TYPE_REMOVE_MENBER) { // 移除类成员
            new RemovingClassesVisitor(classWriter)   
        } else if(type == TEST_TYPE_ADD_MENBER) { // 添加类成员
            new AddingClassesVisitor(classWriter, Opcodes.ACC_PRIVATE,"addedField","I");
        } else if(type == TEST_TYPE_INJECT_METHOD) {
            new AddTimerAdapter(classWriter)
        } else if(type == TEST_TYPE_INJECT_METHOD1) {
            new ModifyMethodAdapter(classWriter)
        }
    }
    
    
    /**
     * 创建文件
     * @param dest
     */
    private void createFile(File dest) {
        // E:\github\TransformDemo\app\build\intermediates\transforms\AsmTestTransform\debug\27
        log("3 " + dest.getPath())
        // 1. 创建文件
        new CreateAdapter(dest.getPath()).createFile();
    }

    /**
     * 扫描所有输入的Jar，做响应的处理
     * @param jarInput
     * @param outputProvider
     */
    void processJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        File dest = outputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR)

        // to do some transform

        // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了        
        FileUtils.copyFile(jarInput.getFile(), dest)
    }

    /**
     * 根据type检查是否是需要处理的文件
     * @param name
     * @return
     */
    private boolean checkTreeFile(String name) {
        if(type == TEST_TYPE_TREEAPI_MODIFY_FILE)
            return isTreeModifyFile(name)
        return false
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isTreeModifyFile(String name) {
        return isNotExcludeFile(name) && "TreeTask.class".equals(name)
    }

    /**
     * 根据type检查是否是需要处理的文件
     * @param name
     * @return
     */
    private boolean checkFile(String name) {
        if(type == TEST_TYPE_INSERT)
            return isMainActivityFile(name)
        if(type == TEST_TYPE_MODIFY)
            return isModifyFile(name)
        if(type == TEST_TYPE_REMOVE_MENBER)
            return isRemove(name)
        if(type == TEST_TYPE_ADD_MENBER)
            return isAdd(name)
        if(type == TEST_TYPE_INJECT_METHOD)
            return isInject1(name)
        if(type == TEST_TYPE_INJECT_METHOD1)
            return isInject2(name)
        return false
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isInject2(String name) {
        return isNotExcludeFile(name) && "TestMethod.class".equals(name)
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isInject1(String name) {
        return isNotExcludeFile(name) && "Time.class".equals(name)
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isAdd(String name) {
        return isNotExcludeFile(name) && "Task.class".equals(name)
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isRemove(String name) {
        return isNotExcludeFile(name) && "Task.class".equals(name)
    }

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isModifyFile(String name) {
        return isNotExcludeFile(name) && "ChildClass.class".equals(name)
    }
    

    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    private boolean isMainActivityFile(String name) {
//        log("2 " + name)
        //只处理需要的class文件
//        if(name != null && name.contains("Activity")) {
//            println '---------------------------------> ' + name
//        }
        return isNotExcludeFile(name) && "MainActivity.class".equals(name);
    }

    /**
     * 公共排除一些不需要修改的文件
     * @return
     */
    private boolean isNotExcludeFile(String name) {
        return (name.endsWith(".class") && !name.startsWith("R\$")
                && !"R.class".equals(name) && !"BuildConfig.class".equals(name))
    }
    
    static void log(String str) {
        println '=========================================> ' + str
    }
}

