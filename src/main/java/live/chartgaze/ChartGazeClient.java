package live.chartgaze;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ChartGaze by TrainFlow AI — Java client for MCP / HTTP market intelligence.
 */
public final class ChartGazeClient {

  public static final String DEFAULT_BASE_URL = "https://api.chartgaze.live";

  private final String apiKey;
  private final String baseUrl;
  private final HttpClient http;
  private final ObjectMapper mapper;

  public ChartGazeClient(String apiKey) {
    this(apiKey, DEFAULT_BASE_URL);
  }

  public ChartGazeClient(String apiKey, String baseUrl) {
    this.apiKey = Objects.requireNonNull(apiKey, "apiKey").trim();
    if (this.apiKey.isEmpty()) {
      throw new IllegalArgumentException("apiKey must not be blank");
    }
    String root = (baseUrl == null || baseUrl.isBlank()) ? DEFAULT_BASE_URL : baseUrl.trim();
    this.baseUrl = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
    this.mapper = new ObjectMapper();
  }

  public JsonNode listTools() throws IOException, InterruptedException {
    return getJson("/mcp/tools");
  }

  public JsonNode callTool(String toolName, Map<String, Object> toolInput)
      throws IOException, InterruptedException {
    Objects.requireNonNull(toolName, "toolName");
    Map<String, Object> input = toolInput == null ? Map.of() : toolInput;
    String q = URLEncoder.encode(toolName, StandardCharsets.UTF_8);
    String json = mapper.writeValueAsString(input);
    return postJson("/mcp/call?tool_name=" + q, json);
  }

  public JsonNode getMarketSnapshot(String symbol) throws IOException, InterruptedException {
    return callTool("get_market_snapshot", Map.of("symbol", symbol));
  }

  public JsonNode getMarketContext(String symbol) throws IOException, InterruptedException {
    return getMarketContext(symbol, true);
  }

  public JsonNode getMarketContext(String symbol, boolean includeEvents)
      throws IOException, InterruptedException {
    Map<String, Object> args = new LinkedHashMap<>();
    args.put("symbol", symbol);
    args.put("include_events", includeEvents);
    return callTool("get_market_context", args);
  }

  public JsonNode getHistoricalContext(String symbol, String isoTimestamp)
      throws IOException, InterruptedException {
    Map<String, Object> args = new LinkedHashMap<>();
    args.put("symbol", symbol);
    args.put("timestamp", isoTimestamp);
    return callTool("get_historical_context", args);
  }

  public JsonNode getEconomicCalendarWeek() throws IOException, InterruptedException {
    return callTool("get_economic_calendar_week", Map.of());
  }

  public JsonNode getTradingAccounts() throws IOException, InterruptedException {
    return callTool("get_trading_accounts", Map.of());
  }

  public JsonNode getUsageSummary() throws IOException, InterruptedException {
    return getJson("/usage/summary");
  }

  public JsonNode listAccounts() throws IOException, InterruptedException {
    return getJson("/accounts/list");
  }

  private JsonNode getJson(String path) throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(60))
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .GET()
            .build();
    return send(req);
  }

  private JsonNode postJson(String pathAndQuery, String jsonBody)
      throws IOException, InterruptedException {
    HttpRequest req =
        HttpRequest.newBuilder(URI.create(baseUrl + pathAndQuery))
            .timeout(Duration.ofSeconds(90))
            .header("Authorization", "Bearer " + apiKey)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
            .build();
    return send(req);
  }

  private JsonNode send(HttpRequest req) throws IOException, InterruptedException {
    HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    String body = res.body() == null ? "" : res.body();
    if (res.statusCode() >= 400) {
      throw new ChartGazeException(res.statusCode(), body);
    }
    if (body.isBlank()) {
      return mapper.nullNode();
    }
    return mapper.readTree(body);
  }

  public Map<String, Object> mapOf(String jsonObject) throws IOException {
    return mapper.readValue(jsonObject, new TypeReference<Map<String, Object>>() {});
  }
}
