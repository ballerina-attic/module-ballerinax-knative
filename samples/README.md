## Ballerina Knative samples


### Prerequisites
 1. Install a recent version of Docker for Mac/Windows and [enable Kubernetes](https://docs.docker.com/docker-for-mac/#kubernetes) OR
    [Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/) is installed and running.
 2. [Nginx backend and controller deployed](#setting-up-nginx).
 3. Mini-kube users should configure following annotations in every sample with valid values: 
    ```bash
    @kubernetes:Deployment {
        dockerHost:"tcp://192.168.99.100:2376", 
        dockerCertPath:"/Users/anuruddha/.minikube/certs"
    }
    ```
 4. Docker for windows users should enable remote access to the API.
 (If DOCKER_HOST and DOCKER_CERT_PATH are exported as environment variables, priority will be given to environment variables.)
 ![alt tag](./images/docker_for_windows.png)
    
 5. [Heapster](https://github.com/kubernetes/heapster) monitoring configured.
    (This is **optional** and required only if Horizontal Pod Autoscaler are used. If you are running on GCE, heapster monitoring will be turned-on by default.)
    
    
#### Setting up nginx

1. Execute the below command to deploy nginx ingress controller.
##### Docker for mac users:
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/nginx-0.27.0/deploy/static/mandatory.yaml
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/nginx-0.27.0/deploy/static/provider/cloud-generic.yaml
```
2. Execute the below command to enable ssl pass-through.
```bash
kubectl patch deployments -n ingress-nginx nginx-ingress-controller -p '{"spec":{"template":{"spec":{"containers":[{"name":"nginx-ingress-controller","args":["\/nginx-ingress-controller","--configmap=$(POD_NAMESPACE)\/nginx-configuration","--tcp-services-configmap=$(POD_NAMESPACE)\/tcp-services","--udp-services-configmap=$(POD_NAMESPACE)\/udp-services","--publish-service=$(POD_NAMESPACE)\/ingress-nginx","--annotations-prefix=nginx.ingress.kubernetes.io","--annotations-prefix=nginx.ingress.kubernetes.io","--enable-ssl-passthrough"]}]}}}}'
```

##### minikube users:
```bash
minikube addons enable ingress
```

2. Verify nginx is up and running. (Namespace can be different based on you installation)
```bash
$> kubectl get pods --all-namespaces
NAMESPACE       NAME                                         READY     STATUS    RESTARTS   AGE
nginx-ingress   default-http-backend-69c767b879-l6pwj        1/1       Running   0          3d
nginx-ingress   nginx-5667df56c8-8d8ct                       1/1       Running   0          3d
```

## Try Knative annotation samples:

1. [Sample1: Knative Hello World](sample1/)
1. [Sample2: Knative Config Map](sample2/)