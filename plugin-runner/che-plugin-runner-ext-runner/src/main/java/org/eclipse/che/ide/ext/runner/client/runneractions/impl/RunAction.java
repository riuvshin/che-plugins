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
package org.eclipse.che.ide.ext.runner.client.runneractions.impl;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.runner.dto.ApplicationProcessDescriptor;
import org.eclipse.che.api.runner.gwt.client.RunnerServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.ext.runner.client.RunnerLocalizationConstant;
import org.eclipse.che.ide.ext.runner.client.callbacks.AsyncCallbackBuilder;
import org.eclipse.che.ide.ext.runner.client.callbacks.FailureCallback;
import org.eclipse.che.ide.ext.runner.client.callbacks.SuccessCallback;
import org.eclipse.che.ide.ext.runner.client.inject.factories.RunnerActionFactory;
import org.eclipse.che.ide.ext.runner.client.manager.RunnerManagerPresenter;
import org.eclipse.che.ide.ext.runner.client.models.Runner;
import org.eclipse.che.ide.ext.runner.client.runneractions.AbstractRunnerAction;
import org.eclipse.che.ide.ext.runner.client.runneractions.impl.launch.LaunchAction;
import org.eclipse.che.ide.ext.runner.client.util.EnvironmentIdValidator;
import org.eclipse.che.ide.ext.runner.client.util.RunnerUtil;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;

/**
 * This action executes a request on the server side for running a runner. Then it adds handlers for listening WebSocket messages from
 * different events from the server.
 *
 * @author Roman Nikitenko
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
public class RunAction extends AbstractRunnerAction {

    private final RunnerServiceClient                                          service;
    private final AppContext                                                   appContext;
    private final RunnerLocalizationConstant                                   locale;
    private final RunnerManagerPresenter                                       presenter;
    private final Provider<AsyncCallbackBuilder<ApplicationProcessDescriptor>> callbackBuilderProvider;
    private final RunnerUtil                                                   runnerUtil;
    private final LaunchAction                                                 launchAction;
    private final AnalyticsEventLogger                                         eventLogger;

    @Inject
    public RunAction(RunnerServiceClient service,
                     AppContext appContext,
                     RunnerLocalizationConstant locale,
                     RunnerManagerPresenter presenter,
                     Provider<AsyncCallbackBuilder<ApplicationProcessDescriptor>> callbackBuilderProvider,
                     RunnerUtil runnerUtil,
                     RunnerActionFactory actionFactory,
                     AnalyticsEventLogger eventLogger) {
        this.service = service;
        this.appContext = appContext;
        this.locale = locale;
        this.presenter = presenter;
        this.callbackBuilderProvider = callbackBuilderProvider;
        this.runnerUtil = runnerUtil;
        this.eventLogger = eventLogger;
        this.launchAction = actionFactory.createLaunch();

        addAction(launchAction);
    }

    /** {@inheritDoc} */
    @Override
    public void perform(@NotNull final Runner runner) {
        eventLogger.log(this);
        final CurrentProject project = appContext.getCurrentProject();
        if (project == null) {
            return;
        }

        presenter.setActive();

        AsyncRequestCallback<ApplicationProcessDescriptor> callback = callbackBuilderProvider
                .get()
                .unmarshaller(ApplicationProcessDescriptor.class)
                .success(new SuccessCallback<ApplicationProcessDescriptor>() {
                    @Override
                    public void onSuccess(ApplicationProcessDescriptor descriptor) {
                        runner.setProcessDescriptor(descriptor);
                        runner.setRAM(descriptor.getMemorySize());

                        presenter.addRunnerId(descriptor.getProcessId());

                        presenter.update(runner);

                        launchAction.perform(runner);
                    }
                })
                .failure(new FailureCallback() {
                    @Override
                    public void onFailure(@NotNull Throwable reason) {

                        if (project.getRunner() == null) {
                            runnerUtil.showError(runner, locale.defaultRunnerAbsent(), null);
                            return;
                        }

                        runnerUtil.showError(runner, locale.startApplicationFailed(project.getProjectDescription().getName()), null);
                    }
                })
                .build();

        String encodedEnvironmentId = runner.getOptions().getEnvironmentId();
        if (encodedEnvironmentId != null && !EnvironmentIdValidator.isValid(encodedEnvironmentId)) {
            runner.getOptions().setEnvironmentId(URL.encode(encodedEnvironmentId));
        }

        service.run(project.getProjectDescription().getPath(), runner.getOptions(), callback);
    }

}