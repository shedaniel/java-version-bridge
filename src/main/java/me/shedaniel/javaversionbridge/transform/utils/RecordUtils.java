/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.utils;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Handle;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Label;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Type;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.commons.InstructionAdapter;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RecordUtils {
    private static void equals(Function<Type, MethodEntry> equalsMethodProvider, InstructionAdapter method, Handle field) {
        Type type = Type.getType(field.getDesc());
        MethodEntry entry = equalsMethodProvider.apply(type);
        method.invokestatic(entry.owner, entry.name, entry.descriptor, false);
    }
    
    static class MethodEntry {
        private final String owner;
        private final String name;
        private final String descriptor;
        
        public MethodEntry(String owner, String name, String descriptor) {
            this.owner = owner;
            this.name = name;
            this.descriptor = descriptor;
        }
        
        public String getOwner() {
            return owner;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescriptor() {
            return descriptor;
        }
    }
    
    public static MethodNode makeEquals(ClassNode classNode, List<MethodNode> extraMethods, Map<Integer, Object> equalsMap, String type, Handle... fields) {
        return _makeEquals(t -> {
            String owner, name, desc;
            switch (t.getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.SHORT:
                case Type.CHAR:
                case Type.INT:
                case Type.LONG:
                case Type.FLOAT:
                case Type.DOUBLE:
                    MethodEntry entry = (MethodEntry) equalsMap.computeIfAbsent(t.getSort(), $ -> {
                        MethodNode m = MethodGenerator.makeMethod(true, "equals", '(' + t.getDescriptor() + t.getDescriptor() + ")Z", method -> {
                            Label label = new Label();
                            Label label2 = new Label();
                            int i = 0;
                            method.visitVarInsn(t.getOpcode(Opcodes.ILOAD), i);
                            i += t.getSize();
                            method.visitVarInsn(t.getOpcode(Opcodes.ILOAD), i);
                            if (t.getSort() == Type.LONG) {
                                method.visitInsn(Opcodes.LCMP);
                                method.visitJumpInsn(Opcodes.IFNE, label);
                            } else if (t.getSort() == Type.FLOAT) {
                                method.visitInsn(Opcodes.FCMPL);
                                method.visitJumpInsn(Opcodes.IFNE, label);
                            } else if (t.getSort() == Type.DOUBLE) {
                                method.visitInsn(Opcodes.DCMPL);
                                method.visitJumpInsn(Opcodes.IFNE, label);
                            } else {
                                method.visitJumpInsn(Opcodes.IF_ICMPNE, label);
                            }
                            method.visitInsn(Opcodes.ICONST_1);
                            method.visitJumpInsn(Opcodes.GOTO, label2);
                            method.visitLabel(label);
                            method.visitInsn(Opcodes.ICONST_0);
                            method.visitLabel(label2);
                            method.visitInsn(Opcodes.IRETURN);
                        });
                        extraMethods.add(m);
                        return new MethodEntry(classNode.name, m.name, m.desc);
                    });
                    owner = entry.owner;
                    name = entry.name;
                    desc = entry.descriptor;
                    break;
                
                case Type.ARRAY:
                case Type.OBJECT:
                    owner = "java/util/Objects";
                    name = "equals";
                    desc = "(Ljava/lang/Object;Ljava/lang/Object;)Z";
                    break;
                
                case Type.VOID:
                case Type.METHOD:
                default:
                    throw new IllegalArgumentException("Unexpected field type: " + t.getDescriptor() + " (sort " + t.getSort() + ')');
            }
            
            return new MethodEntry(owner, name, desc);
        }, type, fields);
    }
    
    public static MethodNode _makeEquals(Function<Type, MethodEntry> equalsMethodProvider, String type, Handle... fields) {
        return MethodGenerator.makeMethod(false, "equals", "(Ljava/lang/Object;)Z", method -> {
            Type thisType = Type.getObjectType(type);
            
            method.load(1, InstructionAdapter.OBJECT_TYPE);
            method.load(0, thisType);
            Label notIdenticallyEqual = new Label();
            method.ifacmpne(notIdenticallyEqual);
            method.iconst(1);
            method.areturn(Type.BOOLEAN_TYPE);
            method.mark(notIdenticallyEqual);
            
            method.load(1, InstructionAdapter.OBJECT_TYPE);
            method.instanceOf(thisType);
            Label isInstance = new Label();
            method.ifne(isInstance);
            method.iconst(0);
            method.areturn(Type.BOOLEAN_TYPE);
            method.mark(isInstance);
            
            if (fields.length > 0) {
                method.load(1, InstructionAdapter.OBJECT_TYPE);
                method.checkcast(thisType);
                method.store(2, thisType);
                
                for (Handle field : fields) {
                    method.load(0, thisType);
                    method.getfield(type, field.getName(), field.getDesc());
                    method.load(2, thisType);
                    method.getfield(type, field.getName(), field.getDesc());
                    equals(equalsMethodProvider, method, field);
                    Label equal = new Label();
                    method.ifne(equal);
                    method.iconst(0);
                    method.areturn(Type.BOOLEAN_TYPE);
                    method.mark(equal);
                }
            }
            
            method.iconst(1);
            method.areturn(Type.BOOLEAN_TYPE);
        });
    }
    
    private static void hashCode(InstructionAdapter method, Handle field) {
        String owner, desc;
        switch (Type.getType(field.getDesc()).getSort()) {
            case Type.BOOLEAN:
                owner = "java/lang/Boolean";
                desc = "(Z)I";
                break;
            
            case Type.BYTE:
                owner = "java/lang/Byte";
                desc = "(B)I";
                break;
            
            case Type.SHORT:
                owner = "java/lang/Short";
                desc = "(S)I";
                break;
            
            case Type.CHAR:
                owner = "java/lang/Character";
                desc = "(C)I";
                break;
            
            case Type.INT:
                owner = "java/lang/Integer";
                desc = "(I)I";
                break;
            
            case Type.LONG:
                owner = "java/lang/Long";
                desc = "(J)I";
                break;
            
            case Type.FLOAT:
                owner = "java/lang/Float";
                desc = "(F)I";
                break;
            
            case Type.DOUBLE:
                owner = "java/lang/Double";
                desc = "(D)I";
                break;
            
            case Type.ARRAY:
            case Type.OBJECT:
                owner = "java/util/Objects";
                desc = "(Ljava/lang/Object;)I";
                break;
            
            case Type.VOID:
            case Type.METHOD:
            default:
                throw new IllegalArgumentException("Unexpected field type: " + field.getDesc() + " (sort " + Type.getType(field.getDesc()).getSort() + ')');
        }
        
        method.invokestatic(owner, "hashCode", desc, false);
    }
    
    public static MethodNode makeHashCode(String type, Handle... fields) {
        return MethodGenerator.makeMethod(false, "hashCode", "()I", method -> {
            if (fields.length > 0) {
                Type thisType = Type.getObjectType(type);
                Handle field = fields[0];
                method.load(0, thisType);
                method.getfield(type, field.getName(), field.getDesc());
                hashCode(method, field);
                
                for (int i = 1, end = fields.length; i < end; i++) {
                    field = fields[i];
                    
                    method.iconst(31);
                    method.mul(Type.INT_TYPE);
                    
                    method.load(0, thisType);
                    method.getfield(type, field.getName(), field.getDesc());
                    hashCode(method, field);
                    method.add(Type.INT_TYPE);
                }
            } else {
                method.iconst(0);
            }
            
            method.areturn(Type.INT_TYPE);
        });
    }
    
    private static void toString(InstructionAdapter method, Handle field) {
        String owner, name, desc;
        switch (Type.getType(field.getDesc()).getSort()) {
            case Type.BOOLEAN:
                owner = "java/lang/String";
                name = "valueOf";
                desc = "(Z)Ljava/lang/String;";
                break;
            
            case Type.BYTE:
                owner = "java/lang/Byte";
                name = "toString";
                desc = "(B)Ljava/lang/String;";
                break;
            
            case Type.SHORT:
                owner = "java/lang/Short";
                name = "toString";
                desc = "(S)Ljava/lang/String;";
                break;
            
            case Type.CHAR:
                owner = "java/lang/String";
                name = "valueOf";
                desc = "(C)Ljava/lang/String;";
                break;
            
            case Type.INT:
                owner = "java/lang/Integer";
                name = "toString";
                desc = "(I)Ljava/lang/String;";
                break;
            
            case Type.LONG:
                owner = "java/lang/Long";
                name = "toString";
                desc = "(J)Ljava/lang/String;";
                break;
            
            case Type.FLOAT:
                owner = "java/lang/Float";
                name = "toString";
                desc = "(F)Ljava/lang/String;";
                break;
            
            case Type.DOUBLE:
                owner = "java/lang/Double";
                name = "toString";
                desc = "(D)Ljava/lang/String;";
                break;
            
            case Type.ARRAY:
            case Type.OBJECT:
                owner = "java/lang/String";
                name = "valueOf";
                desc = "(Ljava/lang/Object;)Ljava/lang/String;";
                break;
            
            case Type.VOID:
            case Type.METHOD:
            default:
                throw new IllegalArgumentException("Unexpected field type: " + field.getDesc() + " (sort " + Type.getType(field.getDesc()).getSort() + ')');
        }
        
        method.invokestatic(owner, name, desc, false);
    }
    
    public static MethodNode makeToString(String type, Handle... fields) {
        return MethodGenerator.makeMethod(false, "toString", "()Ljava/lang/String;", method -> {
            if (fields.length > 0) {
                method.anew(Type.getObjectType("java/lang/StringBuilder"));
                method.dup();
                method.invokespecial("java/lang/StringBuilder", "<init>", "()V", false);
                
                Type thisType = Type.getObjectType(type);
                Handle field = fields[0];
                method.aconst(type + '[' + field.getName() + '=');
                method.invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                method.load(0, thisType);
                method.getfield(type, field.getName(), field.getDesc());
                toString(method, field);
                method.invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                
                for (int i = 1, end = fields.length; i < end; i++) {
                    field = fields[i];
                    
                    method.aconst(", " + field.getName() + '=');
                    method.invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                    method.load(0, thisType);
                    method.getfield(type, field.getName(), field.getDesc());
                    toString(method, field);
                    method.invokevirtual("java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
                }
                
                method.iconst(']');
                method.invokevirtual("java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
                method.invokevirtual("java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            } else {
                method.aconst(type.concat("[]"));
            }
            
            method.areturn(Type.getObjectType("java/lang/String"));
        });
    }
}