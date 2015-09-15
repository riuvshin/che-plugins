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
package org.eclipse.che.ide.ext.runner.client.manager;

import com.google.gwt.http.client.URL;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.project.shared.dto.RunnerConfiguration;
import org.eclipse.che.api.project.shared.dto.RunnersDescriptor;
import org.eclipse.che.api.runner.dto.ApplicationProcessDescriptor;
import org.eclipse.che.api.runner.dto.RunOptions;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.action.permits.ActionDenyAccessDialog;
import org.eclipse.che.ide.api.action.permits.ResourcesLockedActionPermit;
import org.eclipse.che.ide.api.action.permits.Run;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.event.ProjectActionEvent;
import org.eclipse.che.ide.api.event.ProjectActionHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.runner.client.RunnerLocalizationConstant;
import org.eclipse.che.ide.ext.runner.client.actions.ChooseRunnerAction;
import org.eclipse.che.ide.ext.runner.client.inject.factories.ModelsFactory;
import org.eclipse.che.ide.ext.runner.client.inject.factories.RunnerActionFactory;
import org.eclipse.che.ide.ext.runner.client.models.Environment;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.models.RunnerCounter;
import org.eclipse.che.ide.ext.runner.client.runneractions.RunnerAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.CheckRamAndRunAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.GetRunningProcessesAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.StopAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.environments.GetSystemEnvironmentsAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.launch.LaunchAction;
import org.eclipse.che.ide.ext.runner.client.selection.Selection;
import org.eclipse.che.ide.ext.runner.client.selection.SelectionManager;
import org.eclipse.che.ide.ext.runner.client.state.PanelState;
import org.eclipse.che.ide.ext.runner.client.state.State;
import org.eclipse.che.ide.ext.runner.client.tabs.common.Tab;
import org.eclipse.che.ide.ext.runner.client.tabs.common.TabBuilder;
import org.eclipse.che.ide.ext.runner.client.tabs.console.container.ConsoleContainer;
import org.eclipse.che.ide.ext.runner.client.tabs.container.PanelLocation;
import org.eclipse.che.ide.ext.runner.client.tabs.container.TabContainer;
import org.eclipse.che.ide.ext.runner.client.tabs.history.HistoryPanel;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.container.PropertiesContainer;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope;
import org.eclipse.che.ide.ext.runner.client.tabs.templates.TemplatesContainer;
import org.eclipse.che.ide.ext.runner.client.tabs.terminal.container.TerminalContainer;
import org.eclipse.che.ide.ext.runner.client.util.EnvironmentIdValidator;
import org.eclipse.che.ide.ext.runner.client.util.RunnerUtil;
import org.eclipse.che.ide.ext.runner.client.util.TimerFactory;
import org.eclipse.che.ide.ext.runner.client.util.annotations.LeftPanel;
import org.eclipse.che.ide.ext.runner.client.util.annotations.LeftPropertiesPanel;
import org.eclipse.che.ide.ext.runner.client.util.annotations.RightPropertiesPanel;

import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.ext.runner.client.constants.TimeInterval.ONE_SEC;
import static org.eclipse.che.ide.ext.runner.client.manager.menu.SplitterState.SPLITTER_OFF;
import static org.eclipse.che.ide.ext.runner.client.manager.menu.SplitterState.SPLITTER_ON;
import static org.eclipse.che.ide.ext.runner.client.models.Runner.Status.IN_QUEUE;
import static org.eclipse.che.ide.ext.runner.client.selection.Selection.RUNNER;
import static org.eclipse.che.ide.ext.runner.client.state.State.RUNNERS;
import static org.eclipse.che.ide.ext.runner.client.state.State.TEMPLATE;
import static org.eclipse.che.ide.ext.runner.client.tabs.common.Tab.VisibleState.REMOVABLE;
import static org.eclipse.che.ide.ext.runner.client.tabs.common.Tab.VisibleState.VISIBLE;
import static org.eclipse.che.ide.ext.runner.client.tabs.container.PanelLocation.RIGHT_PROPERTIES;
import static org.eclipse.che.ide.ext.runner.client.tabs.container.TabContainer.TabSelectHandler;
import static org.eclipse.che.ide.ext.runner.client.tabs.container.tab.TabType.LEFT;
import static org.eclipse.che.ide.ext.runner.client.tabs.container.tab.TabType.RIGHT;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM.DEFAULT;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope.PROJECT;

