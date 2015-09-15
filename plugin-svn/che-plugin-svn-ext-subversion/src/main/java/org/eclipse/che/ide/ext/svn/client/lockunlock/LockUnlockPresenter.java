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
package org.eclipse.che.ide.ext.svn.client.lockunlock;

import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.comparator.PathFileComparator;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.ext.svn.client.SubversionClientService;
import org.eclipse.che.ide.ext.svn.client.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.ext.svn.client.action.LockAction;
import org.eclipse.che.ide.ext.svn.client.action.UnlockAction;
import org.eclipse.che.ide.ext.svn.client.common.PathTypeFilter;
import org.eclipse.che.ide.ext.svn.client.common.RawOutputPresenter;
import org.eclipse.che.ide.ext.svn.client.common.SubversionActionPresenter;
import org.eclipse.che.ide.ext.svn.client.common.threechoices.ChoiceDialog;
import org.eclipse.che.ide.ext.svn.client.common.threechoices.ChoiceDialogFactory;
import org.eclipse.che.ide.ext.svn.shared.CLIOutputResponse;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Handler for the {@link LockAction} and {@link UnlockAction} actions.
 */
public class LockUnlockPresenter extends SubversionActionPresenter {

    private final DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private final NotificationManager                      notificationManager;
    private final SubversionClientService                  service;
    private final SubversionExtensionLocalizationConstants constants;

    private final ChoiceDialogFactory choiceDialogFactory;
    private final DialogFactory dialogFactory;

    /**
     * Constructor.
     */
    @Inject
    protected LockUnlockPresenter(final AppContext appContext,
                                  final DialogFactory dialogFactory,
                                  final ChoiceDialogFactory choiceDialogFactory,
                                  final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  final EventBus eventBus,
                                  final NotificationManager notificationManager,
                                  final RawOutputPresenter console,
                                  final SubversionExtensionLocalizationConstants constants,
                                  final SubversionClientService service,
                                  final WorkspaceAgent workspaceAgent,
                                  final ProjectExplorerPart projectExplorerPart) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);

        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.constants = constants;
        this.choiceDialogFactory = choiceDialogFactory;
        this.dialogFactory = dialogFactory;
    }

    public void showLockDialog() {
        showDialog(true);
    }

    public void showUnlockDialog() {
        showDialog(false);
    }

    private void showDialog(final boolean lock) {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        final Collection<PathTypeFilter> filter = new ArrayList<>();
        filter.add(PathTypeFilter.FOLDER);
        filter.add(PathTypeFilter.PROJECT);
        final List<String> selectedFolders = getSelectedPaths(filter);
        if (!selectedFolders.isEmpty()) {
            this.dialogFactory.createMessageDialog(getLockDirectoryTitle(lock),
                                                   getLockDirectoryErrorMessage(lock), null).show();
            return;
        }

        final List<String> selectedPaths = getSelectedPaths(Collections.singletonList(PathTypeFilter.FILE));

        final String withoutForceLabel = getWithoutForceLabel(lock);
        final String withForceLabel = getWithForceLabel(lock);
        final String cancelLabel = getCancelLabel(lock);

        final ConfirmCallback withoutForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, false, selectedPaths);
            }
        };
        final ConfirmCallback withForceCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                doAction(lock, true, selectedPaths);
            }
        };
        final ChoiceDialog dialog = this.choiceDialogFactory.createChoiceDialog(getTitle(lock), getContent(lock),
                                                                                withoutForceLabel, withForceLabel, cancelLabel,
                                                                                withoutForceCallback, withForceCallback,
                                                                                null);
        dialog.show();
    }

    String getTitle(final boolean lock) {
        if (lock) {
            return constants.lockDialogTitle();
        } else {
            return constants.unlockDialogTitle();
        }
    }

    private String getContent(final boolean lock) {
        if (lock) {
            return constants.lockDialogContent();
        } else {
            return constants.unlockDialogContent();
        }
    }

    private String getWithoutForceLabel(final boolean lock) {
        if (lock) {
            return constants.lockButtonWithoutForceLabel();
        } else {
            return constants.unlockButtonWithoutForceLabel();
        }
    }

    private String getWithForceLabel(final boolean lock) {
        if (lock) {
            return constants.lockButtonWithForceLabel();
        } else {
            return constants.unlockButtonWithForceLabel();
        }
    }

    private String getCancelLabel(final boolean lock) {
        return constants.buttonCancel();
    }

    private String getLockDirectoryTitle(final boolean lock) {
        if (lock) {
            return constants.dialogTitleLockDirectory();
        } else {
            return constants.dialogTitleUnlockDirectory();
        }
    }

    private String getLockDirectoryErrorMessage(final boolean lock) {
        if (lock) {
            return constants.errorMessageLockDirectory();
        } else {
            return constants.errorMessageUnlockDirectory();
        }
    }

    private void doAction(final boolean lock, final boolean force, final List<String> paths) {
        if (lock) {
            doLockAction(force, paths);
        } else {
            doUnlockAction(force, paths);
        }
    }

    private void doLockAction(final boolean force, final List<String> paths) {
        final AsyncRequestCallback<CLIOutputResponse> callback = makeCallback(true);
        this.service.lock(getCurrentProjectPath(), paths, force, callback);
    }

    private void doUnlockAction(final boolean force, final List<String> paths) {
        final AsyncRequestCallback<CLIOutputResponse> callback = makeCallback(false);
        this.service.unlock(getCurrentProjectPath(), paths, force, callback);
    }

    private AsyncRequestCallback<CLIOutputResponse> makeCallback(final boolean lock) {
        final Unmarshallable<CLIOutputResponse> unmarshaller = this.dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class);
        return new AsyncRequestCallback<CLIOutputResponse>(unmarshaller) {
            @Override
            protected void onSuccess(final CLIOutputResponse result) {
                final List<String> commandList = new ArrayList<>();
                commandList.add(result.getCommand());
                print(commandList);
                print(result.getOutput());
                printAndSpace(result.getErrOutput());
            }
            @Override
            protected void onFailure(final Throwable exception) {
                handleError(exception);
            }
        };
    }

    private void handleError(@NotNull final Throwable e) {
        String errorMessage;
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            errorMessage = e.getMessage();
        } else {
            errorMessage = constants.commitFailed();
        }
        final Notification notification = new Notification(errorMessage, ERROR);
        this.notificationManager.showNotification(notification);
    }
}
