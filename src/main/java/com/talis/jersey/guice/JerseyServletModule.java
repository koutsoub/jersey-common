/*
 *    Copyright 2010 Talis Systems Ltd
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.talis.jersey.guice;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.talis.jersey.auth.HttpBasicAuthenticationFilter;
import com.talis.jersey.filters.LoggingFilter;
import com.talis.jersey.filters.ServerAgentHeaderFilter;

public class JerseyServletModule extends ServletModule{

	private final String[] propertyPackages;
	
	public JerseyServletModule(String...propertyPackages){
		this.propertyPackages = propertyPackages;
	}
	
	@Override
	protected void configureServlets() {
		final Map<String, String> params = new HashMap<String, String>();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, joinPackageNames(propertyPackages));
        params.put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE.toString());
        
        String requestFilters = joinClassNames(LoggingFilter.class, HttpBasicAuthenticationFilter.class, GZIPContentEncodingFilter.class);
        String responseFilters = joinClassNames(LoggingFilter.class, ServerAgentHeaderFilter.class, GZIPContentEncodingFilter.class);
        
        params.put(PackagesResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, requestFilters);
        params.put(PackagesResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, responseFilters);

        
		serve("/*").with(GuiceContainer.class, params);
	}
	
	private String joinPackageNames(String...packageName){
		StringBuilder builder = new StringBuilder("");
		boolean first = true;
		for(String name : packageName){
			if (first){
				first = false;
			}else{
				builder.append(",");
			}
			builder.append(name);
		}
		return builder.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private String joinClassNames(Class...clazz){
		StringBuilder builder = new StringBuilder("");
		boolean first = true;
		for(Class theClass : clazz){
			if (first){
				first = false;
			}else{
				builder.append(",");
			}
			builder.append(theClass.getName());
		}
		return builder.toString();
	}
}
