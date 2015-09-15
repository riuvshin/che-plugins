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
package org.eclipse.che.ide.extension.maven.client.command;

import org.eclipse.che.api.machine.shared.dto.CommandDescriptor;
import org.eclipse.che.ide.CommandLine;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;

import javax.validation.constraints.NotNull;

/**
 * Factory for {@link MavenCommandConfiguration} instances.
 *
 * @author Artem Zatsarynnyy
 */
public class MavenCommandConfigurationFactory extends CommandConfigurationFactory<MavenCommandConfiguration> {

    protected MavenCommandConfigurationFactory(@NotNull CommandType commandType) {
        super(commandType);
    }

    private static boolean isMavenCommand(String commandLine) {
        return commandLine.startsWith("mvn");
    }

    @NotNull
    @Override
    public MavenCommandConfiguration createFromCommandDescriptor(@NotNull CommandDescriptor descriptor) {
        if (!isMavenCommand(descriptor.getCommandLine())) {
            throw new IllegalArgumentException("Not a valid Maven command: " + descriptor.getCommandLine());
        }

        final MavenCommandConfiguration configuration = new MavenCommandConfiguration(descriptor.getId(),
                                                                                      getCommandType(),
                                                                                      descriptor.getName());

        final CommandLine cmd = new CommandLine(descriptor.getCommandLine());

        if (cmd.hasArgument("-f")) {
            final int index = cmd.indexOf("-f");
            final String workDir = cmd.getArgument(index + 1);
            configuration.setWorkingDirectory(workDir);

            cmd.removeArgument("-f");
            cmd.removeArgument(workDir);
        }

        cmd.removeArgument("mvn");
        configuration.setCommandLine(cmd.toString());

        return configuration;
    }
}
