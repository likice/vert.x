package org.nodex.core.stomp;

import org.nodex.core.buffer.Buffer;

import java.util.Map;

/**
 * User: tfox
 * Date: 01/07/11
 * Time: 15:58
 */
public interface StompMsgCallback {
  void onMessage(Map<String, String> headers, Buffer body);
}