/**
 * The class provides much business logic:
 * 1. Provides possibility to launch/start a new runner. It means execute request on the server (communication with server part) and change
 * UI part.
 * 2. Manage runners (stop runner, get different information about runner and etc).
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class RunnerManagerPresenter extends BasePresenter implements RunnerManager,
                                                                     RunnerManagerView.ActionDelegate,
                                                                     ProjectActionHandler,
                                                                     SelectionManager.SelectionChangeListener {
    public static final String TIMER_STUB = "--:--:--";

    private static final String PROJECT_PREFIX = "project:/";

    private final RunnerManagerView           view;
    private final DtoFactory                  dtoFactory;
    private final AppContext                  appContext;
    private final ModelsFactory               modelsFactory;
    private final RunnerActionFactory         actionFactory;
    private final GetSystemEnvironmentsAction getSystemEnvironmentsAction;
    private final Map<Runner, RunnerAction>   runnerActions;
    private final Timer                       runnerTimer;
    private final Timer                       runnerInQueueTimer;
    private final RunnerLocalizationConstant  locale;
    private final HistoryPanel                history;
    private final SelectionManager            selectionManager;
    private final TerminalContainer           terminalContainer;
    private final ConsoleContainer            consoleContainer;
    private final PropertiesContainer         propertiesContainer;
    private final TemplatesContainer          templateContainer;
    private final PanelState                  panelState;
    private final RunnerCounter               runnerCounter;
    private final Set<Long>                   runnersId;
    private final RunnerUtil                  runnerUtil;
    private final ResourcesLockedActionPermit runActionPermit;
    private final ActionDenyAccessDialog      runActionDenyAccessDialog;
    private final ChooseRunnerAction          chooseRunnerAction;

    private final TabContainer leftPropertiesContainer;
    private final TabContainer rightPropertiesContainer;
    private final TabContainer leftTabContainer;

    private GetRunningProcessesAction getRunningProcessAction;

    private Runner      selectedRunner;
    private Environment selectedEnvironment;

    private Tab terminalTab;
    private Tab consoleTab;
    private Tab propertiesTab;

    private State state;

    @Inject
    public RunnerManagerPresenter(final RunnerManagerView view,
                                  RunnerActionFactory actionFactory,
                                  ModelsFactory modelsFactory,
                                  AppContext appContext,
                                  DtoFactory dtoFactory,
                                  ChooseRunnerAction chooseRunnerAction,
                                  EventBus eventBus,
                                  final RunnerLocalizationConstant locale,
                                  @LeftPanel TabContainer leftTabContainer,
                                  @LeftPropertiesPanel TabContainer leftPropertiesContainer,
                                  @RightPropertiesPanel TabContainer rightPropertiesContainer,
                                  PanelState panelState,
                                  Provider<TabBuilder> tabBuilderProvider,
                                  final ConsoleContainer consoleContainer,
                                  TerminalContainer terminalContainer,
                                  PropertiesContainer propertiesContainer,
                                  HistoryPanel history,
                                  TemplatesContainer templateContainer,
                                  RunnerCounter runnerCounter,
                                  SelectionManager selectionManager,
                                  TimerFactory timerFactory,
                                  GetSystemEnvironmentsAction getSystemEnvironmentsAction,
                                  RunnerUtil runnerUtil,
                                  @Run ResourcesLockedActionPermit runActionPermit,
                                  @Run ActionDenyAccessDialog runActionDenyAccessDialog) {
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
        this.dtoFactory = dtoFactory;
        this.chooseRunnerAction = chooseRunnerAction;
        this.actionFactory = actionFactory;
        this.modelsFactory = modelsFactory;
        this.appContext = appContext;
        this.runnerCounter = runnerCounter;
        this.getSystemEnvironmentsAction = getSystemEnvironmentsAction;
        this.runnerUtil = runnerUtil;
        this.runActionPermit = runActionPermit;
        this.runActionDenyAccessDialog = runActionDenyAccessDialog;

        this.leftTabContainer = leftTabContainer;
        this.leftTabContainer.setLocation(PanelLocation.LEFT);
        this.leftPropertiesContainer = leftPropertiesContainer;
        this.leftPropertiesContainer.setLocation(PanelLocation.LEFT_PROPERTIES);
        this.rightPropertiesContainer = rightPropertiesContainer;
        this.rightPropertiesContainer.setLocation(RIGHT_PROPERTIES);

        this.selectionManager = selectionManager;
        this.selectionManager.addListener(this);

        this.history = history;

        this.panelState = panelState;

        this.consoleContainer = consoleContainer;
        this.templateContainer = templateContainer;
        this.terminalContainer = terminalContainer;
        this.propertiesContainer = propertiesContainer;

        this.runnerActions = new HashMap<>();

        this.runnerTimer = timerFactory.newInstance(new TimerFactory.TimerCallBack() {
            @Override
            public void onRun() {
                updateRunnerTimer();

                runnerTimer.schedule(ONE_SEC.getValue());
            }
        });

        this.runnerInQueueTimer = timerFactory.newInstance(new TimerFactory.TimerCallBack() {
            @Override
            public void onRun() {
                if (IN_QUEUE.equals(selectedRunner.getStatus())) {
                    consoleContainer.printInfo(selectedRunner, locale.messageRunnerInQueue());
                }
            }
        });

        eventBus.addHandler(ProjectActionEvent.TYPE, this);
        runnersId = new HashSet<>();

        initializeLeftPanel(panelState, tabBuilderProvider, history, templateContainer);

        initializeLeftPropertiesPanel(tabBuilderProvider);
        initializeRightPropertiesPanel(tabBuilderProvider);

        view.setLeftPanel(leftTabContainer);

        panelState.setSplitterState(SPLITTER_OFF);
        view.setGeneralPropertiesPanel(rightPropertiesContainer);
    }

    private void updateRunnerTimer() {
        if (selectedRunner == null) {
            return;
        }
        view.setTimeout(selectedRunner.getTimeout());

        view.updateMoreInfoPopup(selectedRunner);
    }

    private void initializeLeftPanel(@NotNull final PanelState panelState,
                                     @NotNull Provider<TabBuilder> tabBuilderProvider,
                                     @NotNull HistoryPanel historyPanel,
                                     @NotNull final TemplatesContainer templatesContainer) {
        TabSelectHandler historyHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                selectHistoryTab();
            }
        };

        Tab historyTab = tabBuilderProvider.get()
                                           .presenter(historyPanel)
                                           .selectHandler(historyHandler)
                                           .title(locale.runnerTabHistory())
                                           .visible(REMOVABLE)
                                           .scope(EnumSet.allOf(State.class))
                                           .tabType(LEFT)
                                           .build();

        leftTabContainer.addTab(historyTab);

        TabSelectHandler templatesHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                state = TEMPLATE;
                panelState.setState(State.TEMPLATE);

                templatesContainer.selectEnvironment();
                templatesContainer.changeEnableStateRunButton();

                view.hideOtherButtons();
            }
        };

        Tab templateTab = tabBuilderProvider.get()
                                            .presenter(templatesContainer)
                                            .selectHandler(templatesHandler)
                                            .title(locale.runnerTabTemplates())
                                            .visible(REMOVABLE)
                                            .scope(EnumSet.allOf(State.class))
                                            .tabType(LEFT)
                                            .build();

        leftTabContainer.addTab(templateTab);
    }

    private void initializeLeftPropertiesPanel(@NotNull Provider<TabBuilder> tabBuilderProvider) {
        final TabSelectHandler consoleHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                if (selectedRunner != null) {
                    selectedRunner.setActiveTab(locale.runnerTabConsole());
                }
            }
        };

        consoleTab = tabBuilderProvider.get()
                                       .presenter(consoleContainer)
                                       .title(locale.runnerTabConsole())
                                       .visible(REMOVABLE)
                                       .selectHandler(consoleHandler)
                                       .scope(EnumSet.of(RUNNERS))
                                       .tabType(RIGHT)
                                       .build();

        leftPropertiesContainer.addTab(consoleTab);

        TabSelectHandler propertiesHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                if (RUNNERS.equals(panelState.getState())) {
                    propertiesContainer.show(selectedRunner);

                    if (selectedRunner != null) {
                        selectedRunner.setActiveTab(locale.runnerTabProperties());
                    }
                } else {
                    propertiesContainer.show(selectedEnvironment);
                }
            }
        };

        propertiesTab = tabBuilderProvider.get()
                                          .presenter(propertiesContainer)
                                          .selectHandler(propertiesHandler)
                                          .title(locale.runnerTabProperties())
                                          .visible(REMOVABLE)
                                          .scope(EnumSet.allOf(State.class))
                                          .tabType(RIGHT)
                                          .build();

        leftPropertiesContainer.addTab(propertiesTab);
    }

    private void initializeRightPropertiesPanel(@NotNull Provider<TabBuilder> tabBuilderProvider) {
        rightPropertiesContainer.addTab(consoleTab);

        TabSelectHandler terminalHandler = new TabSelectHandler() {
            @Override
            public void onTabSelected() {
                if (selectedRunner != null) {
                    selectedRunner.setActiveTab(locale.runnerTabTerminal());

                    terminalContainer.update(selectedRunner);
                }
            }
        };

        terminalTab = tabBuilderProvider.get()
                                        .presenter(terminalContainer)
                                        .title(locale.runnerTabTerminal())
                                        .visible(VISIBLE)
                                        .selectHandler(terminalHandler)
                                        .scope(EnumSet.of(RUNNERS))
                                        .tabType(RIGHT)
                                        .build();
        rightPropertiesContainer.addTab(terminalTab);

        rightPropertiesContainer.addTab(propertiesTab);

        rightPropertiesContainer.showTabTitle(consoleTab.getTitle(), false);
        rightPropertiesContainer.showTabTitle(propertiesTab.getTitle(), false);
    }

    /** @return the GWT widget that is controlled by the presenter */
    @NotNull
    public RunnerManagerView getView() {
        return view;
    }

    /**
     * Updates runner when runner state changed.
     *
     * @param runner
     *         runner which was changed
     */
    public void update(@NotNull Runner runner) {
        history.update(runner);

        if (runner.equals(selectedRunner) && history.isRunnerExist(runner)) {
            view.update(runner);
            changeURLDependingOnState(runner);
        }
    }

    private void changeURLDependingOnState(@NotNull Runner runner) {
        switch (runner.getStatus()) {
            case IN_PROGRESS:
                view.setApplicationURl(locale.uplAppWaitingForBoot());
                break;
            case IN_QUEUE:
                view.setApplicationURl(locale.uplAppWaitingForBoot());
                break;
            case STOPPED:
                view.setApplicationURl(locale.urlAppRunnerStopped());
                break;
            case FAILED:
                view.setApplicationURl(null);
                break;
            default:
                String url = runner.getApplicationURL();
                view.setApplicationURl(url == null ? locale.urlAppRunning() : url);
                setDebugPort(runner);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onRunButtonClicked() {
        if (State.TEMPLATE.equals(panelState.getState())) {
            RunOptions runOptions = dtoFactory.createDto(RunOptions.class)
                                              .withOptions(selectedEnvironment.getOptions())
                                              .withEnvironmentId(selectedEnvironment.getId())
                                              .withMemorySize(selectedEnvironment.getRam());

            Runner runner = modelsFactory.createRunner(runOptions, selectedEnvironment.getScope(), selectedEnvironment.getName());

            if (PROJECT.equals(selectedEnvironment.getScope())) {
                runner.setScope(PROJECT);
            }

            launchRunner(runner);
        } else {
            launchRunner();
        }

    }

    /** {@inheritDoc} */
    @Override
    public void onRerunButtonClicked() {
        if (runActionPermit.isAllowed()) {

            selectedRunner.setStatus(IN_QUEUE);

            RunnerAction runnerAction = runnerActions.get(selectedRunner);
            if (runnerAction == null || runnerAction instanceof LaunchAction) {
                //Create new CheckRamAndRunAction and update selected runner
                launchRunner(selectedRunner);
            } else {
                runnerAction.perform(selectedRunner);

                update(selectedRunner);
                selectedRunner.resetCreationTime();
            }
        } else {
            runActionDenyAccessDialog.show();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onStopButtonClicked() {
        terminalContainer.removeTerminalUrl(selectedRunner);

        stopRunner(selectedRunner);

        view.updateMoreInfoPopup(selectedRunner);
    }

    /** {@inheritDoc} */
    @Override
    public void onLogsButtonClicked() {
        Link logUrl = selectedRunner.getLogUrl();
        if (logUrl == null) {
            return;
        }

        view.showLog(logUrl.getHref());
    }

    /** {@inheritDoc} */
    @Override
    public void stopRunner(@NotNull Runner runner) {
        RunnerAction runnerAction = runnerActions.get(runner);
        if (runnerAction != null) {
            runnerAction.stop();
        }

        StopAction stopAction = actionFactory.createStop();
        stopAction.perform(runner);
    }

    /** {@inheritDoc} */
    @Override
    public void onMoreInfoBtnMouseOver() {
        view.showMoreInfoPopup(selectedRunner);
    }

    /** {@inheritDoc} */
    @Override
    public void onToggleSplitterClicked(boolean isShowSplitter) {
        terminalTab.setScopes(isShowSplitter ? EnumSet.allOf(State.class) : EnumSet.of(RUNNERS));

        if (isShowSplitter) {
            panelState.setSplitterState(SPLITTER_ON);

            view.setLeftPropertiesPanel(leftPropertiesContainer);
            view.setRightPropertiesPanel(rightPropertiesContainer);
        } else {
            panelState.setSplitterState(SPLITTER_OFF);

            view.setGeneralPropertiesPanel(rightPropertiesContainer);
        }

        if (TEMPLATE.equals(state)) {
            panelState.setState(TEMPLATE);

            leftTabContainer.showTab(locale.runnerTabTemplates());

            if (SPLITTER_OFF.equals(panelState.getSplitterState())) {
                rightPropertiesContainer.showTab(propertiesTab.getTitle());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Runner launchRunner() {
        CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject == null) {
            throw new IllegalStateException("Can't launch runner for current project. Current project is absent...");
        }

        int ram = DEFAULT.getValue();

        RunnersDescriptor runnersDescriptor = currentProject.getProjectDescription().getRunners();
        String defaultRunner = runnersDescriptor.getDefault();

        if (!EnvironmentIdValidator.isValid(defaultRunner)) {
            defaultRunner = URL.encode(defaultRunner);
        }

        RunnerConfiguration defaultConfigs = runnersDescriptor.getConfigs().get(defaultRunner);

        if (defaultRunner != null && defaultConfigs != null) {
            ram = defaultConfigs.getRam();
        }

        RunOptions runOptions = dtoFactory.createDto(RunOptions.class)
                                          .withSkipBuild(Boolean.valueOf(currentProject.getAttributeValue("runner:skipBuild")))
                                          .withEnvironmentId(defaultRunner)
                                          .withMemorySize(ram);

        Environment environment = chooseRunnerAction.selectEnvironment();
        if (environment != null) {
            if (defaultRunner != null && defaultRunner.equals(environment.getId())) {
                Runner runner = modelsFactory.createRunner(runOptions);
                if (defaultRunner.startsWith(PROJECT_PREFIX)) {
                    runner.setScope(PROJECT);
                }
                return launchRunner(runner);
            }
            runOptions = runOptions.withOptions(environment.getOptions())
                                   .withMemorySize(environment.getRam())
                                   .withEnvironmentId(environment.getId());
            Runner runner = modelsFactory.createRunner(runOptions, environment.getScope(), environment.getName());
            if (environment.getId().startsWith(PROJECT_PREFIX)) {
                runner.setScope(PROJECT);
            }
            return launchRunner(runner);
        }

        Runner runner = modelsFactory.createRunner(runOptions);
        if (defaultRunner != null && defaultRunner.startsWith(PROJECT_PREFIX)) {
            runner.setScope(PROJECT);
        }
        return launchRunner(modelsFactory.createRunner(runOptions));
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Runner launchRunner(@NotNull RunOptions runOptions) {
        return launchRunner(modelsFactory.createRunner(runOptions));
    }

    /** {@inheritDoc} */
    @Override
    @NotNull
    public Runner launchRunner(@NotNull RunOptions runOptions, @NotNull Scope scope, @NotNull String environmentName) {
        return launchRunner(modelsFactory.createRunner(runOptions, scope, environmentName));
    }

    @NotNull
    private Runner launchRunner(@NotNull Runner runner) {
        if (runActionPermit.isAllowed()) {

            CurrentProject currentProject = appContext.getCurrentProject();

            if (currentProject == null) {
                throw new IllegalStateException("Can't launch runner for current project. Current project is absent...");
            }

            selectedEnvironment = null;

            panelState.setState(RUNNERS);
            view.showOtherButtons();

            history.addRunner(runner);

            runnerInQueueTimer.schedule(ONE_SEC.getValue());

            CheckRamAndRunAction checkRamAndRunAction = actionFactory.createCheckRamAndRun();
            checkRamAndRunAction.perform(runner);

            runnerActions.put(runner, checkRamAndRunAction);

            runner.resetCreationTime();
            runnerTimer.schedule(ONE_SEC.getValue());
        } else {
            runActionDenyAccessDialog.show();
        }
        return runner;
    }

    /** {@inheritDoc} */
    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** Sets active runner panel when runner is started */
    public void setActive() {
        PartPresenter activePart = partStack.getActivePart();
        if (!this.equals(activePart)) {
            partStack.setActivePart(this);
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return locale.runnerTitle();
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Nullable
    @Override
    public String getTitleToolTip() {
        return locale.tooltipRunnerPanel();
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectReady(@NotNull ProjectActionEvent projectActionEvent) {
        view.setEnableReRunButton(false);
        view.setEnableStopButton(false);
        view.setEnableLogsButton(false);

        templateContainer.setVisible(true);

        getRunningProcessAction = actionFactory.createGetRunningProcess();

        boolean isRunOperationAvailable = runnerUtil.hasRunPermission();
        view.setEnableRunButton(isRunOperationAvailable);

        if (!isRunOperationAvailable) {
            return;
        }

        templateContainer.showEnvironments();

        getRunningProcessAction.perform();
        getSystemEnvironmentsAction.perform();

        runnerTimer.schedule(ONE_SEC.getValue());
    }

    @Override
    public void onProjectClosing(ProjectActionEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public void onProjectClosed(@NotNull ProjectActionEvent projectActionEvent) {
        partStack.hidePart(this);

        selectionManager.setRunner(null);

        templateContainer.setVisible(false);

        view.setEnableRunButton(false);
        view.setEnableReRunButton(false);
        view.setEnableStopButton(false);
        view.setEnableLogsButton(false);

        view.setApplicationURl(null);
        view.setDebugPort(null);
        view.setTimeout(TIMER_STUB);

        history.clear();
        runnerActions.clear();

        runnerCounter.reset();
        terminalContainer.reset();
        consoleContainer.reset();
        propertiesContainer.reset();

        getRunningProcessAction.stop();
        propertiesContainer.show((Runner)null);
    }

    @Override
    public void onProjectOpened(ProjectActionEvent event) {

    }

    /**
     * Adds already running runner.
     *
     * @param processDescriptor
     *         The descriptor of new runner
     * @return instance of new runner
     */
    @NotNull
    public Runner addRunner(@NotNull ApplicationProcessDescriptor processDescriptor) {
        RunOptions runOptions = dtoFactory.createDto(RunOptions.class);
        Runner runner = modelsFactory.createRunner(runOptions);

        String environmentId = processDescriptor.getEnvironmentId();

        if (environmentId != null && environmentId.startsWith(PROJECT_PREFIX)) {
            runner.setScope(PROJECT);
        }

        runnersId.add(processDescriptor.getProcessId());

        runner.setProcessDescriptor(processDescriptor);
        runner.setRAM(processDescriptor.getMemorySize());
        runner.setStatus(Runner.Status.DONE);
        runner.resetCreationTime();

        history.addRunner(runner);

        onSelectionChanged(RUNNER);

        runnerTimer.schedule(ONE_SEC.getValue());

        LaunchAction launchAction = actionFactory.createLaunch();
        runnerActions.put(runner, launchAction);

        launchAction.perform(runner);

        selectHistoryTab();

        return runner;
    }

    /**
     * Adds id of new running runner.
     *
     * @param runnerId
     *         process id of runner
     */
    public void addRunnerId(@NotNull Long runnerId) {
        runnersId.add(runnerId);
    }

    /**
     * Returns <code>true</code> if runner with current ID was already create, <code>false</code> runner does not exist* *
     *
     * @param runnerId
     *         ID of runner
     */
    public boolean isRunnerExist(@NotNull Long runnerId) {
        return runnersId.contains(runnerId);
    }

    /** {@inheritDoc} */
    @Override
    public void onSelectionChanged(@NotNull Selection selection) {
        if (RUNNER.equals(selection)) {
            runnerSelected();
        } else {
            environmentSelected();
        }
    }

    private void runnerSelected() {
        selectedRunner = selectionManager.getRunner();
        if (selectedRunner == null) {
            showNoRunnerMessage(true);

            propertiesContainer.reset();
            return;
        }

        showNoRunnerMessage(false);

        if (SPLITTER_OFF.equals(panelState.getSplitterState())) {
            rightPropertiesContainer.showTab(selectedRunner.getActiveTab());
        }

        history.selectRunner(selectedRunner);

        if (locale.runnerTabTerminal().equals(selectedRunner.getActiveTab()) || SPLITTER_ON.equals(panelState.getSplitterState())) {
            terminalContainer.update(selectedRunner);
        }

        update(selectedRunner);

        updateRunnerTimer();
    }

    private void showNoRunnerMessage(boolean isVisible) {
        terminalContainer.setVisibleNoRunnerLabel(isVisible);
        consoleContainer.setVisibleNoRunnerLabel(isVisible);
        propertiesContainer.setVisibleNoRunnerLabel(isVisible);
    }

    private void environmentSelected() {
        selectedEnvironment = selectionManager.getEnvironment();
        if (selectedEnvironment == null) {
            return;
        }

        templateContainer.select(selectedEnvironment);
    }

    private void selectHistoryTab() {
        state = RUNNERS;
        panelState.setState(RUNNERS);

        view.setEnableRunButton(runnerUtil.hasRunPermission());

        view.showOtherButtons();
    }

    private void setDebugPort(Runner runner) {
        ApplicationProcessDescriptor runnerDescriptor = runner.getDescriptor();
        if (runnerDescriptor != null && runnerDescriptor.getDebugPort() != -1) {
            view.setDebugPort(String.valueOf(runnerDescriptor.getDebugPort()));
        }
    }
}