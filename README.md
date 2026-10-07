# ChartGaze by TrainFlow AI — Java SDK

**ChartGaze** is the market-intelligence MCP / SDK from **[TrainFlow AI](https://www.trainflow.dev)** — give any AI live multi-TF structure, news, and your trading accounts.

> **Want the AI to trade for you?** ChartGaze is context + accounts.  
> For full autonomous scan → alert → execute on MT4 / MT5 / cTrader / crypto, use **[TrainFlow](https://www.trainflow.dev)** (Nexus / Hunter).  
> Same family: [chartgaze.live](https://chartgaze.live) · [trainflow.dev](https://www.trainflow.dev)

| | |
|---|---|
| Product | ChartGaze by TrainFlow AI |
| Site | https://chartgaze.live |
| Docs | https://www.chartgaze.live/docs |
| MCP connector URL | https://api.chartgaze.live/mcp |
| API / SDK base | https://api.chartgaze.live |
| Org | https://github.com/TrainFlow-AI |
| Autonomous trading | https://www.trainflow.dev |
| License | MIT |
| Language | Java 17+ |

Sibling SDKs: [Python](https://github.com/TrainFlow-AI/chartgaze-python-sdk) · [Go](https://github.com/TrainFlow-AI/chartgaze-go-sdk)

---

## Overview

The **ChartGaze Java SDK** is a small HTTP client for JVM services (Spring, Quarkus, plain Java). It talks to the same ChartGaze API / MCP host that Claude and ChatGPT use.

ChartGaze = **eyes** for your AI. For **hands** (autonomous execution), go to **[TrainFlow](https://www.trainflow.dev)**.

### Key use cases

- **Backend agents** — Spring/Quarkus jobs that need multi-TF context before acting
- **Bots & research** — scheduled gold / FX / crypto context pulls
- **Account-aware services** — list connected MT4 / MT5 / cTrader / crypto accounts
- **Hand-off to TrainFlow** — prototype with ChartGaze, run autonomy on TrainFlow

For chat UIs prefer remote MCP (`https://api.chartgaze.live/mcp`) — no Java required.

---

## Installation

### From source (current)

```bash
git clone https://github.com/TrainFlow-AI/chartgaze-java-sdk.git
cd chartgaze-java-sdk
mvn clean install
```

### Maven

```xml
<dependency>
  <groupId>live.chartgaze</groupId>
  <artifactId>chartgaze-java-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle (Kotlin DSL)

```kotlin
implementation("live.chartgaze:chartgaze-java-sdk:0.1.0")
```

**Requirements:** Java 17+, Maven 3.8+ (Jackson databind is pulled transitively).

---

## Quick start

### 1. Basic market query

```java
import live.chartgaze.ChartGazeClient;
import live.chartgaze.ChartGazeException;
import com.fasterxml.jackson.databind.JsonNode;

public class Main {
  public static void main(String[] args) throws Exception {
    String key = System.getenv("CHARTGAZE_API_KEY");
    ChartGazeClient client = new ChartGazeClient(key);

    JsonNode snapshot = client.getMarketSnapshot("EURUSD");
    System.out.println(snapshot);

    JsonNode context = client.getMarketContext("XAUUSD", true);
    System.out.println(context);
  }
}
```

### 2. Discover tools + accounts

```java
JsonNode tools = client.listTools();
System.out.println(tools);

JsonNode accounts = client.getTradingAccounts();
System.out.println(accounts);

JsonNode restAccounts = client.listAccounts(); // GET /accounts/list
System.out.println(restAccounts);

JsonNode usage = client.getUsageSummary();
System.out.println(usage);
```

### 3. Historical “time machine”

```java
JsonNode hist = client.getHistoricalContext(
    "XAUUSD",
    "2026-09-15T14:30:00Z"
);
System.out.println(hist);
```

### 4. Calendar

```java
JsonNode week = client.getEconomicCalendarWeek();
System.out.println(week);
```

### 5. Arbitrary MCP tool

```java
import java.util.Map;

JsonNode news = client.callTool(
    "get_market_news",
    Map.of("symbol", "XAUUSD", "limit", 5, "hours_back", 24)
);
System.out.println(news);
```

### 6. Local MCP during development

```java
ChartGazeClient local = new ChartGazeClient(
    System.getenv("CHARTGAZE_API_KEY"),
    "http://localhost:8000"
);
```

---

## Authentication

Pass a ChartGaze API key or OAuth access token. Every request sends:

```http
Authorization: Bearer <token>
```

```bash
export CHARTGAZE_API_KEY=cg_pk_live_...
```

Get keys / credits from the [ChartGaze dashboard](https://www.chartgaze.live/dashboard).

---

## API surface (implemented)

| Method | Backend |
|--------|---------|
| `listTools()` | `GET /mcp/tools` |
| `callTool(name, args)` | `POST /mcp/call?tool_name=` |
| `getMarketSnapshot(symbol)` | MCP `get_market_snapshot` |
| `getMarketContext(symbol[, includeEvents])` | MCP `get_market_context` |
| `compareMarkets(symbols)` | MCP `compare_markets` |
| `getMarketNews(symbol, limit, hoursBack)` | MCP `get_market_news` |
| `getHistoricalContext(symbol, isoTimestamp)` | MCP `get_historical_context` |
| `getEconomicCalendarWeek()` | MCP `get_economic_calendar_week` |
| `getTradingAccounts()` | MCP `get_trading_accounts` |
| `proposeTrade(accountId, symbol, side, volume, sl, tp)` | MCP `propose_trade` (account required) |
| `executeTrade(...)` | MCP `execute_trade` (Live mode only) |
| `listAccounts()` | `GET /accounts/list` |
| `getUsageSummary()` | `GET /usage/summary` |

Full MCP catalog: [docs](https://www.chartgaze.live/docs#tools). Free tier = **10 calls** (1 credit each). Paid tools may cost 2–3 credits.

---

## Error handling

```java
try {
  client.getMarketSnapshot("XAUUSD");
} catch (ChartGazeException e) {
  System.err.println("HTTP " + e.getStatusCode());
  System.err.println(e.getResponseBody());
  // 401 → bad/missing auth
  // 400 → tool / quota / validation error
}
```

---

## Spring Boot sketch

```java
@Configuration
public class ChartGazeConfig {
  @Bean
  ChartGazeClient chartGazeClient(
      @Value("${chartgaze.api-key}") String apiKey,
      @Value("${chartgaze.base-url:https://api.chartgaze.live}") String baseUrl
  ) {
    return new ChartGazeClient(apiKey, baseUrl);
  }
}

@RestController
@RequestMapping("/internal/market")
public class MarketController {
  private final ChartGazeClient chartGaze;

  public MarketController(ChartGazeClient chartGaze) {
    this.chartGaze = chartGaze;
  }

  @GetMapping("/{symbol}/context")
  public JsonNode context(@PathVariable String symbol) throws Exception {
    return chartGaze.getMarketContext(symbol, true);
  }
}
```

---

## Trading note

Order placement is **off by default**. Users enable **read-only / confirm / live** under Dashboard → Your AIs. Prefer **confirm** so proposed trades wait for an Approve click (TrainFlow Nexus-style). This SDK does not bypass those controls.

For full autonomous desks use **[TrainFlow](https://www.trainflow.dev)**.

---

## MCP for Claude / ChatGPT

No Java needed — paste `https://api.chartgaze.live/mcp` as a custom connector, or use [chartgaze.live/agents](https://www.chartgaze.live/agents) (Boardy-style “Open in Claude / ChatGPT / Grok” buttons).

---

## Package layout

```
src/main/java/live/chartgaze/
  ChartGazeClient.java      # HTTP + MCP helpers
  ChartGazeException.java   # status + body
pom.xml
README.md
LICENSE
```

---

## Configuration

| Env / ctor | Default | Meaning |
|------------|---------|---------|
| `CHARTGAZE_API_KEY` | — | Bearer token |
| base URL ctor arg | `https://api.chartgaze.live` | MCP / REST host |

---

## Testing

```bash
mvn -q -DskipTests package
export CHARTGAZE_API_KEY=...
# run a small Main against api.chartgaze.live or localhost:8000
```

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `401` / missing authorization | Set `CHARTGAZE_API_KEY` or OAuth bearer |
| Free tier limit | Top up at `/dashboard/billing` |
| Connection refused to localhost | Start MCP on `:8000` or use production base URL |
| Empty accounts | Connect brokers in `/dashboard/accounts` — market tools still work without accounts |

---

## Contributing

PRs welcome under the TrainFlow AI org. Keep the client thin (HTTP + JSON); heavy domain models can land in later versions.

---

## Support

- Docs: https://www.chartgaze.live/docs  
- Agents: https://www.chartgaze.live/agents  
- Email: support@chartgaze.live  
- TrainFlow autonomy: https://www.trainflow.dev  

MIT © TrainFlow AI
