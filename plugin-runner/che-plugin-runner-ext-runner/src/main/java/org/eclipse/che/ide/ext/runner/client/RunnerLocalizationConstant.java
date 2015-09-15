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
package org.eclipse.che.ide.ext.runner.client;

import com.google.gwt.i18n.client.Messages;
import com.google.inject.Singleton;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Contains all names of graphical elements needed for runner plugin.
 *
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
@Singleton
public interface RunnerLocalizationConstant extends Messages {

    @Key("unknown.error.message")
    String unknownErrorMessage();

    @Key("workspaceGigabyteHoursLimit.error.message")
    String workspaceGigabyteHoursLimitErrorMessage();

    @Key("accountGigabyteHoursLimit.error.message")
    String accountGigabyteHoursLimitErrorMessage();

    String environmentCooking(@NotNull String projectName);

    String applicationStarting(@NotNull String projectName);

    String applicationStopped(@NotNull String projectName);

    String applicationFailed(@NotNull String projectName);

    String applicationCanceled(@NotNull String projectName);

    String applicationMaybeStarted(@NotNull String projectName);

    String applicationStarted(@NotNull String projectName);

    String startApplicationFailed(@NotNull String projectName);

    String applicationLogsFailed();

    @Key("defaultRunnerAbsent")
    String defaultRunnerAbsent();

    @Key("runner.label.application.info")
    String runnerLabelApplicationInfo();

    @Key("runner.label.debug.port")
    String runnerLabelDebugPort();

    @Key("runner.label.timeout.info")
    String runnerLabelTimeoutInfo();

    @Key("messages.overrideMemory")
    String messagesOverrideMemory();

    @Key("messages.overrideLessRequiredMemory")
    String messagesOverrideLessRequiredMemory(@Min(value=0) int overrideRAM, @Min(value=0) int requestedRAM);

    @Key("messages.largeMemoryRequest")
    String messagesLargeMemoryRequest();

    @Key("action.project.running.now")
    String projectRunningNow(@NotNull String project);

    @Key("titles.warning")
    String titlesWarning();

    @Key("runner.tab.console")
    String runnerTabConsole();

    @Key("runner.tab.terminal")
    String runnerTabTerminal();

    @Key("action.run")
    String actionRun();

    @Key("action.run.description")
    String actionRunDescription();

    @Key("get.resources.failed")
    String getResourcesFailed();

    String fullLogTraceConsoleLink();

    @Key("remove.environment")
    String removeEnvironment();

    @Key("remove.environment.message")
    String removeEnvironmentMessage(@NotNull String environmentName);

    @Key("custom.runner.get.environment.failed")
    String customRunnerGetEnvironmentFailed();

    @Key("messages.un.multiple.ram.value")
    String ramSizeMustBeMultipleOf(@Min(value=0) int multiple);

    @Key("messages.incorrect.value")
    String messagesIncorrectValue();

    @Key("messages.total.ram.less.custom")
    String messagesTotalRamLessCustom(@Min(value=0) int totalRam, @Min(value=0) int customRam);

    @Key("messages.available.ram.less.custom")
    String messagesAvailableRamLessCustom(@Min(value=0) int overrideRam, @Min(value=0) int total, @Min(value=0) int used);

    String runnerNotReady();

    @Key("runners.panel.title")
    String runnersPanelTitle();

    @Key("tooltip.header")
    String tooltipHeader();

    @Key("tooltip.body.started")
    String tooltipBodyStarted();

    @Key("tooltip.body.finished")
    String tooltipBodyFinished();

    @Key("tooltip.body.timeout")
    String tooltipBodyTimeout();

    @Key("tooltip.body.time.active")
    String tooltipBodyTimeActive();

    @Key("tooltip.body.ram")
    String tooltipBodyRam();

    @Key("runner.tab.history")
    String runnerTabHistory();

    @Key("runner.tab.properties")
    String runnerTabProperties();

    @Key("runner.tab.templates")
    String runnerTabTemplates();

    @Key("url.app.waiting.for.boot")
    String uplAppWaitingForBoot();

