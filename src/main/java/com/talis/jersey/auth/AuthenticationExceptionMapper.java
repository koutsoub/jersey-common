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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    public Response toResponse(AuthenticationException e) {
        if (e.getRealm() != null) {
            return Response.
                    status(Status.UNAUTHORIZED).
                    header("WWW-Authenticate", "Basic realm=\"" + e.getRealm() + "\"").
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        } else {
            return Response.
                    status(Status.UNAUTHORIZED).
                    type("text/plain").
                    entity(e.getMessage()).
                    build();
        }
    }

}