# OpenIG-SampleCompany

*OpenIG configuration files for SampleCompany*


Disclaimer of Liability :
=========================
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. ForgeRock disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.

Pre-requisites :
================
1. Create Linux Server/VM with 4 CPU, 8 GB RAM and 50 GB hard drive. Create user 'forgerock', this user shall be used for all operations in this guide. 
2. The server hosting OpenIG should have internet connectivity as first request tries to download required jars from maven repo. The custom groovy script uses @Grab and it downloads the required dependencies under <User-Home>/.groovy/grapes.
3. Copy binaries for OpenIG, OpenDJ and OpenAM to ~/softwares directory.
4. Copy Install scripts from https://github.com/CharanMann/OpenIG-SampleCompany/tree/master/installScripts to ~/softwares directory. 
5. Specify below local host enteries (both on server hosting and client accessing these applications): <br />
   [IP Address]  openig1.sc.com  # OpenIG, Port:9000 <br />
   [IP Address]  openig2.sc.com  # OpenIG, Port:9002 <br />
   [IP Address]  opendj.sc.com   # OpenDJ IS, Port 1389 <br />
   [IP Address]  openam13.sc.com # OpenAM, Port:8080 <br />
   [IP Address]  employees.sc.com # Internal Employee App, Port:8002 <br />
   [IP Address]  employees-ig.sc.com  # Internal Employee App via OpenIG, Port:9000 <br />
   [IP Address]  customers.sc.com  # External Customer App, Port:8004 <br />
   [IP Address]  customers-ig.sc.com # External Customer App  via OpenIG, Port:9000 <br />
   [IP Address]  apis.sample.com # API server, Port:8010 <br />
   [IP Address]  apis-ig.sample.com  # API server via OpenIG, Port:9002 <br />
   [IP Address]  travel.sample.com  # Internal Travel App, Port:8012 <br />
   [IP Address]  travel-ig.sample.com # Internal Travel App via OpenIG, Port:9002 <br />
   [IP Address]  benefits.sample.com # Internal Benefits App, Port:8014 <br />
   [IP Address]  benefits-ig.sample.com # Internal Benefits App via OpenIG, Port:9002
