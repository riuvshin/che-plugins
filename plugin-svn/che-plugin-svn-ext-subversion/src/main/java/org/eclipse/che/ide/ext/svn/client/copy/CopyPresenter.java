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
package org.eclipse.che.ide.ext.svn.client.copy;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.generic.FileNode;
import org.eclipse.che.ide.api.project.tree.generic.FolderNode;
import org.eclipse.che.ide.api.project.tree.generic.ProjectNode;
import org.eclipse.che.ide.api.project.tree.generic.Openable;
import org.eclipse.che.ide.api.project.tree.generic.StorableNode;
import org.eclipse.che.ide.ext.svn.client.SubversionClientService;
import org.eclipse.che.ide.ext.svn.client.SubversionExtensionLocalizationConstants;
import org.eclipse.che.ide.ext.svn.client.common.RawOutputPresenter;
import org.eclipse.che.ide.ext.svn.client.common.SubversionActionPresenter;
import org.eclipse.che.ide.ext.svn.client.common.filteredtree.FilteredTreeStructureProvider;
import org.eclipse.che.ide.ext.svn.shared.CLIOutputResponse;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.RegExpUtils;
import org.eclipse.che.ide.util.loging.Log;

import org.eclipse.che.commons.annotation.Nullable;
import java.util.List;

import static org.eclipse.che.ide.api.notification.Notification.Status.FINISHED;
import static org.eclipse.che.ide.api.notification.Notification.Status.PROGRESS;
import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;
import static org.eclipse.che.ide.api.notification.Notification.Type.INFO;

