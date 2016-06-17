# Cosmic
Cosmic is open source software designed to deploy and manage large
networks of virtual machines, as a highly available, highly scalable
Infrastructure as a Service (IaaS) cloud computing platform. Cosmic
provides an on-premises (private) cloud offering, or as part of a
hybrid cloud solution.

Cosmic is a turnkey solution that includes the entire "stack" of features
most organizations want with an cloud: compute orchestration,
Network-as-a-Service, user and account management, a full and open native API,
resource accounting, and a first-class User Interface (UI).

Cosmic currently supports the following hypervisors:
KVM and XenServer.
Support for other hypervisors can be added if contributors can provide the infrastructure to test agains.

Users can manage their cloud via Web interface, command line
tools, and/or a full-featured query based API.

## Getting Source Repository

Cosmic officials Git repository is located at:

    https://github.com/missioncriticalcloud/cosmic

## Building from Source

Cosmic requires:
- Java 8
- GitHub account with an [enabled SSH key] (https://help.github.com/articles/adding-a-new-ssh-key-to-your-github-account/) because you need to clone over SSH to get all the submodules
- Maven settings configured to use [Cosmic's Nexus repository](https://beta-nexus.mcc.schubergphilis.com) (see [Maven settings](#maven-settings) bellow)

In order to build Cosmic, you have to follow the steps below:

    git clone --recursive git@github.com:MissionCriticalCloud/cosmic.git
    cd cosmic
    mvn clean install -P developer,systemvm

The steps above will build the essentials to get Cosmic management server working. Besides that, you will also need a hypervisor. See our [build stream configuration](https://beta-jenkins.mcc.schubergphilis.com) for more details.

    cd cosmic-client
    mvn -pl :cloud-client-ui jetty:run

Go to your brouwser and type: [http://localhost:8080/client] (http://localhost:8080/client)

### Maven settings

Configure maven to look for artefacts in [Cosmic's Nexus repository](https://beta-nexus.mcc.schubergphilis.com):

```vim ~/.m2/settings.xml ```

```xml
<settings>
  <mirrors>
     <mirror>
      <!--This sends everything else to /public -->
      <id>beta-nexus</id>
      <mirrorOf>*</mirrorOf>
      <url>https://beta-nexus.mcc.schubergphilis.com/content/groups/public</url>
    </mirror>
  </mirrors>
  <profiles>
      <profile>
        <id>beta-nexus</id>
        <!--Enable snapshots for the built in central repo to direct -->
        <!--all requests to nexus via the mirror -->
        <repositories>
          <repository>
            <id>central</id>
            <url>http://central</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
          </repository>
        </repositories>
       <pluginRepositories>
          <pluginRepository>
            <id>central</id>
            <url>http://central</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
          </pluginRepository>
        </pluginRepositories>
      </profile>
    </profiles>
</settings>
```
Either enable the `beta-nexus` profile on the command line, or make it enabled by default in your settings file.
```xml
...
  <activeProfiles>
    <!--make the profile active all the time -->
    <activeProfile>beta-nexus</activeProfile>
  </activeProfiles>
...
```

## Building RPM Packages

In order to build Cosmic RPM packages, please refer to the [Packaging repository](https://github.com/MissionCriticalCloud/packaging) README section.

## Links

Cosmic is a fork of Apache CloudStack and its API is backwards compatible with CloudStack's API. So, all the documentation can be accessed from:

* [Documentation](http://docs.cloudstack.apache.org)
* [API documentation](http://cloudstack.apache.org/docs/api)

## Getting Involved

Please, join our Slack channel for more details:

* [Mission Critical Cloud](https://missioncriticalcloud.slack.com)

## Reporting Security Vulnerabilities

If you've found an issue that you believe is a security vulnerability in a
released version of Cosmic, please report it to `int-cloud@schubergphilis.com` with details about the vulnerability, how it
might be exploited, and any additional information that might be useful.

## License

Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

Please see the [LICENSE](LICENSE) file included in the root directory
of the source tree for extended license details.

## Notice of Cryptographic Software

This distribution includes cryptographic software. The country in which you currently
reside may have restrictions on the import, possession, use, and/or re-export to another
country, of encryption software. BEFORE using any encryption software, please check your
country's laws, regulations and policies concerning the import, possession, or use, and
re-export of encryption software, to see if this is permitted. See http://www.wassenaar.org/
for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has
classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which
includes information security software using or performing cryptographic functions with
asymmetric algorithms. The form and manner of this Apache Software Foundation distribution
makes it eligible for export under the License Exception ENC Technology Software
Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section
740.13) for both object code and source code.

The following provides more details on the included cryptographic software:

* Cosmic makes use of JaSypt cryptographic libraries
* Cosmic has a system requirement of MySQL, and uses native database encryption functionality.
* Cosmic makes use of the Bouncy Castle general-purpose encryption library.
* Cosmic can optionally interacts with and controls OpenSwan-based VPNs.
* Cosmic has a dependency on and makes use of JSch - a java SSH2 implementation.
