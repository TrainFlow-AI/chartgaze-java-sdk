package live.chartgaze;

/** HTTP / MCP error from ChartGaze. */
public final class ChartGazeException extends RuntimeException {
  private final int statusCode;
  private final String responseBody;

  public ChartGazeException(int statusCode, String responseBody) {
    super("ChartGaze HTTP " + statusCode + ": " + truncate(responseBody));
    this.statusCode = statusCode;
    this.responseBody = responseBody == null ? "" : responseBody;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getResponseBody() {
    return responseBody;
  }

  private static String truncate(String s) {
    if (s == null) return "";
    return s.length() > 500 ? s.substring(0, 500) + "…" : s;
  }
}
