/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform;

import dev.architectury.transformer.shadowed.impl.com.google.common.collect.ImmutableList;
import me.shedaniel.javaversionbridge.transform.bridge.v15.RedirectJ15Code;
import me.shedaniel.javaversionbridge.transform.bridge.v16.RemoveRecordTransformer;
import org.gradle.api.JavaVersion;

import java.util.HashMap;
import java.util.Map;

public class BridgeRegistry {
    public static final Map<JavaVersion, Bridge> bridges = new HashMap<>();
    
    static {
        bridges.put(JavaVersion.VERSION_16, flags -> ImmutableList.of(
                new RemoveRecordTransformer(flags)
        ));
        bridges.put(JavaVersion.VERSION_15, flags -> ImmutableList.of(
                new RedirectJ15Code(flags)
        ));
    }
}
