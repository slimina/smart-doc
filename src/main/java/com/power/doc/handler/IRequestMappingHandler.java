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
package com.power.doc.handler;

import com.power.common.util.StringUtil;
import com.power.common.util.UrlUtil;
import com.power.doc.builder.ProjectDocConfigBuilder;
import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.function.RequestMappingFunc;
import com.power.doc.model.annotation.FrameworkAnnotations;
import com.power.doc.model.request.RequestMapping;
import com.power.doc.utils.DocUrlUtil;
import com.thoughtworks.qdox.model.JavaMethod;

import java.util.Objects;

/**
 * @author yu3.sun on 2022/10/1
 */
public interface IRequestMappingHandler {

    default RequestMapping formatMappingData(ProjectDocConfigBuilder projectBuilder, String controllerBaseUrl, RequestMapping requestMapping) {
        String shortUrl = requestMapping.getShortUrl();
        if (Objects.nonNull(shortUrl)) {
            String serverUrl = projectBuilder.getServerUrl();
            String contextPath = projectBuilder.getApiConfig().getPathPrefix();
            shortUrl = StringUtil.removeQuotes(shortUrl);
            String url = DocUrlUtil.getMvcUrls(serverUrl, contextPath + "/" + controllerBaseUrl, shortUrl);
            shortUrl = DocUrlUtil.getMvcUrls(DocGlobalConstants.EMPTY, contextPath + "/" + controllerBaseUrl, shortUrl);
            String urlSuffix = projectBuilder.getApiConfig().getUrlSuffix();
            if (StringUtil.isNotEmpty(urlSuffix)) {
                url = UrlUtil.simplifyUrl(StringUtil.trim(url)) + urlSuffix;
                shortUrl = UrlUtil.simplifyUrl(StringUtil.trim(shortUrl)) + urlSuffix;
            } else {
                url = UrlUtil.simplifyUrl(StringUtil.trim(url));
                shortUrl = UrlUtil.simplifyUrl(StringUtil.trim(shortUrl));
            }
            requestMapping.setUrl(url).setShortUrl(shortUrl);
            return requestMapping;
        }
        return null;
    }

    RequestMapping handle(ProjectDocConfigBuilder projectBuilder, String controllerBaseUrl, JavaMethod method,
                          FrameworkAnnotations frameworkAnnotations,
                          RequestMappingFunc requestMappingFunc);
}
