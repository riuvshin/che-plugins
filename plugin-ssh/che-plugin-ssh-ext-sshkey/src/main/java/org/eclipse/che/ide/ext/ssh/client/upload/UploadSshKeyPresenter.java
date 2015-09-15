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
package org.eclipse.che.ide.ext.ssh.client.upload;

import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ConsolePart;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.rest.RestContext;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.web.bindery.event.shared.EventBus;

import javax.validation.constraints.NotNull;

/**
 * Main appointment of this class is upload private SSH key to the server.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class UploadSshKeyPresenter implements UploadSshKeyView.ActionDelegate {
    private UploadSshKeyView        view;
    private String                  workspaceId;
    private SshLocalizationConstant constant;
    private String                  restContext;
    private EventBus                eventBus;
    private ConsolePart             console;
    private NotificationManager     notificationManager;
    private AsyncCallback<Void>     callback;

    @Inject
    public UploadSshKeyPresenter(UploadSshKeyView view,
                                 SshLocalizationConstant constant,
                                 @RestContext String restContext,
                                 @Named("workspaceId") String workspaceId,
                                 EventBus eventBus,
                                 ConsolePart console,
                                 NotificationManager notificationManager) {
        this.view = view;
        this.workspaceId = workspaceId;
        this.view.setDelegate(this);
        this.constant = constant;
        this.restContext = restContext;
        this.console = console;
        this.eventBus = eventBus;
        this.notificationManager = notificationManager;
    }

    /** Show dialog. */
    public void showDialog(@NotNull AsyncCallback<Void> callback) {
        this.callback = callback;
        view.setMessage("");
        view.setHost("");
        view.setEnabledUploadButton(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onUploadClicked() {
        String host = view.getHost();
        if (host.isEmpty()) {
            view.setMessage(constant.hostValidationError());
            return;
        }
        view.setEncoding(FormPanel.ENCODING_MULTIPART);
        view.setAction(restContext + "/ssh-keys/" + workspaceId + "/add?host=" + host);
        view.submit();
    }

    /** {@inheritDoc} */
    @Override
    public void onSubmitComplete(@NotNull String result) {
        if (result.isEmpty()) {
            view.close();
            callback.onSuccess(null);
        } else {
            if (result.startsWith("<pre>") && result.endsWith("</pre>")) {
                result = result.substring(5, (result.length() - 6));
            }
            notificationManager.showError(result);
            callback.onFailure(new Throwable(result));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onFileNameChanged() {
        String fileName = view.getFileName();
        view.setEnabledUploadButton(!fileName.isEmpty());
    }
}