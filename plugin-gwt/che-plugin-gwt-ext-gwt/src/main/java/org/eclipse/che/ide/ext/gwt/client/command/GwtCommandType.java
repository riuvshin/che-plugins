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
package org.eclipse.che.ide.ext.gwt.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectNameProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedList;

/**
 * GWT command type.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class GwtCommandType implements CommandType {

    public static final String COMMAND_TEMPLATE = "mvn clean gwt:run-codeserver";

    private static final String ID           = "gwt";
    private static final String DISPLAY_NAME = "GWT";

    private final GwtResources                   resources;
    private final CurrentProjectNameProvider     currentProjectNameProvider;
    private final DevMachineHostNameProvider     devMachineHostNameProvider;
    private final GwtCommandConfigurationFactory configurationFactory;

    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public GwtCommandType(GwtResources resources,
                          GwtCommandPagePresenter page,
                          CurrentProjectNameProvider currentProjectNameProvider,
                          DevMachineHostNameProvider devMachineHostNameProvider) {
        this.resources = resources;
        this.currentProjectNameProvider = currentProjectNameProvider;
        this.devMachineHostNameProvider = devMachineHostNameProvider;
        configurationFactory = new GwtCommandConfigurationFactory(this);
        pages = new LinkedList<>();
        pages.add(page);
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public SVGResource getIcon() {
        return resources.gwtCommandType();
    }

    @NotNull
    @Override
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @NotNull
    @Override
    public CommandConfigurationFactory<GwtCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @NotNull
    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectNameProvider.getKey() + " -Dgwt.bindAddress=" +
               devMachineHostNameProvider.getKey();
    }
}
