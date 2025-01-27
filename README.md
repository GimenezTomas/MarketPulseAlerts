# Market Pulse Alerts

Market Pulse Alerts is a Spring Boot application designed to manage financial instrument subscriptions and notifications. The application allows users to subscribe to various financial instruments and receive alerts based on predefined thresholds.

## Features

- Retrieve financial instruments.
- Subscribe to financial instruments.
- Unsubscribe from financial instruments.
- Synchronize financial instruments with external APIs.
- Get subscribed financial instruments with detailed information.
- Scheduled job to notify users of price threshold breaches.

## API Documentation

You can access the API documentation via Swagger at the following URL:
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Database Setup

The application uses PostgreSQL as its database. Follow these steps to set up and create the database (skip this if you prefer to use docker):

1. **Install PostgreSQL**: Ensure that PostgreSQL is installed on your machine.

2. **Create the Database**:
    - Open your PostgreSQL command line or a database management tool (e.g., pgAdmin).
    - Run the following SQL command to create the database:
      ```sql
      CREATE DATABASE market_pulse_alerts;
      ```

3. **Configure Database User**:
    - Ensure that the specified username (`user`) and password (`password`) have the necessary permissions to access the newly created database.

## Changing the Notification Job Frequency

The application includes a scheduled job that runs every 3 minutes to notify subscribers if any of their subscribed instruments have breached the defined thresholds.

- **To change the frequency**, modify the `fixedRate` property in the `application.properties` file:
  ```properties
  scheduler.notifySubscribers.fixedRate=180000  # Time in milliseconds (180000 ms = 3 minutes)
  ```
You can adjust the value according to your requirements (e.g., setting it to 60000 for 1 minute).

## Running the Application

### Prerequisites

* Java 17 must be installed on your system.
* Docker must be installed and running on your system.

### Start the PostgreSQL Database

1. Run the following command in your terminal to start a detached PostgreSQL container named `mkt-pulse-postgres`:

```bash
docker run -d \
  --name mkt-pulse-postgres \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=market_pulse_alerts \
  -p 5432:5432 \
  postgres
```

### Running with IntelliJ

1. **Clone the Repository**:
   ```bash
   git clone ssh://git@github.com:example/market-pulse-alerts.git
   cd market-pulse-alerts
   ```

2. **Open IntelliJ**:
   - Create a new project from the existing sources and select the cloned `market-pulse-alerts` directory.

3. **Set Up Java 17**:
   - Ensure you have Java 17 configured in IntelliJ. You can check this under `File > Project Structure > Project`.

4. **Run the Application**:
   - Create a new Run Configuration:
      - Go to `Run > Edit Configurations > + > Application`.
      - Name: `Market Pulse Alerts`
      - Main class: `com.example.marketpulsealerts.MarketPulseAlertsApplication`
      - Program arguments: `--spring.profiles.active=local`
      - Use classpath of module: `market-pulse-alerts`
      - Click `OK`.

5. **Start the Application**:
   - Click the green play button in the toolbar to run the service's main method.

6. **Access the Application**:
   Internal Server Error

## External APIs

