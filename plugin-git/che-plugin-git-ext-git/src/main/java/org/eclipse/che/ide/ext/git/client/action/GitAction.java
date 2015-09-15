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
package org.eclipse.che.ide.ext.git.client.action;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static org.eclipse.che.ide.workspace.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

/**
 * @author Roman Nikitenko
 * @author Dmitry Shnurenko
 */
public abstract class GitAction extends AbstractPerspectiveAction {

    protected final AppContext     appContext;
    protected       SelectionAgent selectionAgent;

    public GitAction(String text, String description, SVGResource svgIcon, AppContext appContext,
                     SelectionAgent selectionAgent) {
        super(Arrays.asList(PROJECT_PERSPECTIVE_ID), text, description, null, svgIcon);
        this.appContext = appContext;
        this.selectionAgent = selectionAgent;
    }

    protected boolean isGitRepository() {
        boolean isGitRepository = false;

        if (getActiveProject() != null) {
            ProjectDescriptor rootProjectDescriptor = getActiveProject().getRootProject();
            List<String> listVcsProvider = rootProjectDescriptor.getAttributes().get("vcs.provider.name");

            if (listVcsProvider != null && (!listVcsProvider.isEmpty()) && listVcsProvider.contains("git")) {
                isGitRepository = true;
            }
        }
        return isGitRepository;
    }

    protected CurrentProject getActiveProject() {
        return appContext.getCurrentProject();
    }

    @Override
    public void updateInPerspective(@NotNull ActionEvent event) {
        event.getPresentation().setVisible(getActiveProject() != null);
        event.getPresentation().setEnabled(isGitRepository());
    }
}