/**
 * Presenter for the {@link org.eclipse.che.ide.ext.svn.client.copy.CopyView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CopyPresenter extends SubversionActionPresenter implements CopyView.ActionDelegate {

    private AppContext                               appContext;
    private EventBus                                 eventBus;
    private CopyView                                 view;
    private NotificationManager                      notificationManager;
    private SubversionClientService                  service;
    private DtoUnmarshallerFactory                   dtoUnmarshallerFactory;
    private FilteredTreeStructureProvider            treeStructureProvider;
    private SubversionExtensionLocalizationConstants constants;
    private TreeNode<?>                              sourceNode;
    private Notification                             notification;
    private TargetHolder targetHolder = new TargetHolder();

    private RegExp urlRegExp = RegExp.compile("^(https?|ftp)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    private class TargetHolder {
        // /project/path/to/destination/directory
        String dir;
        // /project/path/to/destination/directory/[item_name]
        String name;

        /** Prepare target directory and item name to normal path. */
        String normalize() {
            dir = dir.endsWith("/") ? dir : dir + '/';

            if (!Strings.isNullOrEmpty(name)) {
                name = name.startsWith("/") ? name.substring(1) : name;
            } else if (!Strings.isNullOrEmpty(view.getNewName())) {
                name = view.getNewName();
            } else if (sourceNode != null) {
                name = sourceNode.getId();
            }

            return dir + name;
        }
    }

    @Inject
    protected CopyPresenter(AppContext appContext,
                            EventBus eventBus,
                            RawOutputPresenter console,
                            WorkspaceAgent workspaceAgent,
                            CopyView view,
                            NotificationManager notificationManager,
                            SubversionClientService service,
                            DtoUnmarshallerFactory dtoUnmarshallerFactory,
                            FilteredTreeStructureProvider treeStructureProvider,
                            SubversionExtensionLocalizationConstants constants,
                            final ProjectExplorerPart projectExplorerPart) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);
        this.appContext = appContext;
        this.eventBus = eventBus;
        this.view = view;
        this.notificationManager = notificationManager;
        this.service = service;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.treeStructureProvider = treeStructureProvider;
        this.constants = constants;
        this.view.setDelegate(this);
    }

    /** Show copy dialog. */
    public void showCopy(TreeNode<?> sourceNode) {
        if (sourceNode == null) {
            return;
        }

        this.sourceNode = sourceNode;

        if (sourceNode instanceof FileNode) {
            view.setDialogTitle(constants.copyViewTitleFile());
        } else if (sourceNode instanceof FolderNode || sourceNode instanceof ProjectNode) {
            view.setDialogTitle(constants.copyViewTitleDirectory());
        }

        targetHolder.name = sourceNode.getId();

        view.setNewName(targetHolder.name);
        view.setComment(targetHolder.name);
        view.setSourcePath(getStorableNodePath(sourceNode), false);

        validate();

        treeStructureProvider.get().getRootNodes(new AsyncCallback<List<TreeNode<?>>>() {
            @Override
            public void onFailure(Throwable caught) {
                notificationManager.showError(constants.copyFailToGetProject());
            }

            @Override
            public void onSuccess(List<TreeNode<?>> result) {
                view.setProjectNodes(result);
            }
        });

        view.show();
    }

    /** {@inheritDoc} */
    @Override
    public void onCopyClicked() {
        final String projectPath = getCurrentProjectPath();

        if (projectPath == null) {
            return;
        }

        final String src = view.isSourceCheckBoxSelected() ? view.getSourcePath() : relPath(projectPath, getStorableNodePath(sourceNode));
        final String target = view.isTargetCheckBoxSelected() ? view.getTargetUrl() : relPath(projectPath, targetHolder.normalize());
        final String comment = view.isTargetCheckBoxSelected() ? view.getComment() : null;

        notification = new Notification(constants.copyNotificationStarted(src), PROGRESS);
        notificationManager.showNotification(notification);

        view.hide();

        service.copy(projectPath, src, target, comment,
                     new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {
                         @Override
                         protected void onSuccess(CLIOutputResponse result) {
                             printResponse(result.getCommand(), result.getOutput(), result.getErrOutput());

                             notification.setMessage(constants.copyNotificationSuccessful());
                             notification.setStatus(FINISHED);
                             notification.setType(INFO);

                             refreshNodes(targetHolder.dir);
                         }

                         @Override
                         protected void onFailure(Throwable exception) {
                             String errorMessage = exception.getMessage();

                             notification.setMessage(constants.copyNotificationFailed() + ": " + errorMessage);
                             notification.setStatus(FINISHED);
                             notification.setType(ERROR);
                         }
                     });
    }

    private void refreshNodes(String forPath) {
        appContext.getCurrentProject().getCurrentTree().getNodeByPath(forPath, new AsyncCallback<TreeNode<?>>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error(CopyPresenter.class, caught);
            }

            @Override
            public void onSuccess(TreeNode<?> result) {
//                eventBus.fireEvent(new RefreshProjectTreeEvent(result, true));
                updateProjectExplorer();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void onNewNameChanged(String newName) {
        targetHolder.name = newName;
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(TreeNode<?> destinationNode) {
        targetHolder.dir = getStorableNodePath(destinationNode);
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourcePathChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetUrlChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceCheckBoxChanged() {
        // url path chosen
        if (view.isSourceCheckBoxSelected()) {
            view.setSourcePath("", true);
            view.setNewName("name");
            targetHolder.name = null;
        } else {
            view.setSourcePath(getStorableNodePath(sourceNode), false);
            view.setNewName(sourceNode.getId());
            targetHolder.name = sourceNode.getId();
        }

        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void onTargetCheckBoxChanged() {
        validate();
    }

    /** {@inheritDoc} */
    @Override
    public void minimize() {
        //stub
    }

    /** {@inheritDoc} */
    @Override
    public void activatePart() {
        //stub
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeExpanded(final TreeNode<?> node) {
        if (node.getChildren().isEmpty()) {
            // If children is empty then node may be not refreshed yet?
            node.refreshChildren(new AsyncCallback<TreeNode<?>>() {
                @Override
                public void onSuccess(TreeNode<?> result) {
                    if (node instanceof Openable) {
                        ((Openable)node).open();
                    }
                    if (!result.getChildren().isEmpty()) {
                        view.updateProjectNode(result, result);
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    Log.error(CopyPresenter.class, caught);
                }
            });
        }
    }

    private String relPath(String base, String path) {
        if (!path.startsWith(base)) {
            return null;
        }

        final String temp = path.substring(base.length());

        return temp.startsWith("/") ? temp.substring(1) : temp;
    }

    private void validate() {
        ValidationStrategy strategy;

        if (view.isSourceCheckBoxSelected()) {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new UrlUrlValidation();
            } else {
                strategy = new UrlFileValidation();
            }
        } else {
            if (view.isTargetCheckBoxSelected()) {
                strategy = new FileUrlValidation();
            } else {
                strategy = new FileFileValidation();
            }
        }

        if (strategy.isValid()) {
            view.hideErrorMarker();
        }
    }

    @Nullable
    private String getStorableNodePath(TreeNode<?> node) {
        return node instanceof StorableNode ? ((StorableNode)node).getPath() : null;
    }

    private interface ValidationStrategy {
        boolean isValid();
    }

    private class FileFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (Strings.isNullOrEmpty(targetHolder.dir)) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            if (targetHolder.normalize().equals(getStorableNodePath(sourceNode))) {
                view.showErrorMarker(constants.copyItemEqual());
                return false;
            }

            if (targetHolder.dir.startsWith(Strings.nullToEmpty(getStorableNodePath(sourceNode)))) {
                view.showErrorMarker(constants.copyItemChildDetect());
                return false;
            }

            return true;
        }
    }

    private class UrlFileValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (Strings.isNullOrEmpty(targetHolder.dir)) {
                view.showErrorMarker(constants.copyEmptyTarget());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            return true;
        }
    }

    private class FileUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }

    private class UrlUrlValidation implements ValidationStrategy {
        @Override
        public boolean isValid() {
            if (!RegExpUtils.resetAndTest(urlRegExp, view.getSourcePath())) {
                view.showErrorMarker(constants.copySourceWrongURL());
                return false;
            }

            if (!RegExpUtils.resetAndTest(urlRegExp, view.getTargetUrl())) {
                view.showErrorMarker(constants.copyTargetWrongURL());
                return false;
            }

            return true;
        }
    }
}
