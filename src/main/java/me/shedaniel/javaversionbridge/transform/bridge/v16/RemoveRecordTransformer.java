/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.bridge.v16;

import dev.architectury.transformer.shadowed.impl.com.google.common.base.Verify;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Handle;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.*;
import dev.architectury.transformer.transformers.base.ClassEditTransformer;
import dev.architectury.transformer.util.Logger;
import me.shedaniel.javaversionbridge.transform.bridge.ASMHelper;
import me.shedaniel.javaversionbridge.transform.utils.AnnotationUtils;
import me.shedaniel.javaversionbridge.transform.utils.RecordUtils;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class RemoveRecordTransformer implements ClassEditTransformer {
    public static final String INSERT_CONSTRUCTOR_PROPERTIES_FLAG = "insertRecordConstructorProperties";
    private final boolean insertConstructorProperties;
    
    public RemoveRecordTransformer(Set<String> flags) {
        this.insertConstructorProperties = flags.contains(INSERT_CONSTRUCTOR_PROPERTIES_FLAG);
    }
    
    @Override
    public ClassNode doEdit(String s, ClassNode classNode) {
        if ((classNode.access & Opcodes.ACC_RECORD) != 0) {
            classNode.superName = "java/lang/Object";
            classNode.access &= ~Opcodes.ACC_RECORD;
            ASMHelper.addInvisibleAnnotation(classNode, new AnnotationNode(AnnotationUtils.WAS_RECORD));
            List<MethodNode> extraMethods = new ArrayList<>();
            Map<Integer, Object> equalsMap = new HashMap<>();
            
            for (MethodNode method : classNode.methods) {
                if (method.name.equals("<init>")) {
                    for (AbstractInsnNode instruction : method.instructions) {
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode methodInsn = (MethodInsnNode) instruction;
                            if (methodInsn.owner.equals("java/lang/Record")) {
                                methodInsn.owner = "java/lang/Object";
                            }
                        }
                    }
                    
                    if (insertConstructorProperties) {
                        AnnotationNode node = new AnnotationNode("Ljava/beans/ConstructorProperties;");
                        node.visit("value", classNode.fields.stream().filter(fieldNode -> (fieldNode.access & Opcodes.ACC_STATIC) == 0)
                                .map(fieldNode -> fieldNode.name)
                                .collect(Collectors.toList()));
                        ASMHelper.addVisibleAnnotation(method, node);
                    }
                } else {
                    boolean isInterface = Modifier.isInterface(classNode.access);
                    ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                    while (iterator.hasNext()) {
                        AbstractInsnNode instruction = iterator.next();
                        
                        if (instruction.getType() == AbstractInsnNode.INVOKE_DYNAMIC_INSN) {
                            InvokeDynamicInsnNode dynamicNode = (InvokeDynamicInsnNode) instruction;
                            Handle handle = dynamicNode.bsm;
                            if (handle.getOwner().equals("java/lang/runtime/ObjectMethods")) {
                                if ("bootstrap".equals(handle.getName())) {
                                    Handle[] fields = Arrays.copyOfRange(dynamicNode.bsmArgs, 2, dynamicNode.bsmArgs.length, Handle[].class);
                                    
                                    if (dynamicNode.bsmArgs[1] != null) {//Is allowed to be null when idin.name is equals or hashCode
                                        String template = (String) dynamicNode.bsmArgs[1];
                                        
                                        if (template.isEmpty()) {
                                            Verify.verify(fields.length == 0, "Expected no getters but received %s", Arrays.toString(fields));
                                        } else {
                                            String[] names = Arrays.stream(fields).map(Handle::getName).toArray(String[]::new);
                                            Verify.verify(Arrays.equals(template.split(";"), names), "Expected %s == %s", template, Arrays.toString(names));
                                        }
                                    }
                                    
                                    MethodNode implementation;
                                    switch (dynamicNode.name) {
                                        case "equals":
                                            implementation = RecordUtils.makeEquals(classNode, extraMethods, equalsMap, classNode.name, fields);
                                            break;
                                        
                                        case "hashCode":
                                            implementation = RecordUtils.makeHashCode(classNode.name, fields);
                                            break;
                                        
                                        case "toString":
                                            implementation = RecordUtils.makeToString(classNode.name, fields);
                                            break;
                                        
                                        default:
                                            throw new IllegalArgumentException("Unexpected object method name: " + dynamicNode.name);
                                    }
                                    
                                    //System.out.println("Transforming " + idin.name + idin.desc + " to " + concat.name + concat.desc);
                                    Verify.verify(!isInterface, "%s has instance method %s generated but is an interface?", classNode.name, dynamicNode.name);
                                    iterator.set(new MethodInsnNode(Opcodes.INVOKESPECIAL, classNode.name, implementation.name, implementation.desc, false));
                                    extraMethods.add(implementation);
                                }
                            }
                        }
                    }
                }
            }
            
            Logger.debug("Removed record from " + classNode.name);
            for (MethodNode extraMethod : extraMethods) {
                ASMHelper.addInvisibleAnnotation(extraMethod, new AnnotationNode(AnnotationUtils.METHOD_GENERATED));
                Logger.debug("Adding method " + ASMHelper.format(classNode, extraMethod));
            }
            classNode.methods.addAll(extraMethods);
        }
    
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode insnNode = (MethodInsnNode) instruction;
            
                    if (insnNode.owner.equals("java/lang/Record")) {
                        insnNode.owner = "java/lang/Object";
                    }
                }
            }
        }
        
        return classNode;
    }
}
