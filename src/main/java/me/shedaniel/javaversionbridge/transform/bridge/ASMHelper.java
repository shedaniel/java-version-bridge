/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.bridge;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.AnnotationNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.FieldNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;

public class ASMHelper {
    public static void addVisibleAnnotation(ClassNode node, AnnotationNode annotationNode) {
        if (node.visibleAnnotations == null) {
            node.visibleAnnotations = new ArrayList<>();
        }
        
        node.visibleAnnotations.add(annotationNode);
    }
    
    public static void addInvisibleAnnotation(ClassNode node, AnnotationNode annotationNode) {
        if (node.invisibleAnnotations == null) {
            node.invisibleAnnotations = new ArrayList<>();
        }
        
        node.invisibleAnnotations.add(annotationNode);
    }
    
    public static void addVisibleAnnotation(MethodNode node, AnnotationNode annotationNode) {
        if (node.visibleAnnotations == null) {
            node.visibleAnnotations = new ArrayList<>();
        }
        
        node.visibleAnnotations.add(annotationNode);
    }
    
    public static void addInvisibleAnnotation(MethodNode node, AnnotationNode annotationNode) {
        if (node.invisibleAnnotations == null) {
            node.invisibleAnnotations = new ArrayList<>();
        }
        
        node.invisibleAnnotations.add(annotationNode);
    }
    
    public static void addVisibleAnnotation(FieldNode node, AnnotationNode annotationNode) {
        if (node.visibleAnnotations == null) {
            node.visibleAnnotations = new ArrayList<>();
        }
        
        node.visibleAnnotations.add(annotationNode);
    }
    
    public static void addInvisibleAnnotation(FieldNode node, AnnotationNode annotationNode) {
        if (node.invisibleAnnotations == null) {
            node.invisibleAnnotations = new ArrayList<>();
        }
        
        node.invisibleAnnotations.add(annotationNode);
    }
    
    public static String format(ClassNode owner, MethodNode method) {
        return owner.name + "." + format(method);
    }
    
    public static String format(MethodNode method) {
        return method.name + method.desc;
    }
}
