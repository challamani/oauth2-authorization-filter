# Oauth2-authorization-filter
<p> Protecting the Restful resources by enabling the authorization checks, works based on an external JSON file.
The JSON file should contain the resource and scope mapping</p>


### Start the token authorization server (KEYCLOAK) 
```shell
docker run -d -p 8080:8080 -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=changeme \
  quay.io/keycloak/keycloak:19.0.3 start-dev
```

#### Access the KEYCLOAK admin page http://localhost:8080

#### Create the realm, and client-for-client credentials grant with specific scopes to the client
1. First let us create a new realm, click the `master` dropdown and select `Create Realm`
2. Enter the Realm name `oauth2-workshop` and select `Create`

Once the realm is created it creates an endpoint that describes what functionality exists from the provider.
You can see it at <localhost:8080/realms/oauth2-workshop/.well-known/openid-configuration> (if you called your realm something different replace the `oauth2-workshop`).
You should see the following endpoints:

1. Select `Clients` from the sidebar and press `Create client`
2. Enter the following information
* Client type: `OpenID Connect`
* Client ID: `ClientCredentialsApp`
* Name: `client credentials Application`
* Description: `An OAuth2 client application using the client credentials grant`
3. Select `Next`
4. Select `Save`

### Generate the access token
#### Copy the client# and secret from the client credentials session
```shell
CLIENT_ID=ClientCredentialsApp
CLIENT_SECRET=mo78isv40IQhW970aXSyKI7QS840wlDe
```

```shell
curl --location -X POST 'http://localhost:8080/realms/oauth2-workshop/protocol/openid-connect/token' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "client_id=$CLIENT_ID" \
--data-urlencode "client_secret=$CLIENT_SECRET" \
--data-urlencode 'grant_type=client_credentials' \
--data-urlencode 'scope=email hr-lead developer'
```

Generate the tokens with different scopes and try to access the endpoints with valid and invalid tokens.

```shell
curl --header "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:9090/test-authz-filter/employees
```

```shell
### Below curl shouldn't work with the bearer token that doesn't contain scopes that can delete an employee entity
curl --header "Authorization: Bearer $ACCESS_TOKEN" \
  -X DELETE  http://localhost:9090/test-authz-filter/employees/4
```

```shell
### This should work with all active bearer tokens
curl --header "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:9090/test-authz-filter/todos
```

#### An example of scope and resource binding
```shell
{
    "resourceId": ".*\/employees\/*",
    "methods": [
      "GET",
      "DELETE",
      "POST",
      "PUT"
    ],
    "scopes": [
      "hr-senior-associate",
      "admin",
      "onboarding-lead"
    ]
  }
```
