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

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.runner.client.inject.factories.WidgetFactory;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.selection.Selection;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.runner.client.tabs.console.panel.Console;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
@Singleton
public class ConsoleContainerPresenter implements ConsoleContainer,
                                                  ConsoleContainerView.ActionDelegate,
                                                  SelectionManager.SelectionChangeListener {

    private final ConsoleContainerView view;
    private final SelectionManager     selectionManager;
    private final WidgetFactory        widgetFactory;
    private final Map<Runner, Console> consoles;

    private Console selectedConsole;

    @Inject
    public ConsoleContainerPresenter(ConsoleContainerView view, WidgetFactory widgetFactory, SelectionManager selectionManager) {
        this.view = view;
        this.view.setDelegate(this);
        this.widgetFactory = widgetFactory;
        this.selectionManager = selectionManager;

        this.selectionManager.addListener(this);

        consoles = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public void print(@NotNull Runner runner, @NotNull String message) {
        Console console = getConsoleOrCreate(runner);
        console.print(message);
    }

    /** {@inheritDoc} */
    @Override
    public void printInfo(@NotNull Runner runner, @NotNull String message) {
        Console console = getConsoleOrCreate(runner);
        console.printInfo(message);
    }

    /** {@inheritDoc} */
    @Override
    public void printError(@NotNull Runner runner, @NotNull String message) {
        Console console = getConsoleOrCreate(runner);
        console.printError(message);
    }

    /** {@inheritDoc} */
    @Override
    public void printWarn(@NotNull Runner runner, @NotNull String message) {
        Console console = getConsoleOrCreate(runner);
        console.printWarn(message);
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        for (Console console : consoles.values()) {
            view.removeWidget(console);
        }

        consoles.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectionChanged(@NotNull Selection selection) {
        if (Selection.ENVIRONMENT.equals(selection)) {
            return;
        }

        Runner runner = selectionManager.getRunner();
        if (runner == null) {
            return;
        }

        selectedConsole = getConsoleOrCreate(runner);
        view.showWidget(selectedConsole);
    }

    @NotNull
    private Console getConsoleOrCreate(@NotNull Runner runner) {
        Console result = consoles.get(runner);
        if (result == null) {
            result = widgetFactory.createConsole(runner);
            consoles.put(runner, result);
        }

        return result;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onScrollBottomClicked() {
        if (selectedConsole == null) {
            return;
        }

        selectedConsole.scrollBottom();
    }

    /** {@inheritDoc} */
    @Override
    public void onWrapTextClicked() {
        if (selectedConsole == null) {
            return;
        }

        selectedConsole.changeWrapTextParam();
        view.selectWrapTextButton(selectedConsole.isWrapText());
    }

    /** {@inheritDoc} */
    @Override
    public void onCleanClicked() {
        if (selectedConsole == null) {
            return;
        }

        selectedConsole.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void deleteConsoleByRunner(@NotNull Runner runner) {
        Console console = consoles.get(runner);

        view.removeWidget(console);
        consoles.remove(runner);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleNoRunnerLabel(boolean visible) {
        view.setVisibleNoRunnerLabel(visible);
    }
}