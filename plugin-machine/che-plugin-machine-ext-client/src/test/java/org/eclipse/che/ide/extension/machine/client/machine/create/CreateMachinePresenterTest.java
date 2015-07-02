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
package org.eclipse.che.ide.extension.machine.client.machine.create;

import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.extension.machine.client.machine.MachineManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** @author Artem Zatsarynny */
@RunWith(MockitoJUnitRunner.class)
public class CreateMachinePresenterTest {

    private final static String RECIPE_URL   = "http://www.host.com/recipe";
    private final static String MACHINE_NAME = "machine";

    @Mock
    private CreateMachineView      view;
    @Mock
    private MachineManager         machineManager;
    @Mock
    private AppContext             appContext;
    @InjectMocks
    private CreateMachinePresenter presenter;
    @Mock(answer = RETURNS_DEEP_STUBS)
    private ProjectDescriptor      projectDescriptor;

    @Before
    public void setUp() {
        when(view.getRecipeURL()).thenReturn(RECIPE_URL);
        when(view.getMachineName()).thenReturn(MACHINE_NAME);
    }

    @Test
    public void shouldSetActionDelegate() {
        verify(view).setDelegate(presenter);
    }

    @Test
    public void shouldShowView() {
        when(projectDescriptor.getRecipe()).thenReturn(RECIPE_URL);
        final CurrentProject currentProject = mock(CurrentProject.class);
        when(currentProject.getRootProject()).thenReturn(projectDescriptor);
        when(appContext.getCurrentProject()).thenReturn(currentProject);

        presenter.showDialog();

        verify(view).show();
        verify(view).setCreateButtonState(eq(false));
        verify(view).setReplaceButtonState(eq(false));
        verify(view).setMachineName(eq(""));
        verify(view).setRecipeURL(eq(""));
        verify(view).setErrorHint(eq(false));
        verify(view).setRecipeURL(eq(RECIPE_URL));
    }

    @Test
    public void buttonsShouldBeDisabledWhenNameIsEmpty() {
        when(view.getMachineName()).thenReturn("");

        presenter.onNameChanged();

        verify(view).setCreateButtonState(eq(false));
        verify(view).setReplaceButtonState(eq(false));
    }

    @Test
    public void buttonsShouldBeEnabledWhenNameIsNotEmpty() {
        presenter.onNameChanged();

        verify(view).setCreateButtonState(eq(true));
        verify(view).setReplaceButtonState(eq(true));
    }

    @Test
    public void shouldCreateMachine() {
        presenter.onCreateClicked();

        verify(view).getRecipeURL();
        verify(view).getMachineName();
        verify(machineManager).startMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
        verify(view).close();
    }

    @Test
    public void shouldReplaceDevMachine() {
        presenter.onReplaceDevMachineClicked();

        verify(view).getMachineName();
        verify(machineManager).startAndBindMachine(eq(RECIPE_URL), eq(MACHINE_NAME));
        verify(view).close();
    }

    @Test
    public void shouldCloseView() {
        presenter.onCancelClicked();

        verify(view).close();
    }
}
