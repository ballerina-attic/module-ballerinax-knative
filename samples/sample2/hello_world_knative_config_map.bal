// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/config;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerinax/knative;

@knative:Service {}
listener http:Listener helloWorldEP = new(8080, {
    secureSocket: {
        keyStore: {
            path: "./security/ballerinaKeystore.p12",
            password: "ballerina"
        },
        trustStore: {
            path: "./security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

@knative:ConfigMap {
    conf: "./conf/ballerina.conf",
    configMaps:[
        {
            mountPath: "/home/ballerina/data",
            data: ["./conf/data.txt"]
        }
    ]
}

@http:ServiceConfig {
    basePath: "/helloWorld"
}

service helloWorld on helloWorldEP {
    @http:ResourceConfig {
        methods: ["GET"],
        path: "/config/{user}"
    }

    resource function getConfig(http:Caller outboundEP, http:Request request, string user) {
        http:Response response = new;
        string userId = getConfigValue(user, "userid");
        string groups = getConfigValue(user, "groups");
        string payload = "{userId: " + userId + ", groups: " + groups + "}";
        response.setTextPayload(payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }

    }

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/data"
    }
    resource function getData(http:Caller outboundEP, http:Request request) {
        http:Response response = new;
        string payload = <@untainted> readFile("./data/data.txt");
        response.setTextPayload("Data: " + <@untainted> payload + "\n");
        var responseResult = outboundEP->respond(response);
        if (responseResult is error) {
            log:printError("error responding back to client.", responseResult);
        }
    }
}

function getConfigValue(string instanceId, string property) returns string {
    string key = <@untainted> instanceId + "." + <@untainted> property;
    return config:getAsString(key, "Invalid User");
}

function readFile(string filePath) returns @tainted string {
    io:ReadableByteChannel bChannel = checkpanic io:openReadableFile(filePath);
    io:ReadableCharacterChannel cChannel = new io:ReadableCharacterChannel(bChannel, "UTF-8");

    var readOutput = cChannel.read(50);
    if (readOutput is string) {
        return readOutput;
    } else {
        return "Error: Unable to read file";
    }
}
