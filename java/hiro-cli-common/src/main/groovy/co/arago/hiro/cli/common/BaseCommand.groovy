package co.arago.hiro.cli.common

import co.arago.hiro.client.Hiro
import co.arago.hiro.admin.client.HiroAdmin
import co.arago.hiro.admin.client.builder.AdminClientBuilder
import co.arago.hiro.client.api.HiroClient
import co.arago.hiro.admin.client.api.HiroIamClient
import co.arago.hiro.admin.client.api.HiroAppClient
import co.arago.hiro.client.builder.TokenBuilder
import groovy.json.JsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import java.util.logging.Level
import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__XID
import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__ID
import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__CREATED_ON
import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__MODIFIED_ON
import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__DELETED_ON
import co.arago.hiro.cli.common.Config
import co.arago.hiro.cli.util.QueryHelper
import co.arago.hiro.cli.common.PrintHelper
import static co.arago.hiro.cli.common.Constants.*
import groovy.cli.commons.CliBuilder

@CompileStatic
class BaseCommand {
    static boolean USE_CUSTOM_PRETTY_PRINT = true
    static def TIMESTAMP_FIELDS = [OGIT__CREATED_ON, OGIT__MODIFIED_ON, OGIT__DELETED_ON]
    
    CliBuilder cli
    String cfgFile = Config.defaultPropFile
    String url = "http://localhost:8888"
    String token
    Boolean trustAllCerts = false
    Level debugRest = null
    HiroClient client = null
    HiroIamClient iamClient = null
    HiroAppClient appClient = null
    Integer debug
    Integer timeoutSecs = 60
    def options
    Integer errorCount = 0
    Boolean exitOnError = true
    Boolean batchMode = false

    @Synchronized
    public void incrementErrorCount() {
        errorCount += 1
    }

    // property access to command
    def getCommand() {
        System.getProperty("co.arago.hiro.cli.CommandName")
    }

    void setCommand(String command) {
        System.setProperty("co.arago.hiro.cli.CommandName", command)
    }

    @CompileDynamic
    protected CliBuilder _getCliBuilder(String usage, includeGlobalSettings=true) {
        def cliBuilder = new CliBuilder(usage: usage, header: 'Options', posix: false)
        cliBuilder.h(longOpt: 'help', args: 0, 'show usage')
        if (includeGlobalSettings) {
            cliBuilder.u(longOpt: 'url', args: 1, argName: 'URL', 'HIRO Graph REST API URL')
            cliBuilder.T(longOpt: 'token', args: 1, argName: 'token', "access token to authorize operations. Overrides env var ${TOKEN_ENV_VAR}")
            cliBuilder._(longOpt: 'trust-all-certs', args: 0, 'skip SSL certificate check')
            cliBuilder._(longOpt: 'timeout', args: 1, 'timeout (seconds) for long running queries')
            cliBuilder._(longOpt: 'debug', args: 1, argName: 'debugLevel', 'set debug level')
            cliBuilder._(longOpt: 'http-debug', args: 0, argName: 'httpDebug', 'enable debugging of HTTP layer')
            cliBuilder._(longOpt: 'batch-mode', args: 0, 'minimal output to stdout (e.g. only IDs during creation)')
            cliBuilder._(longOpt: 'cfg-file', args: 1, argName: 'cfg-file', "path to file containing predefined settings. default=${cfgFile}")
        }
        cliBuilder
    }
    
    CliBuilder getCliBuilder(String usage, includeGlobalSettings=true) {
        if (!cli) {
            cli = _getCliBuilder(usage, includeGlobalSettings)
        }
        cli
    }

    @CompileDynamic
    def parseArgs(args, globalSettings=null) {
        if (!cli) {
            throw new IllegalStateException("CliBuilder not initialized. forgot to call getCliBuilder()?")
        }
        options = cli.parse(args)
        if (!options) {
            sysExit(RC_BAD_OPTARGS)
        }
        assert options != null

        if (options.h) {
            cli.usage()
            sysExit(RC_SUCCESS)
        }
        if (globalSettings) {
            url = globalSettings['url']
            token = globalSettings['token']
            trustAllCerts = globalSettings['trustAllCerts']
            debugRest = globalSettings['debugRest']
            batchMode = globalSettings['batchMode']
            debug = globalSettings['debug']
            timeoutSecs = globalSettings['timeout']
        } else {
            if (options.'cfg-file') {
                cfgFile = options.'cfg-file'
            }
            def cfg = new Config(filePath: cfgFile)
            if (options.u) {
                url = options.u
            } else if ('api_url' in cfg) {
                url = cfg.api_url
            }
            if (!options.T && !(TOKEN_ENV_VAR in System.getenv()) ) {
                sysExit(RC_BAD_OPTARGS, "Either env var ${TOKEN_ENV_VAR} must be set or option -T/--token is required")
            }
            if (options.T) {
                token = options.T
            } else {
                if (TOKEN_ENV_VAR in System.getenv()) {
                    token = System.getenv()[(TOKEN_ENV_VAR)]
                } else {
                    throw new IllegalStateException("Token not set")
                }
            }
            if (options."trust-all-certs") {
                trustAllCerts = true
            } else if ('trust_all_certs' in cfg) {
                trustAllCerts = cfg.trust_all_certs
            }
            if (options."timeout") {
                timeoutSecs = options."timeout" as Integer
            } else if ('timeout' in cfg) {
                timeoutSecs = cfg.timeout as Integer
            }
            if (options."http-debug") {
                debugRest = Level.INFO
            }
            if (options."batch-mode") {
                batchMode = true
            }
            if (options.debug) {
                try {
                    debug = options.debug as Integer
                } catch (NumberFormatException) {
                    debug = 1
                }
            }
        }
        options
    }
    
