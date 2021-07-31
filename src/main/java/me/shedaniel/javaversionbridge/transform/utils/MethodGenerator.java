/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.utils;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.commons.InstructionAdapter;
import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.MethodNode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class MethodGenerator {
    private static final AtomicInteger METHOD_COUNTER = new AtomicInteger();
    
    public static MethodNode makeMethod(boolean isStatic, String name, String desc, Consumer<InstructionAdapter> filler) {
        MethodNode out = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | (isStatic ? Opcodes.ACC_STATIC : 0), name + '£' + METHOD_COUNTER.getAndIncrement(), desc, null, null);
        
        filler.accept(new InstructionAdapter(out));
        
        return out;
    }
}
