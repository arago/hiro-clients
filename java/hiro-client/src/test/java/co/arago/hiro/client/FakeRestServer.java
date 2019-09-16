/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.arago.hiro.client;

import co.arago.hiro.client.util.Helper;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fotto
 */
public class FakeRestServer extends NanoHTTPD {
    private static final String FAILURE_MSG = "operation failed";
    
    private static Level defaultLevel = Level.INFO;
    private static final Logger LOG = Logger.getLogger(FakeRestServer.class.getName());
    
    
    public FakeRestServer(int port) throws IOException {
        super(port);
        
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Map<String, String> headers = session.getHeaders();
        Map<String, String> parms = session.getParms();
        String name = session.getMethod().name();
        Map<String,Object> result = new HashMap<>();
        result.put("uri", uri);
        result.put("method", name);
        result.put("headers", headers);
        result.put("params", parms);
        
        String response = Helper.composeJson(result);
        System.out.println("R="+response);
        return newFixedLengthResponse(response);
    } 
}
