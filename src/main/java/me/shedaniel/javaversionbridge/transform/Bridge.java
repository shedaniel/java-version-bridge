/*
 * Copyright (c) 2021 Chocohead, shedaniel
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */

package me.shedaniel.javaversionbridge.transform;

import dev.architectury.transformer.Transformer;

import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface Bridge {
    List<Transformer> transformers(Set<String> flags);
}
