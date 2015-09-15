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
package org.eclipse.che.ide.ext.git.client.init;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer;
import org.eclipse.che.ide.part.explorer.project.NewProjectExplorerPresenter;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

/**
 * Presenter for Git command Init Repository.
 *
 * @author Ann Zhuleva
 * @author Roman Nikitenko
 */
@Singleton
public class InitRepositoryPresenter {
    private final GitRepositoryInitializer gitRepositoryInitializer;
    private final NewProjectExplorerPresenter projectExplorer;
    private final AppContext              appContext;
    private final GitLocalizationConstant constant;
    private final NotificationManager     notificationManager;

    @Inject
    public InitRepositoryPresenter(AppContext appContext,
                                   GitLocalizationConstant constant,
                                   NotificationManager notificationManager,
                                   GitRepositoryInitializer gitRepositoryInitializer,
                                   NewProjectExplorerPresenter projectExplorer) {
        this.appContext = appContext;
        this.constant = constant;
        this.notificationManager = notificationManager;
        this.gitRepositoryInitializer = gitRepositoryInitializer;
        this.projectExplorer = projectExplorer;
    }

    public void initRepository() {
        final CurrentProject currentProject = appContext.getCurrentProject();

        if (currentProject == null || currentProject.getRootProject() == null) {
            Log.error(getClass(), "Open the project before initialize repository");
            return;
        }

        gitRepositoryInitializer.initGitRepository(currentProject.getRootProject(), new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                handleError(caught);
            }

            @Override
            public void onSuccess(Void result) {
                notificationManager.showInfo(constant.initSuccess());
                //it's need for show .git in project tree
                projectExplorer.reloadChildren();
            }
        });
    }

    /**
     * Handler some action whether some exception happened.
     *
     * @param e
     *         exception what happened
     */
    private void handleError(@NotNull Throwable e) {
        String errorMessage = (e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : constant.initFailed();
        notificationManager.showError(errorMessage);
    }
}
