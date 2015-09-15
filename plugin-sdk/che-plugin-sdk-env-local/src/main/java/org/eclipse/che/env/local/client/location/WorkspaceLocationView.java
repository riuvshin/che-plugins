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
package org.eclipse.che.env.local.client.location;

import com.google.inject.ImplementedBy;

import javax.validation.constraints.NotNull;

/**
 *  The visual part of workspace localization window.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(WorkspaceLocationViewImpl.class)
public interface WorkspaceLocationView extends org.eclipse.che.ide.api.mvp.View<WorkspaceLocationView.ActionDelegate> {
    interface ActionDelegate {
    }

    /**
     * Sets location of workspace.
     *
     * @param workspaceLocation
     *         location path
     */
    void setWorkspaceLocation(@NotNull String workspaceLocation);

    /** Show dialog. */
    void showDialog();

    /** Close dialog. */
    void closeDialog();
}