### 1. Crypto API
- **URL**: [CoinGecko](https://www.coingecko.com/)
- **Account**: {your account}
- **Plan**: Free (30 calls per minute, 10,000 per month)
- **API Key**: {your api key}
- **Documentation for API Key**: [CoinGecko API Documentation](https://www.coingecko.com/en/api/pricing)

#### Example Code
```java
   OkHttpClient client = new OkHttpClient();
   
   Request request = new Request.Builder()
     .url("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin%2Cethereum")
     .get()
     .addHeader("accept", "application/json")
     .addHeader("x-cg-demo-api-key", {"your api key"})
     .build();
   
   Response response = client.newCall(request).execute()
    .url("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&ids=bitcoin%2Cethereum")
    .get()
    .addHeader("accept", "application/json")
    .addHeader("x-cg-demo-api-key", {"your api key"})
    .build();
```
# RESPONSE:
```json
[
    {
        "id": "bitcoin",
        "symbol": "btc",
        "name": "Bitcoin",
        "image": "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400",
        "current_price": 92611,
        "market_cap": 1833435014671,
        "market_cap_rank": 1,
        "fully_diluted_valuation": 1943770656875,
        "total_volume": 65571789735,
        "high_24h": 95344,
        "low_24h": 91251,
        "price_change_24h": -2410.7575867847627,
        "price_change_percentage_24h": -2.53705,
        "market_cap_change_24h": -48773449319.56714,
        "market_cap_change_percentage_24h": -2.59129,
        "circulating_supply": 19807962,
        "total_supply": 21000000,
        "max_supply": 21000000,
        "ath": 108135,
        "ath_change_percentage": -14.27647,
        "ath_date": "2024-12-17T15:02:41.429Z",
        "atl": 67.81,
        "atl_change_percentage": 136602.91855,
        "atl_date": "2013-07-06T00:00:00.000Z",
        "roi": null,
        "last_updated": "2025-01-10T00:51:29.744Z"
    },
    {
        "id": "ethereum",
        "symbol": "eth",
        "name": "Ethereum",
        "image": "https://coin-images.coingecko.com/coins/images/279/large/ethereum.png?1696501628",
        "current_price": 3220.68,
        "market_cap": 387945875014,
        "market_cap_rank": 2,
        "fully_diluted_valuation": 387945875014,
        "total_volume": 30107598550,
        "high_24h": 3352.88,
        "low_24h": 3160.58,
        "price_change_24h": -112.7553956566303,
        "price_change_percentage_24h": -3.38256,
        "market_cap_change_24h": -13092288764.44983,
        "market_cap_change_percentage_24h": -3.2646,
        "circulating_supply": 120479619.4169329,
        "total_supply": 120479619.4169329,
        "max_supply": null,
        "ath": 4878.26,
        "ath_change_percentage": -33.85025,
        "ath_date": "2021-11-10T14:24:19.604Z",
        "atl": 0.432979,
        "atl_change_percentage": 745192.06739,
        "atl_date": "2015-10-20T00:00:00.000Z",
        "roi": {
            "times": 45.49916721397397,
            "currency": "btc",
            "percentage": 4549.916721397397
        },
        "last_updated": "2025-01-10T00:51:20.583Z"
    }
]
```

### 2. Market API
- **URL**: [Profit API](https://api.profit.com/#tag/Websockets)
- **Account**: {your account}
- **Plan**: Free (100 calls per day)
- **API Key**: {your account key}
- **Console**: [Profit Console](https://profit.com/es/settings/data-api)
- **Documentation for API Key**: [Profit API Documentation](https://api.profit.com/#section/Authentification)

## Fetch stocks
### API Endpoint
```
url: https://api.profit.com/data-api/reference/stocks?token={your_account_key}&skip=0&limit=10&country=United%20States&currency=USD&available_data=fundamental&type=Common%20Stock
```
#### Sample Response
```json
{
    "data": [
        {
            "ticker": "AAPL.US",
            "symbol": "AAPL",
            "name": "Apple Inc",
            "type": "Common Stock",
            "currency": "USD",
            "country": "United States",
            "exchange": "NASDAQ"
        },
        {
            "ticker": "AMZN.US",
            "symbol": "AMZN",
            "name": "Amazon.com Inc",
            "type": "Common Stock",
            "currency": "USD",
            "country": "United States",
            "exchange": "NASDAQ"
        }
        // Additional stock items...
    ],
    "total": 47016
}
```

## Fetch stock by symbol

Currently, this is the only method I found to fetch the price, one by one:

### API Endpoint
```
https://api.profit.com/data-api/market-data/quote/TSLA?token={your_api_key}
```

### Sample Response
```json
{
    "ticker": "TSLA.US",
    "name": "Tesla Inc",
    "symbol": "TSLA",
    "price": 392.93,
    "previous_close": 394.6,
    "daily_price_change": -1.670000000000016,
    "daily_percentage_change": -0.423213,
    "timestamp": 1736479841779,
    "asset_class": "STOCKS",
    "currency": "USD",
    "logo_url": "https://cdn.profit.com/logo/stocks/TSLA.US.png",
    "volume": 75699525.0,
    "broker": "NASDAQ",
    "ohlc_week": {
        "open": 393.26,
        "high": 402.49,
        "low": 387.4,
        "close": 394.94
    }
}
```