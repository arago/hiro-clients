package co.arago.hiro.client.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public final class HiroCollections {

  public static Map newMap() {
    return new HashMap();
  }

  public static Map newMap(String... items) {
    final Map map = newMap();

    if ((items.length % 2) != 0) {
      throw new IllegalArgumentException("arguments need to be even");
    }

    for (int i = 0, len = items.length - 1; i < len; i += 2) {
      map.put(items[i], items[i + 1]);
    }

    return map;
  }

  public static Map newMap(Map data) {
    return new HashMap(data);
  }

  public static ConcurrentMap newConcurrentMap() {
    return new ConcurrentHashMap();
  }

  public static ConcurrentMap newConcurrentMap(Map data) {
    return new ConcurrentHashMap(data);
  }

  public static List newList() {
    return new ArrayList();
  }

  public static List newList(Iterable col) {
    final List ret = new ArrayList();
    for (final Object o : col) {
      ret.add(o);
    }

    return ret;
  }

  public static List newList(Object... items) {
    return new ArrayList(Arrays.asList(items));
  }

  public static Set newSet() {
    return new HashSet();
  }

  public static Set newSet(Collection col) {
    return new HashSet(col);
  }

  private HiroCollections() {
  }
}
