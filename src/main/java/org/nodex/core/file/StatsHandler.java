package org.nodex.core.file;

import java.util.Map;

/**
 * User: tfox
 * Date: 07/07/11
 * Time: 12:20
 */
public interface StatsHandler {
  void onStats(Map<String, String> stats);
}
