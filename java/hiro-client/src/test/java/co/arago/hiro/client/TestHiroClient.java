  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.api.TimeseriesValue;
import co.arago.hiro.client.builder.ClientBuilder;
import co.arago.hiro.client.builder.TokenBuilder;
import co.arago.hiro.client.util.DefaultTimeseriesValue;
import co.arago.hiro.client.util.HiroCollections;
import co.arago.hiro.client.util.HiroException;
import co.arago.hiro.client.util.Helper;
import co.arago.hiro.client.util.Listener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author fotto
 */
public class TestHiroClient {

  static int port = 12345;
    static FakeHiroServer server;
    private final String testApiURL;
    private final HiroClient client;

    public TestHiroClient() {
      testApiURL = "http://127.0.0.1:" + Integer.toString(port);

      client = new ClientBuilder().setRestApiUrl(testApiURL).setTokenProvider(new TokenBuilder().makeFixed("52f5ae960afdfdde8459a7f414739d7")).setTrustAllCerts(true).makeHiroClient();
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        server = new FakeHiroServer(port);
    }

    @AfterClass
    public static void tearDownClass() {
        server.stop();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreateUpdateReplace() {
        Map in = new HashMap();
        in.put("attr1", "value1");
        in.put("attr2", "value2");
        System.out.println("send to update: "+Helper.composeJson(in));
      Map out = client.createVertex("fakeType", in, HiroCollections.newMap());
        System.out.println("response after create: "+Helper.composeJson(out));

        assertEquals(in.get("attr1"), out.get("attr1"));

        String id = (String) out.get("ogit/_id");
        System.out.println("ID of created vertex: "+id);
        in.put("attr2", "value2x");
        System.out.println("send to update: "+Helper.composeJson(in));
      out = client.updateVertex(id, in, HiroCollections.newMap());
        System.out.println("response after update: "+Helper.composeJson(out));
        assertEquals(id, out.get("ogit/_id"));
        assertEquals(in.get("attr2"), out.get("attr2"));

      in.put("attr1", null);
        in.put("attr2", "value2y");
      System.out.println("send to update: " + Helper.composeJson(in));
      out = client.updateVertex(id, in, HiroCollections.newMap());
      System.out.println("response after update: " + Helper.composeJson(out));
        assertFalse(out.containsKey("attr1"));
        assertEquals(id, out.get("ogit/_id"));
        assertEquals(in.get("attr2"), out.get("attr2"));
    }

    @Test
    public void testGetItemFailure() throws Exception {
      try
      {
        Map out = client.getVertex("not-there", HiroCollections.newMap());
        System.out.println(Helper.composeJson(out));
        fail("the item should not exist");
      } catch(HiroException t) {
        assertEquals(404, t.getCode());
      }
    }

    @Ignore
    @Test
    public void testTimeseries() throws Exception {

      HiroClient client = new ClientBuilder().setRestApiUrl("https://graph.stage.graphit.co/").setTokenProvider(new TokenBuilder().makeFixed("52f5ae960afdfdde8459a7f414739d7")).setTrustAllCerts(true).makeHiroClient();

      Map v = HiroCollections.newMap();
      v = client.createVertex("ogit/Timeseries", v, HiroCollections.newMap());

      String vId = (String) v.get("ogit/_id");
      assertNotNull(vId);
      final List<TimeseriesValue> tsValues = HiroCollections.newList();
      tsValues.add(new DefaultTimeseriesValue(1444728897461L, "3"));

      client.updateTsValues((String) vId, tsValues);
      assertEquals(1, tsValues.size());

      List<TimeseriesValue> tsValues1 = client.getTsValues(vId, 1444728897461L, System.currentTimeMillis());
      assertEquals(1, tsValues1.size());
      //https://openautopilot.tech.arago.de:8443/bd0aeade-d513-4fc9-887e-188eeae6286a/values?from=0&limit=-1&offset=0&order=ogit%2F_id%20asc&fields=ogit%2F_id%2Cogit%2F_type&query=%2B(ogit%5C%2F_type%3Aogit%5C%2FOntologyEntity)&_TOKEN=1f5aecab2a2394d5aede7a5899ef6ce
    }

    @Test
    public void testEdges() throws Exception {
    }

    @Ignore
    @Test
    public void testContent() {
      System.out.println("before getContent");
      client.getContent("abcd");
    }

    @Test
    public void testHistoryEvents() throws Exception {
      final List<Map> entries = new ArrayList<>();

      Map<String,String> params = new HashMap<>();
      params.put("from", (System.currentTimeMillis() - 10000000) + "");
      params.put("to", System.currentTimeMillis() + "");

      client.historyEvents(params, new Listener<Map>() {

        @Override
        public ListenerState process(Map entry) {
          entries.add(new TreeMap(entry));
          System.err.println("XXX: " + entry);
          return Listener.ListenerState.OK;
        }

        @Override
        public void onException(Throwable t) {
          t.printStackTrace();
          assert(false);
        }

        @Override
        public void onFinish() {
          //blank
        }
      });

      assertEquals(3, entries.size());

      String expected = "{\"body\":{\"ogit\\/_created-on\":1502437236523,\"\\/marsNodeID\":\"{\\\"ids\\\":[\\\"test3:test3:Machine:HIRO_AllInOne\\\"]}\",\"ogit\\/status\":\"FULFILLED\",\"ogit\\/_id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"\\/formular\":\"{\\\"title\\\":\\\"Get Hostname\\\",\\\"attributes\\\":{\\\"Get\\\":\\\"Hostname\\\"},\\\"nodeid\\\":[\\\"test3:test3:Machine:HIRO_AllInOne\\\"]}\",\"ogit\\/_creator\":\"mglusiuk@arago.de\",\"\\/issue-xmlns\":\"https:\\/\\/graphit.co\\/schemas\\/v2\\/IssueSchema\",\"ogit\\/_graphtype\":\"vertex\",\"ogit\\/_owner\":\"mglusiuk@arago.de\",\"ogit\\/_v\":1,\"ogit\\/_is-deleted\":false,\"ogit\\/_modified-by-app\":\"Frontend openautopilot\",\"ogit\\/_creator-app\":\"Frontend openautopilot\",\"ogit\\/_modified-by\":\"mglusiuk@arago.de\",\"ogit\\/_version\":\"2.19.0.114\",\"ogit\\/_type\":\"ogit\\/Task\"},\"id\":\"cj67kc74g07ihdr74p0c3sn6i\",\"nanotime\":1390026310752627,\"timestamp\":1502437236523,\"identity\":\"mglusiuk@arago.de\",\"type\":\"CREATE\"}";

      assertEquals("cj67kc74g07ihdr74p0c3sn6i", entries.get(0).get("id"));
      assertEquals("cj67kc74g07ihdr74p0c3sn6i", ((Map)entries.get(0).get("body")).get("ogit/_id"));
  }

  @Ignore
  @Test
  public void testWsEvents() throws Exception {
    Map params = HiroCollections.newMap();
    params.put("timeout", "60000");
    params.put("filter", "(element.ogit/_type = *)");
    client.getEventStream(params, new Listener<String>() {
      @Override
      public Listener.ListenerState process(String entry) {
        System.out.println("data: " + entry);
        return Listener.ListenerState.OK;
      }
    }, new Listener<String>() {
      @Override
      public Listener.ListenerState process(String entry) {
        System.out.println("log: " + entry);
        return Listener.ListenerState.OK;
      }
    });
  }
}
