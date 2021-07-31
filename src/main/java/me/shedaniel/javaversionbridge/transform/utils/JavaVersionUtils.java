/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform.utils;

import dev.architectury.transformer.shadowed.impl.org.objectweb.asm.Opcodes;
import org.gradle.api.JavaVersion;

public class JavaVersionUtils {
    public static int toAsmVersion(JavaVersion version) {
        if (version == JavaVersion.VERSION_17) {
            return Opcodes.V17;
        } else if (version == JavaVersion.VERSION_16) {
            return Opcodes.V16;
        } else if (version == JavaVersion.VERSION_15) {
            return Opcodes.V15;
        } else if (version == JavaVersion.VERSION_14) {
            return Opcodes.V14;
        } else if (version == JavaVersion.VERSION_13) {
            return Opcodes.V13;
        } else if (version == JavaVersion.VERSION_12) {
            return Opcodes.V12;
        } else if (version == JavaVersion.VERSION_11) {
            return Opcodes.V11;
        } else if (version == JavaVersion.VERSION_1_10) {
            return Opcodes.V10;
        } else if (version == JavaVersion.VERSION_1_9) {
            return Opcodes.V9;
        } else if (version == JavaVersion.VERSION_1_8) {
            return Opcodes.V1_8;
        } else if (version == JavaVersion.VERSION_1_7) {
            return Opcodes.V1_7;
        } else if (version == JavaVersion.VERSION_1_6) {
            return Opcodes.V1_6;
        } else if (version == JavaVersion.VERSION_1_5) {
            return Opcodes.V1_5;
        } else if (version == JavaVersion.VERSION_1_4) {
            return Opcodes.V1_4;
        } else if (version == JavaVersion.VERSION_1_3) {
            return Opcodes.V1_3;
        } else if (version == JavaVersion.VERSION_1_2) {
            return Opcodes.V1_2;
        } else if (version == JavaVersion.VERSION_1_1) {
            return Opcodes.V1_1;
        } else {
            throw new IllegalArgumentException("Illegal version: " + version);
        }
    }
}
