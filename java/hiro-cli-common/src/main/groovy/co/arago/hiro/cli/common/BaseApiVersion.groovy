package co.arago.hiro.cli.common

import co.arago.hiro.cli.common.BaseCommand
import groovy.json.JsonOutput
import co.arago.hiro.client.api.HiroClient
import groovy.cli.commons.CliBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import static co.arago.hiro.cli.common.Constants.*

@CompileStatic
class BaseApiVersion extends BaseCommand {
    
    String outputFormat = 'txt'
    static List supportedFormats = ['json', 'txt']
    def outFormatHelpMsg = "Format to use for content output: ${supportedFormats.join(', ')} (default: ${outputFormat})"
     
    def cliVersions = [:] // to be set in subclasses

    @CompileDynamic
    protected CliBuilder _getCliBuilder(String usage, includeGlobalSettings=true) {
        def cliBuilder = super._getCliBuilder(usage, includeGlobalSettings)
        cliBuilder._(longOpt: 'output-format', args: 1, outFormatHelpMsg)
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
        super.parseArgs(args, globalSettings)
        if (options.'output-format') {
            outputFormat = options.'output-format'.toLowerCase()
            if (!supportedFormats.contains(outputFormat)) {
                sysExit(RC_BAD_OPTARGS, "output format \"${outputFormat}\" not supported")
            }
        }
        options
    }
    
    int run(args, globalSettings=null) {
        def cli 
        if (globalSettings) {
            cli = getCliBuilder("${command} [<specific options>]\n", false)
        } else {
            cli = getCliBuilder("${command} [<Options>]>\n", true)
        }
        
        def options = parseArgs(args, globalSettings)
        token = 'no_token_required'
        HiroClient client = getHiroClient()

        runSafe(exitOnError) {
            Map serverApiVersion = client.apiVersion().collectEntries { k, v  ->
                [k, (v as Map).version]
            }
            Integer apiVersMatches = 0
            if (outputFormat == 'txt') {
                apiVersMatches = printAsTxt(serverApiVersion)
            } else {
                apiVersMatches = printAsJson(serverApiVersion)
            }
            def plural = apiVersMatches == 1 ? '' : 'e'
            if (apiVersMatches > 0) {
                showCount(apiVersMatches, 'API Version mismatch'+plural, 'found between client and server')
                errorCount += 1
            } else if (apiVersMatches == 0 && debug > 0) {
                showCount(apiVersMatches, 'API Version mismatch'+plural, 'found between client and server')         
            } 
        }
        client.close()
        return errorCount ?  OTHER_ERROR : 0  
    }   
    
    // returns two element arrary: (no of mismatches, result Map)
    private List compareVersions(Map apiVersions) {
        Integer non_matching = 0
        Map result = [:]
        cliVersions.each { String api, cli_vers ->
            def api_vers = apiVersions[(api)] ?: 'unknown'
            result.put(api, ['server': api_vers, 'client': cli_vers])
            if (cli_vers != api_vers) {
                non_matching += 1
            }
        }
        return [non_matching, result]
    }
    Integer printAsTxt(Map apiVersions) {
        List result = compareVersions(apiVersions)
        result[1].each { api, vers ->
            println "'$api' API: server: ${vers['server']} client: ${vers['client']}"
        }
        result[0]
    }
    
    Integer printAsJson(Map apiVersions) {
        List result = compareVersions(apiVersions)
        print JsonOutput.toJson(result[1])
        result[0]
    }

}