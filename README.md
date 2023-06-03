# Oauth2-authz-filter
<p> Protecting the Restful resources by enabling the authorization checks, works based on external json file.
the json file should contain the resource and scope mapping</p>


### Start the token issuer
```shell
docker run -d -p 8080:8080 -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=changeme \
  quay.io/keycloak/keycloak:19.0.3 start-dev
```

### Access the KEYCLOAK admin page http://localhost:8080

### Create the realm, client for client credentials grant and specific scopes to client
1. First lets create a new realm, click the `master` dropdown can select `Create Realm`
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
#### Copy the client# and secret from client credentials session
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

Generate the token with different scopes and try access the endpoints with valid and invalid token (token with different scope)
```shell
curl -v  http://localhost:9090/test-authz-filter/employees --header "Authorization: Bearer $ACCESS_TOKEN"

### Below curl shouldn't work with the token that doesn't contain claim-scopes which can delete employee
curl -v -X DELETE  http://localhost:9090/test-authz-filter/employees/4 --header "Authorization: Bearer $ACCESS_TOKEN"

### This should work with all active tokens
curl -v http://localhost:9090/test-authz-filter/todos --header "Authorization: Bearer $ACCESS_TOKEN"
```