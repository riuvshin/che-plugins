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
package org.eclipse.che.ide.ext.git.client.delete;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.part.explorer.project.NewProjectExplorerPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

/**
 * Delete repository command handler, performs deleting Git repository.
 *
 * @author Ann Zhuleva
 */
@Singleton
public class DeleteRepositoryPresenter {
    private GitServiceClient        service;
    private GitLocalizationConstant constant;
    private AppContext              appContext;
    private NotificationManager     notificationManager;
    private final NewProjectExplorerPresenter projectExplorer;

    /**
     * Create presenter.
     *
     * @param service
     * @param constant
     * @param appContext
     * @param notificationManager
     */
    @Inject
    public DeleteRepositoryPresenter(GitServiceClient service,
                                     GitLocalizationConstant constant,
                                     AppContext appContext,
                                     NotificationManager notificationManager,
                                     NewProjectExplorerPresenter projectExplorer) {
        this.service = service;
        this.constant = constant;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.projectExplorer = projectExplorer;
    }

    /** Delete Git repository. */
    public void deleteRepository() {
        final CurrentProject project = appContext.getCurrentProject();
        service.deleteRepository(project.getRootProject(), new AsyncRequestCallback<Void>() {
            @Override
            protected void onSuccess(Void result) {
                project.getRootProject().getAttributes().get("vcs.provider.name").clear();

                notificationManager.showInfo(constant.deleteGitRepositorySuccess());
                //it's need for hide .git in project tree
                projectExplorer.reloadChildren();
            }

            @Override
            protected void onFailure(Throwable exception) {
                notificationManager.showError(exception.getMessage());
            }
        });
    }
}