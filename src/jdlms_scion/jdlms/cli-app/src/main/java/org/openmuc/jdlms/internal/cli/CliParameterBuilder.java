/*
 * Copyright 2012-20 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.internal.cli;

import java.util.List;

public class CliParameterBuilder {

    final String name;
    String description;
    boolean optional = true;

    public CliParameterBuilder(String name) {
        this.name = name;
    }

    public CliParameterBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CliParameterBuilder setMandatory() {
        optional = false;
        return this;
    }

    public LongCliParameter buildLongParameter(String parameterName, long defaultValue) {
        return new LongCliParameter(this, parameterName, defaultValue);
    }

    public LongCliParameter buildLongParameter(String parameterName) {
        return new LongCliParameter(this, parameterName);
    }

    public IntCliParameter buildIntParameter(String parameterName, int defaultValue) {
        return new IntCliParameter(this, parameterName, defaultValue);
    }

    public IntCliParameter buildIntParameter(String parameterName) {
        return new IntCliParameter(this, parameterName);
    }

    public StringCliParameter buildStringParameter(String parameterName, String defaultValue) {
        return new StringCliParameter(this, parameterName, defaultValue);
    }

    public StringCliParameter buildStringParameter(String parameterName) {
        return new StringCliParameter(this, parameterName);
    }

    public StringListCliParameter buildStringListParameter(String parameterName) {
        return new StringListCliParameter(this, parameterName);
    }

    public StringListCliParameter buildStringListParameter(String parameterName, List<String> defaultValue) {
        return new StringListCliParameter(this, parameterName, defaultValue);
    }

    public FlagCliParameter buildFlagParameter() {
        return new FlagCliParameter(this);
    }

}