6. Install and configure SampleCompany application on same or different server: refer https://github.com/CharanMann/SampleCompany  
  
   
OpenIG Installation & Configuration:
====================================
1. Install two OpenIG 4 instances on Apache Tomcat under /opt/forgerock/OpenIG1 and /opt/forgerock/OpenIG2 respectively. Refer https://backstage.forgerock.com/#!/docs/openig/4/gateway-guide#install
2. Change port number for Apache Tomcat for OpenIG1 to 9000 and OpenIG2 to 9002 respectively.  
3. Specify config directories for each OpenIG instance by specifying OPENIG_BASE. e.g for OpenIG1 specify "export OPENIG_BASE=/home/forgerock/.openig1" in /opt/forgerock/OpenIG1/bin/setenv.sh
4. Create logs directory for each OpenIG instance. e.g for OpenIG1 create /home/forgerock/.openig1/logs
5. Copy OpenIG configurations. e.g for OpenIG1 copy https://github.com/CharanMann/OpenIG-SampleCompany/tree/master/openig1 to /home/forgerock/.openig1 
6. Specify CORS filter for OpenIG2 in /opt/forgerock/OpenIG2/conf/web.xml. Refer https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CORS_Filter for sample CORS filter template.
   CORS filter params: <br />
   ============= <br />
   * url-pattern: /history/*
   * cors.allowed.origins: http://employees-ig.sc.com:9000
   * cors.allowed.methods: GET,POST,HEAD,OPTIONS,PUT
   * cors.allowed.headers: Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization
   * cors.exposed.headers: Access-Control-Allow-Origin,Access-Control-Allow-Credentials
   * cors.support.credentials: false
   * Leave default value for rest of parameters
7. Start both OpenIG instances. Verify there are no errors in Apache Tomcat and OpenIG logs. 

OpenDJ Identity Store Installation & Configuration:
===================================================
1. Install OpenDJ 3 under /opt/forgerock/opendjis. Refer https://backstage.forgerock.com/#!/docs/opendj/3/install-guide#command-line-install <br />
   Setup params: <br />
   ============= <br />
   * LDAP Listener Port:            1389
   * Administration Connector Port: 4444
   * JMX Listener Port:
   * LDAP Secure Access:            disabled
   * Root User DN:                  cn=Directory Manager
   * Password                       cangetindj
   * Directory Data:                Backend Type: JE Backend
                                    Create New Base DN dc=sample,dc=com
   * Base DN Data: Only Create Base Entry (dc=sample,dc=com)

OpenAM Installation & Configuration:
====================================
1. Install OpenAM 13 under /opt/forgerock/OpenAM-Server. Refer https://backstage.forgerock.com/#!/docs/openam/13/install-guide#configure-openam-custom <br />
2. Navigate to http://openam13.sc.com:8080/openam for OpenAM configuration. <br />
   Setup params: <br />
   ============= <br />
   * amAdmin password: cangetinam
   * Server Setting:
     * Server URL: http://openam13.sc.com:8080/openam
     * Cookie Domain: .sc.com
     * Configuration Directory: /home/forgerock/openam   
   * Configuration Store Details 
     * SSL/TLS Enabled: No
     * Host Name: localhost
     * Listening Port: 50389
     * Root Suffix: dc=openam,dc=forgerock,dc=org
     * User Name: cn=Directory Manager
   * User Store Details
     * User Data Store Type: OpenDJ
     * SSL/TLS Enabled: No 
     * Host Name: opendj.sc.com
     * Listening Port: 1389 
     * Root Suffix: dc=sample,dc=com
     * User Name: cn=Directory Manager
     * Password: cangetindj
   * Site Configuration Details: This instance is not setup behind a load balancer
   * Default Policy Agent password: cangetinwa 
3. Stop OpenDJ Identity store and import identity data using: ./import-ldif --includeBranch dc=sample,dc=com --backendID userRoot --ldifFile ~/softwares/installScripts/sample.ldif
4. Install SSO Admin Tools under /opt/forgerock/OpenAM-Tools. 
5. Install patch: 12321-1-tpatch for SSO Admin Tools. Copy openam-auth-fr-oath-13.0.0.jar file from the deployed OpenAM Server war file into the lib directory in the OpenAM Tools home:
   cp /opt/forgerock/OpenAM-Server/webapps/openam/WEB-INF/lib/openam-auth-fr-oath-13.0.0.jar /opt/forgerock/OpenAM-Tools/lib
6. Import OpenAM service configs : 
   * Execute command: ./ssoadm import-svc-cfg -u amadmin -f /tmp/pwd.txt -e password -X ~/softwares/installScripts/openam-13.xml
   * Directory Service contains existing data. Do you want to delete it? [y|N] y
   * Check for any errors. Check if all service configurations have been imported successfully. 
   * Note that no OpenAM policies shall appear in any realm, this is due to bug: https://bugster.forgerock.org/jira/browse/OPENAM-8169 
7. Enable CORS filter for OpenAM. Refer https://backstage.forgerock.com/#!/docs/openam/13/install-guide/chap-prepare-install#enable-cors-support 
   CORS filter params: <br />
   ============= <br />
   * url-pattern: /json/*
   * methods: POST,GET,PUT,DELETE,PATCH,OPTIONS
   * origins: *
   * allowCredentials: false
   * headers: Accept,Accept-Encoding,Accept-Language,Authorization,Cookie,Connection,Content-Length,Content-Type,iPlanetDirectoryPro,Host,Origin,User-Agent,X-OpenAM-Username,X-OpenAM-Password,X-Requested-With
   * expectedHostname: openam13.sc.com:8080
   * Leave default value for rest of parameters

OpenIG Use Cases testing:
========================= 
1. OpenIG-OpenAM PEP for web applications
2. OpenIG-OpenAM PEP for REST APIs
3. OpenIG-OAuth2 RS
4. OpenIG-SAML SP
5. OpenIG-Credentials Replay-OpenAMAgent: Not yet implemented
6. OpenIG-Credentials Replay-File-DB: Not yet implemented
7. OpenIG-OIDC RP
8. OpenIG-UMA RS: Not yet implemented

SampleCompany URLs :
===========================
1. CommonServices direct: http://apis.sample.com:8010/history/emp1 <br />
   CommonServices via OpenIG: http://apis-ig.sample.com:9002/history/emp1
2. EmployeeApp direct: http://employees.sc.com:8002/employeeApp/#/ <br />
   EmployeeApp via OpenIG: http://employees-ig.sc.com:9000/employeeApp/#/
3. CustomerApp direct: http://employees.sc.com:8002/employeeApp/#/ <br />
   CustomerApp via OpenIG: http://employees-ig.sc.com:9000/employeeApp/#/
4. TravelApp direct: http://travel.sample.com:8012/travelApp/#/ <br />
   TravelApp via OpenIG: http://travel-ig.sample.com:9002/travelApp/#/
5. BenefitsApp direct: http://benefits.sample.com:8014/benefitsApp/#/ <br />
   BenefitsApp via OpenIG:: http://benefits-ig.sample.com:9002/benefitsApp/#/

* * *

The contents of this file are subject to the terms of the Common Development and
Distribution License (the License). You may not use this file except in compliance with the
License.

You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
specific language governing permission and limitations under the License.

When distributing Covered Software, include this CDDL Header Notice in each file and include
the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
Header, with the fields enclosed by brackets [] replaced by your own identifying
information: "Portions copyright [year] [name of copyright owner]".

Copyright 2016 Charan Mann
