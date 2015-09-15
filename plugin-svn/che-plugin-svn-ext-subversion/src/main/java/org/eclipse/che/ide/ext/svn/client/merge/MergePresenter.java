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
package org.eclipse.che.ide.ext.svn.client.merge;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ProjectExplorerPart;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.api.project.tree.TreeStructure;
import org.eclipse.che.ide.api.project.tree.generic.ProjectNode;
import org.eclipse.che.ide.ext.svn.client.SubversionClientService;
import org.eclipse.che.ide.ext.svn.client.common.RawOutputPresenter;
import org.eclipse.che.ide.ext.svn.client.common.SubversionActionPresenter;
import org.eclipse.che.ide.ext.svn.client.common.filteredtree.FilteredTreeStructureProvider;
import org.eclipse.che.ide.ext.svn.shared.CLIOutputResponse;
import org.eclipse.che.ide.ext.svn.shared.InfoResponse;
import org.eclipse.che.ide.ext.svn.shared.SubversionItem;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Manages merging the branches and folders.
 */
@Singleton
public class MergePresenter extends SubversionActionPresenter implements MergeView.ActionDelegate {

    private final MergeView view;
    private final SubversionClientService subversionClientService;
    private final AppContext appContext;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final NotificationManager notificationManager;
    private final FilteredTreeStructureProvider treeStructureProvider;

    /** Target tree node to merge. */
    private TreeNode<?> targetNode;

    /** Subversion target to merge. */
    private SubversionItem mergeTarget;

    /** Source URL to merge with. */
    private String sourceURL;

    /**
     * Creates an instance of this presenter.
     */
    @Inject
    public MergePresenter(final MergeView view,
                          final SubversionClientService subversionClientService,
                          final AppContext appContext,
                          final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                          final EventBus eventBus,
                          final RawOutputPresenter console,
                          final WorkspaceAgent workspaceAgent,
                          final ProjectExplorerPart projectExplorerPart,
                          final NotificationManager notificationManager,
                          final FilteredTreeStructureProvider treeStructureProvider) {
        super(appContext, eventBus, console, workspaceAgent, projectExplorerPart);

        this.view = view;
        this.subversionClientService = subversionClientService;
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.notificationManager = notificationManager;
        this.treeStructureProvider = treeStructureProvider;

        view.setDelegate(this);
    }

    /**
     * Prepares to merging and opens Merge dialog.
     */
    public void merge() {
        targetNode = getSelectedNode();
        if (targetNode == null) {
            return;
        }

        view.enableMergeButton(false);
        view.sourceCheckBox().setValue(false);

        /** get info of selected project item */
        String target = getSelectedPaths().get(0);
        subversionClientService.info(appContext.getCurrentProject().getRootProject().getPath(), target, "HEAD", false,
                new AsyncRequestCallback<InfoResponse>(dtoUnmarshallerFactory.newUnmarshaller(InfoResponse.class)) {
                    @Override
                    protected void onSuccess(InfoResponse result) {
                        if (result.getErrorOutput() != null && !result.getErrorOutput().isEmpty()) {
                            printResponse(null, null, result.getErrorOutput());
                            notificationManager.showError("Unable to execute subversion command");
                            return;
                        }

                        mergeTarget = result.getItems().get(0);
                        view.targetTextBox().setValue(mergeTarget.getRelativeURL());

                        String repositoryRoot = mergeTarget.getRepositoryRoot();

                        subversionClientService.info(appContext.getCurrentProject().getRootProject().getPath(), repositoryRoot, "HEAD", true,
                                new AsyncRequestCallback<InfoResponse>(dtoUnmarshallerFactory.newUnmarshaller(InfoResponse.class)) {
                                    @Override
                                    protected void onSuccess(InfoResponse result) {
                                        if (result.getErrorOutput() != null && !result.getErrorOutput().isEmpty()) {
                                            printResponse(null, null, result.getErrorOutput());
                                            notificationManager.showError("Unable to execute subversion command");
                                            return;
                                        }

                                        sourceURL = result.getItems().get(0).getURL();
                                        SubversionTreeNode subversionTreeNode = new SubversionTreeNode(result.getItems().get(0));

                                        List<TreeNode<?>> children = new ArrayList<>();
                                        if (result.getItems().size() > 1) {
                                            for (int i = 1; i < result.getItems().size(); i++) {
                                                SubversionItem item = result.getItems().get(i);
                                                if (!"file".equals(item.getNodeKind())) {
                                                    children.add(new SubversionTreeNode(item));
                                                }
                                            }
                                            Collections.sort(children, svnDirectoryComparator);
                                        }

                                        subversionTreeNode.setChildren(children);
                                        view.setRootNode(subversionTreeNode);
                                        view.show();
                                        validateSourceURL();
                                    }

                                    @Override
                                    protected void onFailure(Throwable exception) {
                                        notificationManager.showError(exception.getMessage());
                                    }
                                });
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.showError(exception.getMessage());
                    }
                });
    }

    /**
     * Performs actions when clicking Merge button.
     */
    @Override
    public void mergeClicked() {
        view.hide();

        String target = getSelectedPaths().get(0);
        subversionClientService.merge(appContext.getCurrentProject().getRootProject().getPath(), target, sourceURL,
                new AsyncRequestCallback<CLIOutputResponse>(dtoUnmarshallerFactory.newUnmarshaller(CLIOutputResponse.class)) {

                    @Override
                    protected void onSuccess(CLIOutputResponse result) {
                        printCommand(result.getCommand());
                        printAndSpace(result.getOutput());
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        notificationManager.showError(exception.getMessage());
                    }
                });
    }

