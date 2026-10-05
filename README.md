# ChartGaze by TrainFlow AI — Java SDK

**ChartGaze** is the market-intelligence MCP / SDK from **[TrainFlow AI](https://www.trainflow.dev)**.

| | |
|---|---|
| Site | https://chartgaze.live |
| Docs | https://www.chartgaze.live/docs |
| MCP | https://mcp.chartgaze.live |
| License | MIT · Java 17+ |

## Install

```bash
git clone https://github.com/TrainFlow-AI/chartgaze-java-sdk.git
cd chartgaze-java-sdk && mvn clean install
```

```xml
<dependency>
  <groupId>live.chartgaze</groupId>
  <artifactId>chartgaze-java-sdk</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Quick start

```java
ChartGazeClient client = new ChartGazeClient(System.getenv("CHARTGAZE_API_KEY"));
System.out.println(client.getMarketContext("XAUUSD", true));
```

See [docs](https://www.chartgaze.live/docs#sdks) for the full tool catalog.
