/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge;

import dev.architectury.transformer.Transform;
import dev.architectury.transformer.Transformer;
import dev.architectury.transformer.input.OpenedFileAccess;
import dev.architectury.transformer.transformers.BuiltinProperties;
import dev.architectury.transformer.transformers.ClasspathProvider;
import dev.architectury.transformer.transformers.base.edit.SimpleTransformerContext;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.WorkResults;
import org.gradle.api.tasks.bundling.Jar;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TransformingTask extends Jar {
    private final ConfigurableFileCollection classpath = getProject().getObjects().fileCollection();
    
    @InputFiles
    public ConfigurableFileCollection getClasspath() {
        return classpath;
    }
    
    @Override
    protected CopyAction createCopyAction() {
        CopyAction action = super.createCopyAction();
        return stream -> {
            System.setProperty(BuiltinProperties.LOCATION, getProject().getGradle().getRootProject().getBuildDir().toPath().resolve("java-version-bridge").toAbsolutePath().toString());
            action.execute(stream);
            try (OpenedFileAccess outputInterface = OpenedFileAccess.ofJar(getArchiveFile().get().getAsFile().toPath())) {
                Transform.runTransformers(new SimpleTransformerContext($ -> {}, true, false, true),
                        ClasspathProvider.of(classpath.getFiles().stream().map(File::toPath).collect(Collectors.toList())),
                        getArchiveFileName().get(), outputInterface, getTransformers());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
            
            return WorkResults.didWork(true);
        };
    }
    
    protected abstract List<Transformer> getTransformers();
}