    /**
     * Closes the dialog when clicking Cancel button.
     */
    @Override
    public void cancelClicked() {
        view.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceCheckBoxClicked() {
        if (true == view.sourceCheckBox().getValue()) {
            view.sourceURLTextBox().setValue(sourceURL);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSourceURLChanged(String sourceURL) {
        this.sourceURL = sourceURL;
        validateSourceURL();
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeSelected(TreeNode<?> node) {
        if (!(node instanceof SubversionTreeNode)) {
            return;
        }

        SubversionTreeNode treeNode = (SubversionTreeNode)node;
        sourceURL = treeNode.getData().getURL();
        validateSourceURL();
    }

    /** Validates source URL and disables Merge button if it's unable to merge. */
    private void validateSourceURL() {
        if (sourceURL.startsWith(mergeTarget.getURL())) {
            view.setError("Cannot merge directory itself");
            view.enableMergeButton(false);
            return;
        }

        if (mergeTarget.getURL().startsWith(sourceURL)) {
            view.setError("Cannot merge with parent directory");
            view.enableMergeButton(false);
            return;
        }

        view.setError(null);
        view.enableMergeButton(true);
    }

    /** {@inheritDoc} */
    @Override
    public void onNodeExpanded(TreeNode<?> node) {
        List<TreeNode<?>> children = node.getChildren();
        for (TreeNode<?> childNode : children) {
            if (childNode.getChildren() == null && childNode instanceof SubversionTreeNode) {

                final SubversionTreeNode subversionTreeNode = (SubversionTreeNode)childNode;

                subversionClientService.info(appContext.getCurrentProject().getRootProject().getPath(), subversionTreeNode.getData().getURL(), "HEAD", true,
                        new AsyncRequestCallback<InfoResponse>(dtoUnmarshallerFactory.newUnmarshaller(InfoResponse.class)) {
                            @Override
                            protected void onSuccess(final InfoResponse result) {
                                if (result.getErrorOutput() != null && !result.getErrorOutput().isEmpty()) {
                                    printResponse(null, null, result.getErrorOutput());
                                    notificationManager.showError("Unable to execute subversion command");
                                    return;
                                }

                                List<TreeNode<?>> children = new ArrayList<>();
                                if (result.getItems().size() > 1) {
                                    for (int i = 1; i < result.getItems().size(); i++) {
                                        SubversionItem item = result.getItems().get(i);
                                        if (!"file".equals(item.getNodeKind())) {
                                            children.add(new SubversionTreeNode(item));
                                        }
                                    }
                                    Collections.sort(children, svnDirectoryComparator);
                                }

                                subversionTreeNode.setChildren(children);
                                view.render(subversionTreeNode);
                            }

                            @Override
                            protected void onFailure(Throwable exception) {
                                notificationManager.showError(exception.getMessage());
                            }
                        });
            }
        }
    }

    /** Comparator to sort subversion tree items. */
    private Comparator<TreeNode<?>> svnDirectoryComparator = new Comparator<TreeNode<?>>() {
        @Override
        public int compare(TreeNode<?> node1, TreeNode<?> node2) {
            if (node1 instanceof SubversionTreeNode && node2 instanceof SubversionTreeNode) {
                return ((SubversionTreeNode)node1).getData().getPath()
                        .compareTo(((SubversionTreeNode)node2).getData().getPath());
            }

            return 0;
        }
    };

    /** Node to use as item in subversion tree. */
    public class SubversionTreeNode implements TreeNode<SubversionItem> {

        private SubversionItem data;

        private TreeNode<?> parent;

        private List<TreeNode<?>> children;

        public SubversionTreeNode(SubversionItem data) {
            this.data = data;
        }

        @Override
        public TreeNode<?> getParent() {
            return parent;
        }

        @Override
        public void setParent(TreeNode<?> parent) {
            this.parent = parent;
        }

        @Override
        public SubversionItem getData() {
            return data;
        }

        @Override
        public void setData(SubversionItem data) {
            this.data = data;
        }

        @NotNull
        @Override
        public String getId() {
            return data.getURL();
        }

        @NotNull
        @Override
        public TreeStructure getTreeStructure() {
            return null;
        }

        @NotNull
        @Override
        public ProjectNode getProject() {
            return null;
        }

        @NotNull
        @Override
        public String getDisplayName() {
            if (data.getRepositoryRoot().equals(data.getURL())) {
                return data.getRepositoryRoot();
            }

            return data.getPath();
        }

        @Nullable
        @Override
        public SVGImage getDisplayIcon() {
            return null;
        }

        @Override
        public void setDisplayIcon(SVGImage icon) {
        }

        @Override
        public boolean isLeaf() {
            if ("file".equals(data.getNodeKind())) {
                return true;
            }

            if (children != null && children.isEmpty()) {
                return true;
            }

            return false;
        }

        @NotNull
        @Override
        public List<TreeNode<?>> getChildren() {
            return children;
        }

        @Override
        public void setChildren(List<TreeNode<?>> children) {
            this.children = children;
        }

        @Override
        public void refreshChildren(AsyncCallback<TreeNode<?>> callback) {
        }

        @Override
        public void processNodeAction() {
        }

        @Override
        public boolean isRenamable() {
            return false;
        }

        @Override
        public void rename(String newName, RenameCallback callback) {
        }

        @Override
        public boolean isDeletable() {
            return false;
        }

        @Override
        public void delete(DeleteCallback callback) {
        }

        private TreeNodeElement<TreeNode<?>> treeNodeElement;

        @Override
        public TreeNodeElement<TreeNode<?>> getTreeNodeElement() {
            return treeNodeElement;
        }

        @Override
        public void setTreeNodeElement(TreeNodeElement<TreeNode<?>> treeNodeElement) {
            this.treeNodeElement = treeNodeElement;
        }
    }

}
