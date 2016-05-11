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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/3/16 , 6:12 AM.
 */

/*
 * Groovy script for retrieving user profile attributes and setting as HTTP headers
 *
 * This script requires these arguments: profileAttributes, openamUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status

/**
 * Creates unauthorized error
 * @return Status.UNAUTHORIZED
 */
def getUnauthorizedError() {
    Response response = new Response()
    response.status = Status.UNAUTHORIZED
    response.entity = "Authentication Failed"
    return response
}

def openAMRESTClient = new RESTClient(openamUrl)

// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookie = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform cookie validation and get uid
    println("iPlanetDirectoryPro cookie found, performing validation")
    response = openAMRESTClient.post(path: 'sessions/' + openAMCookie, query: ['_action': 'validate'])
    isTokenValid = response.getData().get("valid")
    uid = response.getData().get("uid")

    // If cookie validation succeeds and has valid uid
    if (isTokenValid && null != uid) {

        // Retrieving user profile attributes
        println("Retrieving user profile attributes: " + profileAttributes + " for user: " + uid)
        response = openAMRESTClient.get(path: 'users/' + uid, headers: ['iPlanetDirectoryPro': openAMCookie])

        // Iterate over required profile attributes
        for (attrName in profileAttributes.split()) {
            attrValue = response.getData().get(attrName)[0];

            // Set the attributes in header
            println("Setting HTTP header: " + attrName + " ,value: " + attrValue)
            request.headers.add(attrName, attrValue)
        }

        // Call the next handler. This returns when the request has been handled.
        return next.handle(context, request)
    } else {
        println("Token validation failed")
        return getUnauthorizedError()
    }
} else {
    println("No iPlanetDirectoryPro cookie present")
    return getUnauthorizedError()
}



