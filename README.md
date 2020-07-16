# Ballerina Knative Extension
 
Annotation based Knative extension implementation for ballerina. 

[![Travis (.org)](https://img.shields.io/travis/ballerina-platform/module-ballerinax-kantive.svg?logo=travis)](https://travis-ci.org/ballerina-platform/module-ballerinax-knative)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-knative/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-knative)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**Refer [samples](samples) for more info.**

## Supported Annotations:

### @knative:Service{}
- Supported with ballerina services, listeners and functions.


|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|dockerHost|Docker host IP and docker PORT|`"unix:///var/run/docker.sock"` for Unix.`"npipe:////./pipe/docker_engine"` for Windows 10|
|dockerCertPath|Docker certificate path.|"DOCKER_CERT_PATH" environment variable|
|registry|Docker registry url|null|
|username|Username for docker registry|null|
|password|password for docker registry|null|
|baseImage|Base image for docker image building|"ballerina/thin-base::<BALLERINA_VERSION>"|
|image|Docker image name with tag.|Default is `"<OUTPUT_FILE_NAME>:latest"`|
|buildImage|Docker image to be build or not|`true`|
|push|Enable pushing docker image to registry|`false`|
|copyFiles|Array of External files for docker image|null|
|singleYAML|Generate a single yaml file with all kubernetes artifacts|true|
|namespace|Kubernetes namespace to be used on all artifacts|default|
|replicas|Number of replicas|`1`|
|livenessProbe|Enable/Disable liveness probe and configure it|`false`|
|readinessProbe|Enable/Disable readiness probe and configure it.|`false`|
|imagePullPolicy|Image pull policy.|`"IfNotPresent"`|
|env|Environment variable map for containers|null|
|podAnnotations|Map of annotations for pods|null|
|podTolerations|Toleration for pods|null|
|dependsOn|Services this deployment depends on|null|
|imagePullSecrets|Image pull secrets|null|
|containerConcurrency|concurent request handle by one container instance|null|
|timeoutSeconds|max time the instance is allowed for responding to a request|null|
|port|containerPort value for Knative service|ballerina service port|

### @knative:ConfigMap{}
- Supported with ballerina services and functions.

|**Annotation Name**|**Description**|**Default value**|
|--|--|--|
|name|Name config map mount|<BALLERINA_SERVICE_NAME>-config-map|
|mountPath|Path to mount on container|null|
|readOnly|Is mount read only|true|
|defaultMode|Default permission mode|null|
|data|Paths to data files|null|

## How to build

1. Download and install JDK 8 or later
2. Install Docker
3. Get a clone or download the source from this repository (https://github.com/ballerinax/kubernetes)
4. Run the Gradle command ``gradle build`` from within the ``module-ballerinax-knative`` directory.
5. Copy ``knative-extension/build/libs/knative-extension-***.jar`` file to ``<BALLERINA_HOME>/bre/lib`` directory.
6. Copy ``knative-extension-annotation/target/caches/bir-cache/*`` folder to ``<BALLERINA_HOME>/bir-cache/`` directory.

### Enabling debug logs
- Use the "BAL_DOCKER_DEBUG=true" environment variable to enable docker related debug logs when building the ballerina
source(s).
- Use the "BAL_KUBERNETES_DEBUG=true" environment variable to enable kubernetes related debug logs when building the 
ballerina source(s).

## Deploy ballerina service directly using `kubectl` command.
This repository also provides a kubectl plugin which allows to build ballerina programs and deploy their kubernetes 
artifacts directly to a kuberetes cluster. The plugin is located at `kubernetes-extension/src/main/resources/kubectl-extension/kubectl-ballerina-deploy`.
Follow the steps mentioned in "[Extend kubectl with plugins](https://kubernetes.io/docs/tasks/extend-kubectl/kubectl-plugins/)"
. Check if the plugin is available using the command `kubectl plugin list`.

## Replacing values with environment variables.
You can replace values in an annotation using environment variables. The replacement is done with a string placeholder 
like `"$env{ENV_VAR}"`. As an example lets say that you want to set the `namespace` field in the @kubernetes:Deployment{} 
annotation with a environment variable and the name of the environment variable is `K8S_NAMESPACE`. Following is how the
 annotation would look like:
```ballerina
@knative:Service {
    namespace: "$env{K8S_NAMESPACE}"
}
```  
Note: You cannot use the `ballerina/config` module to replace values in the annotation. This is because the kubernetes 
artifacts are generated during compile time. The `ballerina/config` module works in the runtime. 
 
### How to execute:
```bash
$> kubectl ballerina deploy hello_world_kantive.bal
Compiling source
	hello_world_knative.bal

Generating executables
	hello_world_knative.jar

Generating Knative artifacts...

	@knative:Service 			 - complete 1/1
	@knative:Docker 			 - complete 2/2

	Execute the below command to deploy the Knative artifacts:
	kubectl apply -f /home/sample/kubernetes/knative
```

### Annotation Usage Sample:

```ballerina
```ballerina
import ballerina/http;
import ballerina/log;
import ballerinax/knative;

@knative:Service {
    name:"hello"
}
listener http:Listener helloEP = new(9090);

@http:ServiceConfig {
    basePath: "/helloWorld"
}
service helloWorld on helloEP {
    resource function sayHello(http:Caller caller, http:Request request) {
        var responseResult = caller->respond("Hello, World from service helloWorld ! ");
        if (responseResult is error) {
            log:printError("error responding", responseResult);
        }
    }
}
```

The Knative artifacts will be created in following structure.
```bash
$> tree 
.
├── docker
│   └── Dockerfile
├── hello_world_knative.bal
├── hello_world_knative.jar
└── kubernetes
    └── knative
        └── hello_world_knative.yaml
```
