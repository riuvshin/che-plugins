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
package org.eclipse.che.ide.ext.runner.client.tabs.properties.panel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.ext.runner.client.RunnerLocalizationConstant;
import org.eclipse.che.ide.ext.runner.client.RunnerResources;
import org.eclipse.che.ide.ext.runner.client.inject.factories.WidgetFactory;
import org.eclipse.che.ide.ext.runner.client.tabs.container.tab.Background;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.button.PropertyButtonWidget;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Boot;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope;
import org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Shutdown;
import org.eclipse.che.ide.ui.switcher.Switcher;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.ide.ext.runner.client.tabs.container.tab.Background.BLUE;
import static org.eclipse.che.ide.ext.runner.client.tabs.container.tab.Background.GREY;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM.DEFAULT;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM.MB_100;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.RAM.MB_8000;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope.PROJECT;
import static org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.Scope.SYSTEM;

/**
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public class PropertiesPanelViewImpl extends Composite implements PropertiesPanelView {

    interface PropertiesPanelViewImplUiBinder extends UiBinder<Widget, PropertiesPanelViewImpl> {
    }

    private static final PropertiesPanelViewImplUiBinder UI_BINDER = GWT.create(PropertiesPanelViewImplUiBinder.class);

    public static final String PORT_STUB = " ------------> ";

    @UiField
    Label     configLink;
    @UiField
    FlowPanel configLinkPanel;
    @UiField
    TextBox   name;
    @UiField
    TextBox   type;

    @UiField
    FlowPanel buttonsPanel;

    @UiField
    ListBox   ram;
    @UiField
    ListBox   scope;
    @UiField
    ListBox   boot;
    @UiField
    ListBox   shutdown;
    @UiField
    Switcher  projectDefault;
    @UiField
    FlowPanel projectDefaultPanel;


    @UiField
    DockLayoutPanel   propertiesPanel;
    @UiField
    SimpleLayoutPanel editorPanel;
    @UiField
    Label             dockerLabel;
    @UiField
    FlowPanel         portMappingHeader;
    @UiField
    FlowPanel         portsPanel;

    @UiField(provided = true)
    final RunnerLocalizationConstant locale;
    @UiField(provided = true)
    final RunnerResources            resources;

    private final WidgetFactory widgetFactory;
    private final Label         unAvailableMessage;

    private ActionDelegate delegate;

    private PropertyButtonWidget saveBtn;
    private PropertyButtonWidget cancelBtn;
    private PropertyButtonWidget deleteBtn;

    @Inject
    public PropertiesPanelViewImpl(RunnerLocalizationConstant locale,
                                   RunnerResources resources,
                                   WidgetFactory widgetFactory) {
        this.locale = locale;
        this.resources = resources;

        initWidget(UI_BINDER.createAndBindUi(this));

        this.widgetFactory = widgetFactory;

        prepareField(ram, EnumSet.range(MB_100, MB_8000));
        prepareField(scope, EnumSet.range(PROJECT, SYSTEM));
        prepareField(boot, EnumSet.allOf(Boot.class));
        prepareField(shutdown, EnumSet.allOf(Shutdown.class));

        unAvailableMessage = new Label(locale.editorNotReady());
        unAvailableMessage.addStyleName(resources.runnerCss().fullSize());
        unAvailableMessage.addStyleName(resources.runnerCss().unAvailableMessage());

        editorPanel.setWidget(unAvailableMessage);

        PropertyButtonWidget.ActionDelegate createDelegate = new PropertyButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onCopyButtonClicked();
            }
        };
        createButton(locale.propertiesButtonCreate(), createDelegate, BLUE);

        PropertyButtonWidget.ActionDelegate saveDelegate = new PropertyButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onSaveButtonClicked();
            }
        };
        saveBtn = createButton(locale.propertiesButtonSave(), saveDelegate, GREY);

        PropertyButtonWidget.ActionDelegate deleteDelegate = new PropertyButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onDeleteButtonClicked();
            }
        };
        deleteBtn = createButton(locale.propertiesButtonDelete(), deleteDelegate, GREY);

        PropertyButtonWidget.ActionDelegate cancelDelegate = new PropertyButtonWidget.ActionDelegate() {
            @Override
            public void onButtonClicked() {
                delegate.onCancelButtonClicked();
            }
        };
        cancelBtn = createButton(locale.propertiesButtonCancel(), cancelDelegate, GREY);


        ValueChangeHandler<Boolean> valueChangeHandler = new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                delegate.onSwitcherChanged(valueChangeEvent.getValue());
            }
        };

        projectDefault.addValueChangeHandler(valueChangeHandler);

        portMappingHeader.setVisible(false);
    }

    private void prepareField(@NotNull ListBox field, @NotNull Set<? extends Enum> items) {
        for (Enum item : items) {
            field.addItem(item.toString().toLowerCase());
        }
    }

    @NotNull
    private PropertyButtonWidget createButton(@NotNull String title,
                                              @NotNull PropertyButtonWidget.ActionDelegate delegate,
                                              @NotNull Background background) {
        PropertyButtonWidget button = widgetFactory.createPropertyButton(title, background);
        button.setDelegate(delegate);

        buttonsPanel.add(button);

        return button;
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getName() {
        return name.getText();
    }

    /** {@inheritDoc} */
    @Override
    public void setName(@NotNull String name) {
        this.name.setText(name);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public RAM getRam() {
        String value = ram.getValue(ram.getSelectedIndex());
        return RAM.detect(value);
    }

    /** {@inheritDoc} */
    @Override
    public void selectMemory(@NotNull RAM size) {
        if (DEFAULT.equals(size)) {
            selectDefaultMemory(Integer.toString(DEFAULT.getValue()));
        } else {
            ram.setItemSelected(size.ordinal(), true);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addRamValue(@Min(value=0) int value) {
        for (int i = 0; i < ram.getItemCount(); i++) {
            if (ram.getValue(i).equals(value + " mb")) {
                return;
            }
        }
        ram.addItem(String.valueOf(value) + " mb");
    }

    /** {@inheritDoc} */
    @Override
    public void selectMemory(@Min(value=0) int size) {
        for (int i = 0; i < ram.getItemCount(); i++) {
            if (ram.getValue(i).equals(size + " mb")) {
                ram.setItemSelected(i, true);

                return;
            }
        }

        selectDefaultMemory(Integer.toString(DEFAULT.getValue()));
    }

    private void selectDefaultMemory(@NotNull String size) {
        size = size + " mb";
        int amountItems = ram.getItemCount();
        for (int index = 0; index < amountItems; index++) {
            if (size.equals(ram.getValue(index))) {
                ram.setItemSelected(index, true);
                return;
            }
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Scope getScope() {
        String value = scope.getValue(scope.getSelectedIndex());
        return Scope.detect(value);
    }

    /** {@inheritDoc} */
    @Override
    public void selectScope(@NotNull Scope scope) {
        this.scope.setItemSelected(scope.ordinal(), true);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getType() {
        return type.getText();
    }

    @Override
    public void setConfig(@NotNull String config) {
        configLink.setText(config);
    }

    /** {@inheritDoc} */
    @Override
    public void setType(@NotNull String type) {
        this.type.setText(type);
    }

    @Override
    public void setPorts(Map<String, String> ports) {
        if (ports == null) {
            portMappingHeader.setVisible(false);
            portsPanel.clear();
            return;
        }

        portMappingHeader.setVisible(true);
        for (Map.Entry<String, String> entry : ports.entrySet()) {
            FlowPanel port = new FlowPanel();
            Label portLabel = new Label(entry.getValue() + PORT_STUB + entry.getKey());
            port.add(portLabel);
            portsPanel.add(port);
        }
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Boot getBoot() {
        String value = boot.getValue(boot.getSelectedIndex());
        return Boot.detect(value);
    }

    /** {@inheritDoc} */
    @Override
    public void selectBoot(@NotNull Boot boot) {
        this.boot.setItemSelected(boot.ordinal(), true);
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public Shutdown getShutdown() {
        String value = shutdown.getValue(shutdown.getSelectedIndex());
        return Shutdown.detect(value);
    }

    /** {@inheritDoc} */
    @Override
    public void selectShutdown(@NotNull Shutdown shutdown) {
        this.shutdown.setItemSelected(shutdown.ordinal(), true);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableSaveButton(boolean enable) {
        saveBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableCancelButton(boolean enable) {
        cancelBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableDeleteButton(boolean enable) {
        deleteBtn.setEnable(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableNameProperty(boolean enable) {
        name.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableRamProperty(boolean enable) {
        ram.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableBootProperty(boolean enable) {
        boot.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableShutdownProperty(boolean enable) {
        shutdown.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setEnableScopeProperty(boolean enable) {
        scope.setEnabled(enable);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleSaveButton(boolean visible) {
        saveBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleDeleteButton(boolean visible) {
        deleteBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleCancelButton(boolean visible) {
        cancelBtn.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void setVisibleConfigLink(boolean visible) {
        configLinkPanel.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void showEditor(@Nullable EditorPartPresenter editor) {
        if (editor == null) {
            editorPanel.setWidget(unAvailableMessage);
        } else {
            editor.go(editorPanel);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void hideButtonsPanel() {
        propertiesPanel.setWidgetHidden(buttonsPanel, true);
    }

    /** {@inheritDoc} */
    @Override
    public void changeSwitcherState(boolean isOn) {
        projectDefault.setValue(isOn);
    }

    /** {@inheritDoc} */
    @Override
    public void hideSwitcher() {
        projectDefaultPanel.setVisible(false);
    }

    /** {@inheritDoc} */
    @Override
    public void incorrectName(boolean isCorrect) {
        saveBtn.setEnable(!isCorrect);

        name.getElement().getStyle().setBorderColor(isCorrect ? "#ffe400" : "#191c1e");
    }

    @UiHandler("name")
    public void onTextInputted(@SuppressWarnings("UnusedParameters") KeyUpEvent event) {
        delegate.onConfigurationChanged();
    }

    @UiHandler({"ram", "scope", "boot", "shutdown"})
    public void handleChange(@SuppressWarnings("UnusedParameters") ChangeEvent event) {
        delegate.onConfigurationChanged();
    }

    @UiHandler({"configLink"})
    public void handleConfigClick(@SuppressWarnings("UnusedParameters") ClickEvent event) {
        delegate.onConfigLinkClicked();
    }
}