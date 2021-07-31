/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.bridge.v15;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.AbstractInsnNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.MethodInsnNode;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.MethodNode;
import dev.architectury.transformer.transformers.base.ClassEditTransformer;
import dev.architectury.transformer.util.Logger;
import me.shedaniel.javaversionbridge.transform.bridge.ASMHelper;

import java.util.Set;

public class RedirectJ15Code implements ClassEditTransformer {
    public RedirectJ15Code(Set<String> flags) {
    }
    
    @Override
    public ClassNode doEdit(String s, ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
                    MethodInsnNode insnNode = (MethodInsnNode) instruction;
                    
                    if (insnNode.owner.equals("java/lang/String")) {
                        if (insnNode.name.equals("formatted") && insnNode.desc.equals("([Ljava/lang/Object;)Ljava/lang/String;")) {
                            Logger.debug("Redirected String.formatted in " + ASMHelper.format(classNode, method));
                            insnNode.setOpcode(Opcodes.INVOKESTATIC);
                            insnNode.name = "format";
                            insnNode.desc = "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;";
                        }
                    }
                }
            }
        }
        return classNode;
    }
}
