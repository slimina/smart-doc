/*
 * smart-doc https://github.com/shalousun/smart-doc
 *
 * Copyright (C) 2018-2022 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.power.doc.builder;

import com.power.doc.constants.TornaConstants;
import com.power.doc.factory.BuildTemplateFactory;
import com.power.doc.helper.JavaProjectBuilderHelper;
import com.power.doc.model.ApiConfig;
import com.power.doc.model.ApiDoc;
import com.power.doc.model.torna.Apis;
import com.power.doc.model.torna.TornaApi;
import com.power.doc.template.IDocBuildTemplate;
import com.power.doc.utils.TornaUtil;
import com.thoughtworks.qdox.JavaProjectBuilder;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.power.doc.utils.TornaUtil.buildApis;
import static com.power.doc.utils.TornaUtil.buildErrorCode;


/**
 * @author xingzi 2021/2/2 18:05
 **/
public class TornaBuilder {

    /**
     * build controller api
     *
     * @param config config
     */
    public static void buildApiDoc(ApiConfig config) {
        JavaProjectBuilder javaProjectBuilder = JavaProjectBuilderHelper.create();
        buildApiDoc(config, javaProjectBuilder);
    }


    /**
     * Only for smart-doc maven plugin and gradle plugin.
     *
     * @param config             ApiConfig
     * @param javaProjectBuilder ProjectDocConfigBuilder
     */
    public static void buildApiDoc(ApiConfig config, JavaProjectBuilder javaProjectBuilder) {
        config.setParamsDataToTree(true);
        DocBuilderTemplate builderTemplate = new DocBuilderTemplate();
        builderTemplate.checkAndInit(config, true);
        ProjectDocConfigBuilder configBuilder = new ProjectDocConfigBuilder(config, javaProjectBuilder);
        IDocBuildTemplate docBuildTemplate = BuildTemplateFactory.getDocBuildTemplate(config.getFramework());
        List<ApiDoc> apiDocList = docBuildTemplate.getApiData(configBuilder);
        apiDocList = docBuildTemplate.handleApiGroup(apiDocList, config);
        buildTorna(apiDocList, config, javaProjectBuilder);
    }

    /**
     * build torna Data
     *
     * @param apiDocs   apiData
     * @param apiConfig ApiConfig
     * @param builder   JavaProjectBuilder
     */
    public static void buildTorna(List<ApiDoc> apiDocs, ApiConfig apiConfig, JavaProjectBuilder builder) {
        TornaApi tornaApi = new TornaApi();
        tornaApi.setAuthor(apiConfig.getAuthor());
        tornaApi.setIsReplace(BooleanUtils.toInteger(apiConfig.getReplace()));
        Apis api;
        List<Apis> groupApiList = new ArrayList<>();
        //Convert ApiDoc to Apis
        for (ApiDoc groupApi : apiDocs) {
            List<Apis> apisList = new ArrayList<>();
            List<ApiDoc> childrenApiDocs = groupApi.getChildrenApiDocs();
            for (ApiDoc a : childrenApiDocs) {
                api = new Apis();
                api.setName(StringUtils.isBlank(a.getDesc()) ? a.getName() : a.getDesc());
                api.setItems(buildApis(a.getList(), TornaUtil.setDebugEnv(apiConfig, tornaApi)));
                api.setIsFolder(TornaConstants.YES);
                api.setAuthor(a.getAuthor());
                api.setOrderIndex(a.getOrder());
                apisList.add(api);
            }
            api = new Apis();
            api.setName(StringUtils.isBlank(groupApi.getDesc()) ? groupApi.getName() : groupApi.getDesc());
            api.setAuthor(tornaApi.getAuthor());
            api.setOrderIndex(groupApi.getOrder());
            api.setIsFolder(TornaConstants.YES);
            api.setItems(apisList);
            groupApiList.add(api);

        }
        tornaApi.setCommonErrorCodes(buildErrorCode(apiConfig, builder));
        // delete default group when only default group
        tornaApi.setApis(groupApiList.size() == 1 ? groupApiList.get(0).getItems() : groupApiList);
        // Push to torna
        TornaUtil.pushToTorna(tornaApi, apiConfig, builder);
    }
}

