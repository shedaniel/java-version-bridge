/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge;

import dev.architectury.transformer.Transformer;
import me.shedaniel.javaversionbridge.transform.Bridge;
import me.shedaniel.javaversionbridge.transform.BridgeRegistry;
import me.shedaniel.javaversionbridge.transform.bridge.EditVersionTransformer;
import org.gradle.api.JavaVersion;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BridgeTransformingTask extends TransformingTask {
    private final Property<JavaVersion> fromVersion = getProject().getObjects().property(JavaVersion.class);
    private final Property<JavaVersion> toVersion = getProject().getObjects().property(JavaVersion.class);
    private final SetProperty<String> flags = getProject().getObjects().setProperty(String.class).empty();
    
    @Input
    public Property<JavaVersion> getFromVersion() {
        return fromVersion;
    }
    
    @Input
    public Property<JavaVersion> getToVersion() {
        return toVersion;
    }
    
    @Input
    public SetProperty<String> getFlags() {
        return flags;
    }
    
    @Override
    @Internal
    protected List<Transformer> getTransformers() {
        List<Transformer> transformer = new ArrayList<>();
        transformer.add(new EditVersionTransformer(toVersion.get()));
        for (Map.Entry<JavaVersion, Bridge> entry : BridgeRegistry.bridges.entrySet()) {
            if (entry.getKey().compareTo(toVersion.get()) > 0 & entry.getKey().compareTo(fromVersion.get()) <= 0) {
                transformer.addAll(entry.getValue().transformers(flags.get()));
            }
        }
        return transformer;
    }
}
