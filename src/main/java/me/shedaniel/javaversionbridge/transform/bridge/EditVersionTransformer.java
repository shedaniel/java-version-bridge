/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.bridge;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.tree.ClassNode;
import dev.architectury.transformer.transformers.base.ClassEditTransformer;
import me.shedaniel.javaversionbridge.transform.utils.JavaVersionUtils;
import org.gradle.api.JavaVersion;

public class EditVersionTransformer implements ClassEditTransformer {
    public final int newVersion;
    
    public EditVersionTransformer(JavaVersion newVersion) {
        this.newVersion = JavaVersionUtils.toAsmVersion(newVersion);
    }
    
    @Override
    public ClassNode doEdit(String s, ClassNode classNode) {
        classNode.version = newVersion;
        return classNode;
    }
}
