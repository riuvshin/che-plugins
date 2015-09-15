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
package org.eclipse.che.ide.extension.machine.client.command.edit;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import org.eclipse.che.commons.annotation.Nullable;
import java.util.Collection;

/**
 * The view of {@link EditCommandsPresenter}.
 *
 * @author Artem Zatsarynnyy
 */
public interface EditCommandsView extends View<EditCommandsView.ActionDelegate> {

    /** Show view. */
    void show();

    /** Close view. */
    void close();

    /** Returns the component used for command configurations display. */
    AcceptsOneWidget getCommandConfigurationsDisplayContainer();

    void clearCommandConfigurationsDisplayContainer();

    /**
     * Sets command types and command configurations to display.
     *
     * @param commandTypes
     *         available types of commands
     * @param commandConfigurations
     *         existed commands
     */
    void setData(Collection<CommandType> commandTypes, Collection<CommandConfiguration> commandConfigurations);

    /** Returns command name. */
    String getConfigurationName();

    /** Sets command name. */
    void setConfigurationName(String name);

    /** Sets enabled state of the 'Add' button. */
    void setAddButtonState(boolean enabled);

    /** Sets enabled state of the 'Remove' button. */
    void setRemoveButtonState(boolean enabled);

    /** Sets enabled state of the 'Execute' button. */
    void setExecuteButtonState(boolean enabled);

    /** Sets enabled state of the 'Cancel' button. */
    void setCancelButtonState(boolean enabled);

    /** Sets enabled state of the 'Apply' button. */
    void setApplyButtonState(boolean enabled);

    /** Returns the selected command type or type of the selected command configuration. */
    @Nullable
    CommandType getSelectedCommandType();

    /** Select command with the given ID. */
    void selectCommand(String commandId);

    /** Returns the selected command configuration. */
    @Nullable
    CommandConfiguration getSelectedConfiguration();

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when 'Ok' button is clicked. */
        void onOkClicked();

        /** Called when 'Apply' button is clicked. */
        void onApplyClicked();

        /** Called when 'Cancel' button is clicked. */
        void onCancelClicked();

        /** Called when 'Add' button is clicked. */
        void onAddClicked();

        /** Called when 'Remove' button is clicked. */
        void onRemoveClicked();

        /** Called when 'Execute' button is clicked. */
        void onExecuteClicked();

        /**
         * Called when some command type is selected.
         *
         * @param type
         *         selected command type
         */
        void onCommandTypeSelected(CommandType type);

        /**
         * Called when some command configuration is selected.
         *
         * @param configuration
         *         selected command configuration
         */
        void onConfigurationSelected(CommandConfiguration configuration);

        /** Called when configuration name is changed. */
        void onNameChanged();
    }
}
