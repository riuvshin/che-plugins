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
package org.eclipse.che.ide.ext.runner.client.tabs.console.container;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.tabs.common.TabPresenter;

import javax.validation.constraints.NotNull;

/**
 * The common representation of console container widget. This widget provides an ability to manager many console widgets for every runner.
 *
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@ImplementedBy(ConsoleContainerPresenter.class)
public interface ConsoleContainer extends TabPresenter {

    /**
     * Prints a given message with unknown content in the console for a given runner.
     *
     * @param runner
     *         runner that needs to contain a given line
     * @param message
     *         message that needs to be printed
     */
    void print(@NotNull Runner runner, @NotNull String message);

    /**
     * Prints a given message with info content in the console for a given runner.
     * Printed message will look like this: [INFO] some string
     *
     * @param runner
     *         runner that needs to contain a given line
     * @param message
     *         message that needs to be printed
     */
    void printInfo(@NotNull Runner runner, @NotNull String message);

    /**
     * Prints a given message with error content in the console for a given runner.
     * Printed message will look like this: [ERROR] some string
     *
     * @param runner
     *         runner that needs to contain a given line
     * @param message
     *         message that needs to be printed
     */
    void printError(@NotNull Runner runner, @NotNull String message);

    /**
     * Prints a given message with warning content in the console for a given runner.
     * Printed message will look like this: [WARNING] some string
     *
     * @param runner
     *         runner that needs to contain a given line
     * @param message
     *         message that needs to be printed
     */
    void printWarn(@NotNull Runner runner, @NotNull String message);

    /** Cleans the data of the console widgets. */
    void reset();

    /** Deletes console by Runner. */
    void deleteConsoleByRunner(@NotNull Runner runner);

    /**
     * Changes visibility of the no runner label.
     *
     * @param visible
     *         visible state that needs to be applied
     */
    void setVisibleNoRunnerLabel(boolean visible);

}