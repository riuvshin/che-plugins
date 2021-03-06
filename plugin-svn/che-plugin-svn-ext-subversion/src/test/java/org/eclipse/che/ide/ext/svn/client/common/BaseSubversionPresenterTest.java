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
package org.eclipse.che.ide.ext.svn.client.common;

import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.svn.client.SubversionClientService;
import org.eclipse.che.ide.ext.svn.client.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.part.explorer.project.NewProjectExplorerPresenter;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

/**
 * Base class for all tests testing extensions of {@link SubversionActionPresenter}
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("NonJREEmulationClassesInClientCode")
public abstract class BaseSubversionPresenterTest {

    public static String PROJECT_NAME = "plugin-svn-test";
    public static String PROJECT_PATH = "/plugin-svn-test";

    @Mock
    protected AppContext                               appContext;
    @Mock
    protected CurrentProject                           currentProject;
    @Mock
    protected DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    @Mock
    protected EventBus                                 eventBus;
    @Mock
    protected NotificationManager                      notificationManager;
    @Mock
    protected ProjectDescriptor                        projectDescriptor;
    @Mock
    protected ProjectDescriptor                        rootProjectDescriptor;
    @Mock
    protected RawOutputPresenter                       rawOutputPresenter;
    @Mock
    protected SubversionClientService                  service;
    @Mock
    protected SubversionExtensionLocalizationConstants constants;
    @Mock
    protected WorkspaceAgent                           workspaceAgent;
    @Mock
    protected NewProjectExplorerPresenter              projectExplorerPart;

    @Before
    public void setUp() throws Exception {
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        when(currentProject.getProjectDescription()).thenReturn(projectDescriptor);
        when(currentProject.getRootProject()).thenReturn(rootProjectDescriptor);

        when(projectDescriptor.getName()).thenReturn(PROJECT_NAME);
        when(projectDescriptor.getPath()).thenReturn(PROJECT_PATH);

        when(rootProjectDescriptor.getName()).thenReturn(PROJECT_NAME);
        when(rootProjectDescriptor.getPath()).thenReturn(PROJECT_PATH);
    }
}
