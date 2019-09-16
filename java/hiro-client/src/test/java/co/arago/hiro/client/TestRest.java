  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client;

import co.arago.hiro.client.builder.ClientBuilder;
import co.arago.hiro.client.builder.TokenBuilder;
import co.arago.hiro.client.api.RestClient;
import co.arago.hiro.client.api.TokenProvider;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author fotto
 */
public class TestRest {
    
    static int port = 12345;
    static FakeHiroServer server;
    private final String testApiURL;
    private final RestClient client;
  private final TokenProvider tp;
    
    public TestRest() {
        testApiURL = "http://127.0.0.1:"+Integer.toString(port);
        tp = new TokenBuilder().makeFixed("dGVzdEBhcmFnby5kZToweERFQURCRUVGCg==");
        client = new ClientBuilder().setRestApiUrl(testApiURL).setTokenProvider(tp).setTrustAllCerts(true).makeRestClient();
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
    public void testGet() {
//        List<String> path = Arrays.asList("sub1","subsub2");
//        Map<String,String> parms = new HashMap<>();
//        parms.put("parameter1", "value1");
//        String result = client.get(path, parms);
//        Map m = Helper.parseJsonBody(result);
//        assertEquals("get", m.get("method"));
    }
    
    @Test
    public void testPost() {
        
    }
    @Test
    public void testPostWithParams() {
        
    }
    @Test
    public void testPut() {
        
    }
    @Test
    public void testDelete() {
        
    }
    
    @Test
    public void testPostBinary() {
        
    }
    
    @Test
    public void testGetBinary() {
    }

}
