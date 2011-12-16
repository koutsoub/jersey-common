/*
 *    Copyright 2011 Talis Systems Ltd
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

package com.talis.jersey.auth;

import java.security.Principal;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.core.util.Base64;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class HttpBasicAuthenticationFilter implements ContainerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(HttpBasicAuthenticationFilter.class);

    private static final String REALM = "MajatMonitoring";

    @Context UriInfo uriInfo;
    private final Authenticator authenticator;

    @Inject 
    public HttpBasicAuthenticationFilter(Authenticator authenticator){
    	this.authenticator = authenticator;
    }
    
	@Override
	public ContainerRequest filter(ContainerRequest request) {
	    User user = authenticate(request);
        request.setSecurityContext(new Authorizer(user));
        return request;
	}
    
    private User authenticate(ContainerRequest request) {
        String authentication = request.getHeaderValue(ContainerRequest.AUTHORIZATION);
        if (authentication == null) {
        	if(LOG.isDebugEnabled()){
        		LOG.debug("No authorization header supplied");
        	}       	
        	return User.UNAUTHENTICATED;
        }
        
        if (!authentication.startsWith("Basic ")) {
        	if(LOG.isDebugEnabled()){
        		LOG.debug(String.format("Unsupported authentication type [%s]", authentication));
        	}
            return User.UNAUTHENTICATED;
        }
        authentication = authentication.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(authentication)).split(":");
        if (values.length < 2) {
        	if(LOG.isDebugEnabled()){
        		LOG.debug(String.format("Incorrect number of arguments in credentials string [%s]", 
        								new String(Base64.base64Decode(authentication))));
        	}
            return User.UNAUTHENTICATED;
        }

        String username = values[0];
        String password = values[1];
        if (LOG.isDebugEnabled()){
        	LOG.debug(String.format("Credentials supplied [%s:%s]", username, password));
        }
        
        if ((username == null) || (password == null)) {
        	if(LOG.isDebugEnabled()){
        		LOG.debug("Username and/or Password was null, not authenticating");
        	}
            return User.UNAUTHENTICATED;
        }

        try{
        	authenticator.authenticate(username, password);
        	return new User(username, "admin");
        }catch(InvalidCredentialsException e){
        	if (LOG.isDebugEnabled()){
        		LOG.debug("Authentication failure", e);
        	}
        	throw new MappableContainerException(new AuthenticationException("Invalid username or password\r\n", REALM, e));
        }
    }

    public class Authorizer implements SecurityContext {

        private final User user;
        private final Principal principal;

        public Authorizer(final User user) {
            this.user = user;
            this.principal = new Principal() {

                public String getName() {
                    return user.username;
                }
            };
        }

        public Principal getUserPrincipal() {
            return this.principal;
        }

        public boolean isUserInRole(String role) {
            return (role.equals(user.role));
        }

        public boolean isSecure() {
            return "https".equals(uriInfo.getRequestUri().getScheme());
        }

        public String getAuthenticationScheme() {
            return SecurityContext.BASIC_AUTH;
        }
    }

    static class User {

    	static final User UNAUTHENTICATED = new User("anonymous", "user");
    	
    	public String username;
        public String role;

        public User(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
}
