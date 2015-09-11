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
package org.eclipse.che.ide.extension.machine.client.actions;

import elemental.client.Browser;

import com.google.common.base.Strings;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.rest.RestContext;
import org.eclipse.che.ide.util.loging.Log;

import javax.annotation.Nonnull;
import java.util.Collections;

import static org.eclipse.che.ide.extension.machine.client.perspective.MachinePerspective.MACHINE_PERSPECTIVE_ID;

/**
 * The action contains business logic which calls special method to show logs of the extension server.
 *
 * @author Roman Nikitenko
 */
public class ShowExtServerLogsAction extends AbstractPerspectiveAction {

    private static final String EXT_SERVER_LOG_PATH = "/home/user/che/ext-server/logs/catalina.out";

    private final String                      restContext;
    private final AppContext                  appContext;
    private final NotificationManager         notificationManager;
    private final MachineLocalizationConstant localizedConstant;
    private final AnalyticsEventLogger        eventLogger;

    @Inject
    public ShowExtServerLogsAction(@RestContext String restContext,
                                   AppContext appContext,
                                   MachineLocalizationConstant locale,
                                   NotificationManager notificationManager,
                                   MachineLocalizationConstant localizedConstant,
                                   AnalyticsEventLogger eventLogger) {
        super(Collections.singletonList(MACHINE_PERSPECTIVE_ID),
              locale.showExtServerLogsTitle(),
              locale.showExtServerLogsDescription(),
              null, null);
        this.restContext = restContext;
        this.appContext = appContext;
        this.notificationManager = notificationManager;
        this.localizedConstant = localizedConstant;
        this.eventLogger = eventLogger;
    }

    /** {@inheritDoc} */
    @Override
    public void updateInPerspective(@Nonnull ActionEvent event) {
        //to do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(@Nonnull ActionEvent event) {
        eventLogger.log(this);

        String machineId = appContext.getDevMachineId();
        if (Strings.isNullOrEmpty(machineId)) {
            notificationManager.showWarning(localizedConstant.noDevMachine());
            Log.error(getClass(), "Can't to get logs of extension server without machine id");
            return;
        }
        String origin = Browser.getWindow().getLocation().getOrigin();
        String url = origin + restContext + "/machine/" + machineId + "/filepath" + EXT_SERVER_LOG_PATH + "?startFrom=2&limit=100";

        Window.open(url, "Logs", "");
    }
}
