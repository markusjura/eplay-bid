# Bid Service
Stores the auction item data in a Mongo DB. Provides the following HTTP Endpoints:
- Retrieve all auction items which hasn't been expired
- Retrieve an image for a specific item
- Update the price of an auction item. Validates if
   - The price is higher than the current price
   - The auction has not been expired
- SSE Broadcast channel to retrieve an auction item update
- Re-initialize the DB with new randomly generated auction item

For each HTTP Endpoint an `Authorization` header need to be set with:
- The keyword `token`
- The *token* you retrieved from the `authService` during user log in

A valid `Authorization` header would look like:
```
token e2e272b2731ae9396cc9b27fce6d2c295de4c90a-1395219575936-user-1
```

More details can be found in the [REST API documentation](http://eplay-bid.markusjura.cloudbees.net)

#### How to start
The service is hosted on [http://eplay-bid.markusjura.cloudbees.net](http://eplay-bid.markusjura.cloudbees.net). You can use your local
`bidService` by:

1. Installing mongo
    ```
    $ brew install mongo
    ```

2. Starting mongo
    ```
    $ mongod
    ```

3. Changing the `service.bid.url` in the *eplay application.conf* to your URL
    ```
    service.bid.url="http://localhost:9002"
    ```

4. Starting the local `bidService`
    ```
    $ cd bidService
    $ activator
    > start 9002
    ```