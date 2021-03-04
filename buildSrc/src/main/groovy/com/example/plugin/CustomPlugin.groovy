package com.example.plugin

import com.example.asm1.MyTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.example.asm2.AsmTestTransform

class CustomPlugin implements Plugin<Project> {
    void apply(Project project) {
        System.out.println("========================")
        System.out.println("这是个插件!")
        System.out.println("========================")

//        project.android.registerTransform(new MyTransform())
        project.android.registerTransform(new AsmTestTransform())
    }
    
}