    @Key("url.app.runner.stopped")
    String urlAppRunnerStopped();

    @Key("url.app.running")
    String urlAppRunning();

    @Key("tooltip.runner.panel")
    String tooltipRunnerPanel();

    @Key("template.match.project.type")
    String matchProjectType();

    @Key("properties.config")
    String propertiesConfig();

    @Key("properties.open.config")
    String propertiesOpenConfig();

    @Key("properties.name")
    String propertiesName();

    @Key("properties.ram")
    String propertiesRam();

    @Key("properties.scope")
    String propertiesScope();

    @Key("properties.type")
    String propertiesType();

    @Key("properties.boot")
    String propertiesBoot();

    @Key("properties.shutdown")
    String propertiesShutdown();

    @Key("properties.port.mapping")
    String propertiesPortMapping();

    @Key("properties.port.opened")
    String propertiesPortOpened();

    @Key("properties.port.mappedTo")
    String propertiesPortMappedTo();

    @Key("properties.dockerfile")
    String propertiesDockerfile();

    @Key("properties.button.save")
    String propertiesButtonSave();

    @Key("properties.button.delete")
    String propertiesButtonDelete();

    @Key("properties.button.cancel")
    String propertiesButtonCancel();

    @Key("runner.title")
    String runnerTitle();

    String editorNotReady();

    @Key("tooltip.run.button")
    String tooltipRunButton();

    @Key("tooltip.stop.button")
    String tooltipStopButton();

    @Key("tooltip.docker.button")
    String tooltipDockerButton();

    @Key("console.tooltip.scroll")
    String consoleTooltipScroll();

    @Key("console.tooltip.clear")
    String consoleTooltipClear();

    @Key("action.choose.runner")
    String actionChooseRunner();

    @Key("console.tooltip.wraptext")
    String consoleTooltipWraptext();

    @Key("properties.button.create")
    String propertiesButtonCreate();

    @Key("tooltip.rerun.button")
    String tooltipRerunButton();

    @Key("tooltip.logs.button")
    String tooltipLogsButton();

    @Key("action.run.with")
    String actionRunWith();

    @Key("action.runner.not.specified")
    String actionRunnerNotSpecified();

    @Key("message.runner.shutting.down")
    String messageRunnerShuttingDown();

    @Key("create.custom.runner")
    String createCustomRunner();

    @Key("template.default.runner")
    String templateDefaultRunner();

    @Key("templates.default.runner.stub")
    String templatesDefaultRunnerStub();

    @Key("template.default.more.info")
    String templateDefaultMoreInfo();

    @Key("template.default.project.runner")
    String templateDefaultProjectRunner();

    @Key("messages.availableLessOverrideMemory.title")
    String messagesAvailableLessOverrideMemoryTitle();

    @Key("messages.availableLessOverrideMemory.content")
    String messagesAvailableLessOverrideMemoryContent();

    @Key("messages.availableLessOverrideMemory.settingsLink")
    String messagesAvailableLessOverrideMemorySettingsLink();

    @Key("messages.availableLessOverrideMemory.backToConfig")
    String messagesAvailableLessOverrideMemoryBackToConfig();

    @Key("workspace.preferences.runners.title")
    String workspacePreferencesRunnersTitle();

    @Key("workspace.preferences.title")
    String workspacePreferencesTitle(String workspace);

    @Key("workspace.preferences.runners.shutdown.value.title")
    String workspacePreferencesRunnersShutdownValueTitle();

    @Key("workspace.preferences.runners.shutdown.set.button")
    String workspacePreferencesRunnersShutdownSetButton();

    @Key("terminal.not.ready")
    String terminalNotReady();

    @Key("template.create.new")
    String templateCreateNew();

    @Key("tooltip.header.menu.button")
    String tooltipHeaderMenuButton();

    @Key("menu.toggle.splitter")
    String menuToggleSplitter();

    @Key("message.runner.in.queue")
    String messageRunnerInQueue();

    @Key("no.active.runner")
    String noActiveRunner();

    @Key("clear.inactives")
    String clearInactives();
}