    HiroClient getHiroClient() {
        if(!client) {
            if (token) {
                client = Hiro.newClient().setRestApiUrl(url).
                setTokenProvider(new TokenBuilder().makeFixed(token)).
                setTrustAllCerts(trustAllCerts).setDebugRest(debugRest).
                setTimeout(timeoutSecs * 1000).makeHiroClient()
            } else {
                throw new RuntimeException("Construction of HiroClient without specified token not implemented, yet")
            }
        }
        if (!client instanceof HiroClient) {
            throw new IllegalStateException("client of of wrong type (${client.getClass().name}). requested type: HiroClient")
        }
        client
    }

    HiroIamClient getHiroIamClient() {
        if(!iamClient) {
            if (token) {
                iamClient =(HiroAdmin.newClient().setRestApiUrl(url).
                setTokenProvider(new TokenBuilder().makeFixed(token)).
                setTrustAllCerts(trustAllCerts).setDebugRest(debugRest).
                setTimeout(timeoutSecs * 1000) as AdminClientBuilder).makeHiroIamClient()
            } else {
                throw new RuntimeException("Construction of HiroIamClient without specified token not implemented, yet")
            }
        }
        iamClient
    }
    
    HiroAppClient getHiroAppClient() {
        if(!appClient) {
            if (token) {
                appClient = (HiroAdmin.newClient().setRestApiUrl(url).
                setTokenProvider(new TokenBuilder().makeFixed(token)).
                setTrustAllCerts(trustAllCerts).setDebugRest(debugRest).
                setTimeout(timeoutSecs * 1000) as AdminClientBuilder).makeHiroAppClient()
            } else {
                throw new RuntimeException("Construction of HiroAppClient without specified token not implemented, yet")
            }
        }
        appClient
    }
    
    def runSafe(Boolean exitOnError, Closure c) {
        def result = null
        try {
            result = c.call()
        } catch (Throwable ex) {
            if (debug >=1 ) {
                ex.printStackTrace(System.err)
            }
            if(exitOnError) {
                client?.close()
                iamClient?.close()
                appClient?.close()
                sysExit(OTHER_ERROR, ex.message)
            } else {
                showError(ex.message)
            }
        }
        result
    }
    
    void sysExit(Integer rc, String message="") {
        client?.close()
        iamClient?.close()
        appClient?.close()
        if (message) {
            System.err.println(message)
        }

        System.exit(rc)
    }
    
    void showWarning(String message) {
        System.err.println("WARNING: " + message)
    }
    
    static void showError(String message) {
        System.err.println("ERROR: " + message)
    }
    
    void showCount(Integer count, String countName, String message = '') {
        System.err << count + ' ' + countName + (count == 1 ? '' : 's') + \
        (message ? ' ' : '') + message +'\n'
    }
   
    // ony for GetCommand and QueryCommand
    
    def printCount(Integer count, String nodeType = 'Item') {
        if (batchMode) {
            System.out << count << '\n'
        } else {
            System.out << count << ' ' << nodeType << (count == 1 ? '' : 's') << ' found\n'
        }
    }  
  
    def showSummary(Integer count, String nodeType = 'Item') {
        System.err << count << ' ' << nodeType << (count == 1 ? '' : 's') << ' found\n'
    }

    def showUndeployed(Integer count, String nodeType = 'Item') {
        System.err << count << ' undeployed ' << nodeType << (count == 1 ? '' : 's') << ' found\n'
    }

    // translate and ogit/_xid into a list of ogit/_id 
    // works only for HiroClient
    def getVertexId(HiroClient client, String xid, Boolean returnAsList=true) {
        if (!client instanceof HiroClient) {
            throw new IllegalStateException("client of of wrong type (${client.getClass().name}). expected: HiroClient")
        }        
        def result = []
        def params = [:]
        def exitOnError = false
        runSafe(exitOnError) {
            client.xidQuery(xid, params).each { m ->
                result.add(m[(OGIT__ID)])
            }
        }
        if (debug >= 4) {
            System.err << "query for _xid=${xid} returned ${result.size()} result"+(
                result.size() == 1 ? "":"s")+"\n"
        }
        if (returnAsList) {
            return result
        } else {
            // return scalar: _id if unique. null otherwise
            if (result.size() == 1) {
                result[0]
            } else {
                if (result.size() > 1) {
                    showError("Found more than one vertex with "+
                        OGIT__XID+"=${xid}. Manual cleanup required. IDs found: "+
                        result.join(" "))
                }
                return null
            }
        }
    }

    Map setHumanReadableTimes(Map m) {
        TIMESTAMP_FIELDS.each { field ->
            if (m.containsKey(field)) {
                m[(field)] = new Date(m[(field)] as String).toString()
                        // .format("yyyy-MM-dd HH:mm:ss.SSSZ")
            }
        }
        return m
    }

    // used in various get commands:
    def filterErrorsFromListResult(Map m) {
        if (m.containsKey(ERROR_KEY)) {
            Map error = m.get(ERROR_KEY)
            showError("Error ${error.code} when retrieving vertex ${error[(OGIT__ID)]}: ${error.message}")
            incrementErrorCount()
            false
        } else {
            true
        }
    }

    String mapToPrettyJson(Map m) {
        if (USE_CUSTOM_PRETTY_PRINT) {
            return PrintHelper.toJsonString(m)
        } else {
            return new JsonBuilder(m).toPrettyString()
        }
    }
}
