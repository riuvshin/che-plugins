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
package org.eclipse.che.ide.ext.github.client;

import org.eclipse.che.ide.ext.github.shared.Collaborators;
import org.eclipse.che.ide.ext.github.shared.GitHubIssueComment;
import org.eclipse.che.ide.ext.github.shared.GitHubIssueCommentInput;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequest;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestCreationInput;
import org.eclipse.che.ide.ext.github.shared.GitHubPullRequestList;
import org.eclipse.che.ide.ext.github.shared.GitHubRepository;
import org.eclipse.che.ide.ext.github.shared.GitHubRepositoryList;
import org.eclipse.che.ide.ext.github.shared.GitHubUser;
import org.eclipse.che.ide.rest.AsyncRequestCallback;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Client service for Samples.
 *
 * @author Oksana Vereshchaka
 * @author Kevin Pollet
 */
public interface GitHubClientService {
    /**
     * Get given repository information.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getRepository(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback);

    /**
     * Get list of available public and private repositories of the authorized user.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesList(@NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of forks for given repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getForks(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Fork the given repository for the authorized user.
     *
     * @param user
     *         the owner of the repository to fork.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void fork(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubRepository> callback);

    /**
     * Add a comment to the issue on the given repository.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param issue
     *         the issue number.
     * @param input
     *         the comment.
     * @param callback
     *         callback called when operation is done.
     */
    void commentIssue(@NotNull String user, @NotNull String repository, @NotNull String issue, @NotNull GitHubIssueCommentInput input,
                      @NotNull AsyncRequestCallback<GitHubIssueComment> callback);

    /**
     * Get pull requests for given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getPullRequests(@NotNull String owner, @NotNull String repository, @NotNull AsyncRequestCallback<GitHubPullRequestList> callback);

    /**
     * Get a pull request by id for a given repository.
     * 
     * @param owner the owner of the target repository
     * @param repository the target repository
     * @param pullRequestId the Id of the pull request
     * @param callback the callback with either the pull request as argument or null if it doesn't exist
     */
    void getPullRequest(@NotNull String owner,
                        @NotNull String repository,
                        @NotNull String pullRequestId,
                        @NotNull AsyncRequestCallback<GitHubPullRequest> callback);

    /**
     * Create a pull request on origin repository
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param input
     *         the pull request information.
     * @param callback
     *         callback called when operation is done.
     */
    void createPullRequest(@NotNull String user, @NotNull String repository, @NotNull GitHubPullRequestCreationInput input,
                           @NotNull AsyncRequestCallback<GitHubPullRequest> callback);

    /**
     * Get the list of available public repositories for a GitHub user.
     *
     * @param userName
     *         the name of GitHub User
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByUser(String userName, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get the list of available repositories by GitHub organization.
     *
     * @param organization
     *         the name of GitHub organization.
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByOrganization(String organization, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of available public repositories for GitHub account.
     *
     * @param account
     *         the GitHub account.
     * @param callback
     *         callback called when operation is done.
     */
    void getRepositoriesByAccount(String account, @NotNull AsyncRequestCallback<GitHubRepositoryList> callback);

    /**
     * Get list of collaborators of GitHub repository. For detail see GitHub REST API http://developer.github.com/v3/repos/collaborators/.
     *
     * @param user
     *         the owner of the repository.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     */
    void getCollaborators(@NotNull String user, @NotNull String repository, @NotNull AsyncRequestCallback<Collaborators> callback);

    /**
     * Get the GitHub oAuth token for the pointed user.
     *
     * @param user
     *         user's id
     * @param callback
     *         callback called when operation is done.
     */
    void getUserToken(@NotNull String user, @NotNull AsyncRequestCallback<String> callback);

    /**
     * Get the map of available public and private repositories of the authorized user and organizations he exists in.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void getAllRepositories(@NotNull AsyncRequestCallback<Map<String, List<GitHubRepository>>> callback);

    /**
     * Get the list of the organizations, where authorized user is a member.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void getOrganizations(@NotNull AsyncRequestCallback<List<String>> callback);

    /**
     * Get authorized user information.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void getUserInfo(@NotNull AsyncRequestCallback<GitHubUser> callback);

    /**
     * Generate and upload new public key if not exist on github.com.
     *
     * @param callback
     *         callback called when operation is done.
     */
    void updatePublicKey(@NotNull AsyncRequestCallback<Void> callback);
}