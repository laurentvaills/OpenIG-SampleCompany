/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 Charan Mann
 * Portions Copyrighted 2016 ForgeRock AS
 *
 * OpenIG-SampleCompany: Created by Charan Mann on 5/11/16 , 2:12 PM.
 */

/*
 * Groovy script for retrieving user profile attributes and setting as HTTP headers
 *
 * This script requires these arguments: passwordAttribute, openamUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status

import static groovyx.net.http.ContentType.JSON

/**
 * Creates unauthorized error
 * @return Status.UNAUTHORIZED
 */
def getUnauthorizedError() {
    logger.info("Returning UNAUTHORIZED error")
    Response errResponse = new Response()

    errResponse.status = Status.UNAUTHORIZED
    errResponse.headers.add("Content-Type", ["application/json; charset=utf-8"])
    errResponse.entity = "{\"code\": 401,\"reason\":\"Unauthorized\",\"message\":\"Authentication Failed\"}"
    return errResponse
}

def openAMRESTClient = new RESTClient(openamUrl)

// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookie = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform cookie validation and get uid
    logger.info("iPlanetDirectoryPro cookie found, performing validation")
    response = openAMRESTClient.post(path: 'sessions/' + openAMCookie, query: ['_action': 'validate'])
    isTokenValid = response.getData().get("valid")
    uid = response.getData().get("uid")

    // If cookie validation succeeds and has valid uid
    if (isTokenValid && null != uid) {

        // Retrieving session attributes
        logger.info("Retrieving session attribute: " + passwordAttribute)
        response = openAMRESTClient.post(path: 'sessions/', query: ['_action': 'getProperty'], headers: ['Content-Type': 'application/json', 'iPlanetDirectoryPro': openAMCookie], body: ["properties": [passwordAttribute]], requestContentType: JSON)

        // Set the attributes in header
        logger.info("Setting HTTP header: " + "username" + " ,value: " + uid)
        request.headers.add("username", uid)

        logger.info("Setting HTTP header: " + "password" + " ,value: " + response.getData().get(passwordAttribute))
        request.headers.add("password", response.getData().get(passwordAttribute))

        // Call the next handler. This returns when the request has been handled.
        return next.handle(context, request)
    } else {
        logger.info("Token validation failed")
        return getUnauthorizedError()
    }
} else {
    logger.info("No iPlanetDirectoryPro cookie present")
    return getUnauthorizedError()
}




