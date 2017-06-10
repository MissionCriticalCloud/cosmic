[![Build Status](https://beta-jenkins.mcc.schubergphilis.com/buildStatus/icon?job=cosmic/0001-cosmic-master-build)](https://beta-jenkins.mcc.schubergphilis.com/job/cosmic/job/0001-cosmic-master-build/)

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
Support for other hypervisors can be added if contributors can provide the infrastructure to test against.

Users can manage their cloud via Web interface, command line
tools, and/or a full-featured query based API.

## Getting Source Repository

Cosmic officials Git repository is located at:

    https://github.com/MissionCriticalCloud/cosmic

## Building from Source

Cosmic requires:
- Java 8
- Maven settings configured to use [Cosmic's Nexus repository](https://beta-nexus.mcc.schubergphilis.com) (see [Maven settings](#maven-settings) below)

In order to build Cosmic, you have to follow these steps:

    git clone https://github.com/MissionCriticalCloud/cosmic.git
    cd cosmic
    mvn clean install -P developer,systemvm

The steps above will build the essentials to get Cosmic management server working. Besides that, you will also need a hypervisor. See our [build stream configuration](https://beta-jenkins.mcc.schubergphilis.com) for more details.

This will run the UI and API:

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

## Links

Cosmic is a fork of Apache CloudStack and its API is mostly backwards compatible with CloudStack's API. So, all the documentation can be accessed from http://docs.cloudstack.apache.org.

[API documentation](http://apidoc.cosmiccloud.io/) for Cosmic is on a separate page.

## Getting Involved

Please, join our Slack channel for more details:

* [Mission Critical Cloud](https://missioncriticalcloud.slack.com)

If you want an invite, please e-mail `int-cloud@schubergphilis.com` and we'll welcome you on Slack soon.

## Development environment "The Bubble"

For ease of development, testing and evaluating we created a project called [The Bubble](https://github.com/MissionCriticalCloud/bubble-blueprint). The Bubble is a single host or VM, that hosts all the VMs to build a Cosmic Cloud. A special [Bubble Toolkit](https://github.com/MissionCriticalCloud/bubble-toolkit) project exists to automate common tasks. This is also where our CI scripts live.

## Submitting code

Feel free to open a Pull Request. Our [CI system](https://beta-jenkins.mcc.schubergphilis.com/job/cosmic/) will automatically kick-in and build a real cloud based on your branch. The test results will be reported on the Gighub Pull Request. Our policy is to only merge when Pull Requests builds are green. After merge, another build is started to verify it once in master.

## Reporting Security Vulnerabilities

If you've found an issue that you believe is a security vulnerability in a
released version of Cosmic, please report it to `int-cloud@schubergphilis.com` with details about the vulnerability, how it
might be exploited, and any additional information that might be useful.

## Sponsors

The Cosmic team would like to recognize and thank the contributions of the following entities to the success of this project and resulting system:
* [Schuberg Philis](https://www.schubergphilis.com): who is responsible for the birth of the Cosmic project (and all other related projects, see [vagrant-cloudstack](https://github.com/MissionCriticalCloud/vagrant-cloudstack) or [bubble-blueprint](https://github.com/MissionCriticalCloud/bubble-blueprint) for some examples) and resulting systems, and is relentlessly supporting all the development costs of Cosmic.
* [JetBrains](https://www.jetbrains.com): who's motto is "Create anything" and by providing our team with free access to their amazing development environment suite (IntelliJ IDEA - Ultimate edition) allows us to create Cosmic.

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
