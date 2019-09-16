/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.cli.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fotto
 */
public class QueryHelper {
    
    private final static String PLUS = "+";
    private final static String MINUS = "-";
    
    /*
       produces a ES query string from (multi)map
    
       keys may start with '+' or '-'. If not a '+' is assumed
    
       if value is a list then multiple query parts will be produced.
    
    Example: input  {"+ogit/_type": "abcd", "-ogit/status": [ 'X', 'Y']}
    
    will be translated into query
    
       +ogit/_type:abcd AND -ogit/status:X AND -ogit/status:Y
    
    NOTE: it doesn't make any sense to have a '+' key with a list of value
    
    NOTE: any kind of OR is not supported, yet.
    */
    // see https://github.com/arago/hiro-graph-js/tree/master/packages/hiro-graph-lucene 
    // for more ideas how to build queries
    public static String fixedValueEsQuery(final Map m) {
        
        List<String> tmp = new ArrayList<>();
        for (Object key: m.keySet()) {
            String newKey = ((String) key).replaceAll("\\/", "\\\\/");
            String indicator = newKey.substring(0, 1);
            if (!PLUS.equals(indicator) && !MINUS.equals(indicator)) {
                newKey = PLUS + newKey;
            }
            Object oKey = m.get(key);
            if (oKey instanceof String) {
                String newValue = ((String) m.get(key));
                tmp.add(newKey + ":\"" + newValue + "\"");
            } else if (oKey instanceof List) {
                for (Object el: (List) oKey) {
                    String newValue = ((String) el);
                    tmp.add(newKey+":\""+newValue+"\"");
                }
            }
        }
        String result = String.join(" AND ", tmp);
        return result;
  }

  // "query":"(ogit\\/_type: ogit\\/Automation\\/*) AND (ogit\\/name: HIRO_E*)"
  public static String wildCardEsQuery(final Map m) {
    List<String> tmp = new ArrayList<>();
    for (Object key : m.keySet()) {
      String newKey = ((String) key).replaceAll("\\/", "\\\\/");
      String newValue = ((String) m.get(key)).replaceAll("\\/", "\\\\/").replaceAll(" ", "\\\\ ");
      newValue = newValue.replaceAll(":", "\\\\:");
      tmp.add("(" + newKey + ": " + newValue + ")");
    }
    String result = String.join(" AND ", tmp);
    return result;
  }
}
