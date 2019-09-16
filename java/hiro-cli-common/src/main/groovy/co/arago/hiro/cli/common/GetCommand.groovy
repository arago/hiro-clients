package co.arago.hiro.cli.common

import co.arago.hiro.cli.common.BaseCommand
import groovy.json.JsonBuilder
import groovy.cli.commons.CliBuilder

import co.arago.hiro.cli.util.QueryHelper
import co.arago.hiro.client.Hiro
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import static co.arago.hiro.client.api.OGITConstants.Attributes.OGIT__ID
import static co.arago.hiro.cli.common.Constants.*

@CompileStatic
class GetCommand extends BaseCommand {
    
    String outputFormat = 'json'
    def supportedFormats = ['json', 'json-compact']
    String outputDir
    def makeJsonArray
    
    Closure errorFilter = this.&filterErrorsFromListResult  // BaseCommand

    @CompileDynamic
    protected CliBuilder _getCliBuilder(String usage, includeGlobalSettings=true) {
        def cliBuilder = super._getCliBuilder(usage, includeGlobalSettings)
        cliBuilder._(longOpt: 'as-json-array', args: 0, 'return result as JSON array. Ignored if output/list format is not JSON or if --directory is used')
        cliBuilder.d(longOpt: 'directory', args: 1, argName: 'directory','save results into directory <directory>')
        def outFormatHelpMsg = "Format to use for content output: ${supportedFormats.join(', ')} (default: ${outputFormat})"
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
        if (options.d) {
            outputDir = options.d
        }
        if (options.'as-json-array') {
            makeJsonArray = true
        }
        if (options.'output-format') {
            outputFormat = options.'output-format'.toLowerCase()
            if (!supportedFormats.contains(outputFormat)) {
                sysExit(RC_BAD_OPTARGS, "output format \"${outputFormat}\" not supported")
            }
        }
        // refactor common stuff from GetFormalNode
        options
    }
    
    def _fileExtension(String outputFormat) {
        String result
        switch(outputFormat) {
            case ~/^json.*/: result = 'json'; break
            default: result = 'txt'
        }
        result
    }
    
    String formatRecord(Map m, String outputFormat) {
        String result
        switch (outputFormat) {
            case 'json-compact': result = new JsonBuilder(m).toString(); break
            case 'json': result = mapToPrettyJson(m); break
            default: result = m as String
        }
        result
    }
    
    def printHeader(String outputFormat) {
        if (outputDir || !outputFormat.startsWith('json')) {
            return
        }
        if (makeJsonArray) {
            println "["
        }
    }
    
    def printFooter(String outputFormat) {
        if (outputDir || !outputFormat.startsWith('json')) {
            return
        }
        if (makeJsonArray) {
            println "]"
        }
    }

    def printSeparator(String outputFormat, Boolean isFirst=Boolean.FALSE) {
        if (outputDir || !outputFormat.startsWith('json')) {
            return
        }
        if (makeJsonArray && !isFirst) {
            print ","
        }
    }

    def printPreformattedRecord(String id, String payload) {
        if (outputDir) {
            def dir = new File(outputDir)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            new File(outputDir, id.replaceAll(/\s/, '_')+'.'+_fileExtension(outputFormat)).withWriter('utf-8') { writer ->
                writer.writeLine(payload)
            }
        } else {
            println payload
        }    
    }
    
    def printRecord(String id, Map m, String outputFormat) {
        String data = formatRecord(m, outputFormat)
        printPreformattedRecord(id, data)
    }
    
}