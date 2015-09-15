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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.processes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.machine.shared.dto.ProcessDescriptor;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.TableResources;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The class contains methods to displaying processes.
 *
 * @author Dmitry Shnurenko
 */
@Singleton
public class ProcessesViewImpl extends Composite implements ProcessesView {
    interface ProcessesViewImplUiBinder extends UiBinder<Widget, ProcessesViewImpl> {
    }

    private final static ProcessesViewImplUiBinder UI_BINDER = GWT.create(ProcessesViewImplUiBinder.class);

    private ActionDelegate delegate;

    @UiField(provided = true)
    final CellTable<ProcessDescriptor> processesTable;
    @UiField(provided = true)
    final MachineLocalizationConstant  locale;

    @Inject
    public ProcessesViewImpl(MachineLocalizationConstant locale, TableResources tableResources) {
        this.locale = locale;
        this.processesTable = createTable(tableResources);

        initWidget(UI_BINDER.createAndBindUi(this));
    }

    private CellTable<ProcessDescriptor> createTable(@NotNull TableResources tableResources) {
        CellTable<ProcessDescriptor> table = new CellTable<>(0, tableResources);

        TextColumn<ProcessDescriptor> name = new TextColumn<ProcessDescriptor>() {
            @Override
            public String getValue(ProcessDescriptor descriptor) {
                return "Process " + descriptor.getPid();
            }
        };

        TextColumn<ProcessDescriptor> protocol = new TextColumn<ProcessDescriptor>() {
            @Override
            public String getValue(ProcessDescriptor descriptor) {
                //TODO it's stub. Need add real value
                return "tcp";
            }
        };

        TextColumn<ProcessDescriptor> port = new TextColumn<ProcessDescriptor>() {
            @Override
            public String getValue(ProcessDescriptor descriptor) {
                //TODO it's stub. Need add real value
                return "8000";
            }
        };

        TextColumn<ProcessDescriptor> time = new TextColumn<ProcessDescriptor>() {
            @Override
            public String getValue(ProcessDescriptor descriptor) {
                //TODO it's stub. Need add real value
                return "10:12:24";
            }
        };

        TextColumn<ProcessDescriptor> active = new TextColumn<ProcessDescriptor>() {
            @Override
            public String getValue(ProcessDescriptor descriptor) {
                boolean isActive = descriptor.isAlive();

                //TODO it's stub. Need add real value
                return isActive ? locale.processActive() : locale.processActive();
            }
        };

        table.addColumn(name, locale.processTableName());
        table.addColumn(protocol, locale.processTableProtocol());
        table.addColumn(port, locale.processTablePort());
        table.addColumn(time, locale.processTableTime());
        table.addColumn(active, locale.processTableActive());

        final SingleSelectionModel<ProcessDescriptor> selectionModel = new SingleSelectionModel<>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                delegate.onProcessClicked(selectionModel.getSelectedObject());
            }
        });
        table.setSelectionModel(selectionModel);
        table.setLoadingIndicator(null);

        return table;
    }

    /** {@inheritDoc} */
    @Override
    public void setProcesses(@NotNull List<ProcessDescriptor> descriptors) {
        processesTable.setRowData(descriptors);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }
}