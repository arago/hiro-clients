  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client;

import co.arago.hiro.client.api.HiroClient;
import co.arago.hiro.client.util.HiroCollections;
import java.util.HashMap;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Ignore
public class QueryTest {
    private static final String API = "http://localhost:8888/";

    public QueryTest() {
        // testing against an acutal graphit
    }


    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIdQuery() {
      HiroClient client = Hiro.newClient().setRestApiUrl(API).setTokenProvider(Hiro.newToken().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVG")).makeHiroClient();

      List result = client.idQuery(HiroCollections.newList("ogit/Node", "ogit/Attachment", "blorg"), new HashMap());
      assertEquals(3, result.size());
    }

    @Test
    public void testVerticesQuery() {
      HiroClient client = Hiro.newClient().setRestApiUrl(API).setTokenProvider(Hiro.newToken().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVG")).makeHiroClient();

      List result = client.vertexQuery("ogit\\/_id:$what", HiroCollections.newMap("what", "ogit/Node"));
      assertEquals(1, result.size());
    }

    @Test
    public void testGremlinSimple() {
      HiroClient client = Hiro.newClient().setRestApiUrl(API).setTokenProvider(Hiro.newToken().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVG")).makeHiroClient();

      List result = client.graphQuery("ogit/Node", "outE.inV", new HashMap());
      assertEquals(14, result.size());
    }

    @Test
    public void testGremlinWithMap() {
      HiroClient client = Hiro.newClient().setRestApiUrl(API).setTokenProvider(Hiro.newToken().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVG")).makeHiroClient();

      List result = client.graphQuery("ogit/Node", "outE.inV.map('ogit/_id')", new HashMap());
      System.err.println(result);
      assertEquals(14, result.size());
    }

     @Test
    public void testGremlinWithTree() {
      HiroClient client = Hiro.newClient().setRestApiUrl(API).setTokenProvider(Hiro.newToken().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVG")).makeHiroClient();

      List result = client.graphQuery("ogit/Node", "outE.inV.tree.cap", new HashMap());
      System.err.println(result);
      assertEquals(1, result.size());
    }
}
