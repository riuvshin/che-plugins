/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.extension.machine.client.command.arbitrary;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;

/**
 * Represents command that is defined by arbitrary command line.
 *
 * @author Artem Zatsarynnyy
 */
public class ArbitraryCommandConfiguration extends CommandConfiguration {

    private String commandLine;

    protected ArbitraryCommandConfiguration(String id, CommandType type, String name) {
        super(id, type, name);
        commandLine = "";
    }

    public String getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    @NotNull
    @Override
    public String toCommandLine() {
        return getCommandLine();
    }
}
