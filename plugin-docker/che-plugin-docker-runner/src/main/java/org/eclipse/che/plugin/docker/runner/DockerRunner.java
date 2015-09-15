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
package org.eclipse.che.plugin.docker.runner;

import com.google.gson.reflect.TypeToken;

import org.bouncycastle.util.encoders.Base64;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.project.shared.dto.RunnerEnvironment;
import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.runner.internal.ResourceAllocators;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.InitialAuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author andrew00x
 */
@Singleton
public class DockerRunner extends BaseDockerRunner {

    private final String            apiEndPoint;
    private final InitialAuthConfig initialAuthConfig;
    private static final String AUTH_PREFERENCE_NAME = "codenvy:dockerCredentials";

    @Inject
    public DockerRunner(@Named(Constants.DEPLOY_DIRECTORY) File deployDirectoryRoot,
                        @Named(Constants.APP_CLEANUP_TIME) int cleanupTime,
                        @Named(HOST_NAME) String hostName,
                        @Named("api.endpoint") String apiEndpoint,
                        @Nullable @Named(WATCH_UPDATE_OF_PROJECT_TYPES) String[] watchUpdateProjectTypes,
                        ResourceAllocators allocators,
                        CustomPortService portService,
                        InitialAuthConfig initialAuthConfig,
                        DockerConnector dockerConnector,
                        EventService eventService,
                        ApplicationLinksGenerator applicationLinksGenerator) {
        super(deployDirectoryRoot,
              cleanupTime,
              hostName,
              watchUpdateProjectTypes == null ? Collections.<String>emptySet() : new HashSet<>(Arrays.asList(watchUpdateProjectTypes)),
              allocators,
              portService,
              dockerConnector,
              eventService,
              applicationLinksGenerator);
        this.apiEndPoint = apiEndpoint;
        this.initialAuthConfig = initialAuthConfig;
    }

    public List<RunnerEnvironment> getEnvironments() {
        // Must no appears as 'system' runners.
        return Collections.emptyList();
    }

    @Override
    public String getName() {
        return "docker";
    }

    @Override
    public String getDescription() {
        return "The linux container runtime";
    }

    @Override
    protected DockerEnvironment getDockerEnvironment(RunRequest request) throws IOException, RunnerException {
        return new CustomDockerEnvironment(request);
    }

    @Override
    protected AuthConfigs getAuthConfigs(RunRequest request) throws IOException, RunnerException {
        AuthConfigs initial = initialAuthConfig.getAuthConfigs();
        try {
            String response = HttpJsonHelper.requestString(apiEndPoint + "/profile/prefs", "GET",
                                                           null,
                                                           Pair.of("token", request.getUserToken()));

            Map<String, String> userPrefs = JsonHelper.fromJson(response, Map.class, new TypeToken<Map<String, String>>() {
            }.getType());

            final String encodedAuthConfig = userPrefs.get(AUTH_PREFERENCE_NAME);
            if (encodedAuthConfig != null) {
                String userConfigJson = new String(Base64.decode(encodedAuthConfig));
                AuthConfigs userConfig = DtoFactory.getInstance().createDtoFromJson(userConfigJson, AuthConfigs.class);

                for (AuthConfig one : userConfig.getConfigs().values()) {
                    initial.getConfigs().put(one.getServeraddress(), one);
                }
            }
        } catch (ForbiddenException | UnauthorizedException | ServerException un) {
            return null;
        } catch (ConflictException | NotFoundException | JsonParseException e) {
            LOG.warn(e.getLocalizedMessage());
        }

        return initial;
    }
}
