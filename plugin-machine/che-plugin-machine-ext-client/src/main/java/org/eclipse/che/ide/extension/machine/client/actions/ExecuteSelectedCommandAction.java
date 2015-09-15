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
package org.eclipse.che.ide.extension.machine.client.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandManager;

import javax.validation.constraints.NotNull;
import java.util.Collections;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * Action to execute command which is selected in drop-down list.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class ExecuteSelectedCommandAction extends AbstractPerspectiveAction {

    private final SelectCommandComboBoxAction selectCommandAction;
    private final CommandManager              commandManager;
    private final AnalyticsEventLogger        eventLogger;

    @Inject
    public ExecuteSelectedCommandAction(MachineLocalizationConstant localizationConstant,
                                        MachineResources resources,
                                        SelectCommandComboBoxAction selectCommandAction,
                                        CommandManager commandManager,
                                        AnalyticsEventLogger eventLogger) {
        super(Collections.singletonList(PROJECT_PERSPECTIVE_ID),
              localizationConstant.executeSelectedCommandControlTitle(),
              localizationConstant.executeSelectedCommandControlDescription(),
              null,
              resources.execute());
        this.selectCommandAction = selectCommandAction;
        this.commandManager = commandManager;
        this.eventLogger = eventLogger;
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent e) {
        e.getPresentation().setVisible(selectCommandAction.getSelectedCommand() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        final CommandConfiguration command = selectCommandAction.getSelectedCommand();
        if (command != null) {
            commandManager.execute(command);
        }
    }
}
