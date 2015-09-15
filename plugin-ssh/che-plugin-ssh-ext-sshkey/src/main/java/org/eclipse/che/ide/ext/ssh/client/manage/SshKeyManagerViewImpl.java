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
package org.eclipse.che.ide.ext.ssh.client.manage;

import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.SshResources;
import org.eclipse.che.ide.ext.ssh.dto.KeyItem;
import org.eclipse.che.ide.ui.cellview.CellTableResources;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link SshKeyManagerView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class SshKeyManagerViewImpl extends Composite implements SshKeyManagerView {
    interface SshKeyManagerViewImplUiBinder extends UiBinder<Widget, SshKeyManagerViewImpl> {
    }

    private static SshKeyManagerViewImplUiBinder ourUiBinder = GWT.create(SshKeyManagerViewImplUiBinder.class);

    @UiField
    Button             btnGenerate;
    @UiField
    Button             btnUpload;
    @UiField
    SimplePanel        btnGenerateGithubKey;
    @UiField(provided = true)
    CellTable<KeyItem> keys;
    @UiField(provided = true)
    final   SshResources            res;
    @UiField(provided = true)
    final   SshLocalizationConstant locale;
    private ActionDelegate          delegate;

    /**
     * Create view.
     *
     * @param resources
     * @param locale
     */
    @Inject
    protected SshKeyManagerViewImpl(SshResources resources, SshLocalizationConstant locale, CellTableResources res) {
        this.res = resources;
        this.locale = locale;

        initSshKeyTable(res);
        initWidget(ourUiBinder.createAndBindUi(this));
        btnGenerateGithubKey.sinkEvents(Event.ONCLICK);
        btnGenerateGithubKey.addHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onGenerateGithubKeyClicked();

            }
        }, ClickEvent.getType());
    }

    /** Creates table what contains list of available ssh keys. */
    private void initSshKeyTable(CellTable.Resources res) {
        keys = new CellTable<KeyItem>(15, res);
        Column<KeyItem, String> hostColumn = new Column<KeyItem, String>(new TextCell()) {
            @Override
            public String getValue(KeyItem object) {
                return object.getHost();
            }

            @Override
            public void render(Context context, KeyItem object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "window-preferences-sshKeys-cellTable-host-"
                                      + context.getIndex() + "\">");
                super.render(context, object, sb);
            }
        };
        hostColumn.setSortable(true);

        Column<KeyItem, String> publicKeyColumn = new Column<KeyItem, String>(new ButtonCell()) {
            @Override
            public String getValue(KeyItem object) {
                if (object.getPublicKeyUrl() != null) {
                    return "View";
                } else {
                    return "";
                }
            }

            /** {@inheritDoc} */
            @Override
            public void render(Context context, KeyItem object, SafeHtmlBuilder sb) {
                if (object != null && object.getPublicKeyUrl() != null) {
                    sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "window-preferences-sshKeys-cellTable-key-"
                                          + context.getIndex() + "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        publicKeyColumn.setFieldUpdater(new FieldUpdater<KeyItem, String>() {
            @Override
            public void update(int index, KeyItem object, String value) {
                delegate.onViewClicked(object);
            }
        });

        Column<KeyItem, String> deleteKeyColumn = new Column<KeyItem, String>(new ButtonCell()) {
            @Override
            public String getValue(KeyItem object) {
                return "Delete";
            }

            @Override
            public void render(Context context, KeyItem object, SafeHtmlBuilder sb) {
                if (object != null) {
                    sb.appendHtmlConstant(
                            "<div id=\"" + UIObject.DEBUG_ID_PREFIX + "window-preferences-sshKeys-cellTable-delete-" + context.getIndex() +
                            "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        deleteKeyColumn.setFieldUpdater(new FieldUpdater<KeyItem, String>() {
            @Override
            public void update(int index, KeyItem object, String value) {
                delegate.onDeleteClicked(object);
            }
        });

        keys.addColumn(hostColumn, "Host");
        keys.addColumn(publicKeyColumn, "Public Key");
        keys.addColumn(deleteKeyColumn, "Delete");
        keys.setColumnWidth(hostColumn, 50, Style.Unit.PCT);
        keys.setColumnWidth(publicKeyColumn, 30, Style.Unit.PX);
        keys.setColumnWidth(deleteKeyColumn, 30, Style.Unit.PX);

        // don't show loading indicator
        keys.setLoadingIndicator(null);
    }

    /** {@inheritDoc} */
    @Override
    public void setKeys(@NotNull List<KeyItem> keys) {
        // Wraps Array in java.util.List
        List<KeyItem> appList = new ArrayList<KeyItem>();
        for (int i = 0; i < keys.size(); i++) {
            appList.add(keys.get(i));
        }
        this.keys.setRowData(appList);
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler("btnGenerate")
    public void onGenerateClicked(ClickEvent event) {
        delegate.onGenerateClicked();
    }

    @UiHandler("btnUpload")
    public void onUpdateClicked(ClickEvent event) {
        delegate.onUploadClicked();
    }
}
