package co.arago.hiro.cli.common

import co.arago.hiro.client.builder.TokenBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized
import groovyjarjarcommonscli.CommandLine
import groovyjarjarcommonscli.CommandLineParser
import groovyjarjarcommonscli.Options

import co.arago.hiro.cli.common.Config
import static co.arago.hiro.cli.common.Constants.*
import java.util.logging.Level
import groovy.cli.commons.CliBuilder

@CompileStatic
class BaseCommandWrapper {
        
    def String mainCommand = 'hiro'
    def commands = [:] // must be set in sub-class
    def command_help = "" // will initialized during run-time

    String cfgFile = Config.defaultPropFile    
    CliBuilder cli
    def generalSettings = ['debug': 0] // have something there 

    @CompileDynamic
    int run(args) {
        command_help = '\nCommands:\n' + commands.collect { String k, Map v -> k+ ' '*(FIELD_LENGTH-k.length()) + v.desc }.join("\n")
        def helperClass
        
        cli = new CliBuilder(usage: mainCommand+' [<global options>] {help|<command> {help|<subcommand> <specific options and arguments>}}\n' + command_help, \
            header: '\nGlobal Options:', posix: false)
        cli.h(longOpt: 'help', args: 0, 'show general help')
        cli._(longOpt: 'version', args: 0, 'show version and exit')
        cli.u(longOpt: 'url', args: 1, argName: 'URL', 'HIRO Graph REST API URL')        
        cli.T(longOpt: 'token', args: 1, argName: 'token', "access token to authorize operations. Overrides env var ${TOKEN_ENV_VAR}")
        cli._(longOpt: 'batch-mode', args: 0, 'minimal output to stdout (e.g. only IDs during creation)')
        cli._(longOpt: 'trust-all-certs', args: 0, 'skip SSL certificate check')
        cli._(longOpt: 'timeout', args: 1, 'timeout (seconds) for long running queries')
        cli._(longOpt: 'debug', args: 1, argName: 'debugLevel', 'set debug level')
        cli._(longOpt: 'http-debug', args: 0, argName: 'httpDebug', 'enable debugging of HTTP layer')
        cli._(longOpt: 'cfg-file', args: 1, argName: 'cfg-file', "path to file containing predefined settings. default=${cfgFile}")
        
        cli.stopAtNonOption = true 
        def options = cli.parse(args)
        if (!options) {
            showGeneralHelp("bad args/opts")
        }
        if(options.h) {
            showGeneralHelp()
        }
        if(options.version) {
            showVersion()
        }
        if(!options.arguments()) {
            // command missing
            showGeneralHelp("command missing")
        }
        
        ArrayList unparsedArgs = options.arguments()
        def command = unparsedArgs[0]
        unparsedArgs.remove(0)
        switch (command) {
            case 'help':
                showGeneralHelp()
                break
            case 'version':
                showVersion()
                break
            case 'api-version':
                showApiVersion(options, unparsedArgs)
                break
            case 'commands': // hidden in usage
                def result = commands.collect { k,v -> k }.findAll { k -> 
                    k != 'help' && k != 'version' && k != 'api-version'
                }
                print result.join('\n')
                System.exit(RC_SUCCESS)
                break
            case commands:
                // process details below
                break
            default:
                showGeneralHelp("Invalid command: ${command}")
                break
        }

        def domainHelper = commands[(command)]['class'].newInstance()
        domainHelper.command = command
        domainHelper.mainCommand = mainCommand
        def runnerClass = domainHelper.parseSubCommand(unparsedArgs)
        def copyArgs
        def runner
        if (unparsedArgs[1] && unparsedArgs[1] in ['help', '-help', '--help']) {
            copyArgs = ['--help']
            runner = runnerClass.newInstance()
            runner.command = "${mainCommand} [<general options>] ${command} ${unparsedArgs[0]} "
            helperClass = domainHelper.getHelperClass(unparsedArgs[0])
            if (helperClass) {
                runner.helperClass = helperClass
            }
            runner.run(copyArgs, generalSettings) // will exit with usage
        }
        
        // process global options
        if (options.'cfg-file') {
            cfgFile = options.'cfg-file'
        }
        def cfg = new Config(filePath: cfgFile)
        generalSettings = cfg.getMappedConfig()
        if (!options.T && !(TOKEN_ENV_VAR in System.getenv()) ) {
            sysExit(RC_BAD_OPTARGS, "Either env var ${TOKEN_ENV_VAR} must be set or option -T/--token is required")
        }
        if (options.T) {
            generalSettings['token'] = options.T
        } else {
            if (TOKEN_ENV_VAR in System.getenv()) {
                generalSettings['token'] = System.getenv()[(TOKEN_ENV_VAR)]
            } else {
                throw new IllegalStateException("Token not set")
            }
        }
        if (options.u) {
            generalSettings['url'] = options.u
        } else if ('api_url' in cfg) {
            generalSettings['url'] = cfg.api_url
        }
        if (options."http-debug") {
            generalSettings['debugRest'] = Level.INFO
        }
        if (options."batch-mode") {
            generalSettings['batchMode'] = true
        }
        if (options.debug) {
            try {
                generalSettings['debug'] = options.debug as Integer
            } catch (NumberFormatException) {
                generalSettings['debug'] = 1
            }
        }
        if (options."trust-all-certs") {
            generalSettings['trustAllCerts'] = true
        }
        if (options."timeout") {
            generalSettings['timeout'] = options."timeout" as Integer
        }

        // unparsedArgs still comtains sub_command
        copyArgs = unparsedArgs.drop(1)
        runner = runnerClass.newInstance()
        runner.command = "${mainCommand} [<general options>] ${command} ${unparsedArgs[0]} "
        helperClass = domainHelper.getHelperClass(unparsedArgs[0])
        // some commands (KI/MARS) need a helper class to be set
        if (helperClass) {
            runner.helperClass = helperClass
        }
        if (generalSettings['debug'] >= 4) { // use different level here?
            System.err << "Run with settings:\n"
            System.err << "  Command:       ${command}\n"
            System.err << "  SubCommand:    ${unparsedArgs[0]}\n"
            //System.err << "  Token:         "+generalSettings['token']+"\n"
            System.err << "  URL:           "+generalSettings['url']+"\n"
            System.err << "  trustAllCerts: "+generalSettings['trustAllCerts']+"\n"
            System.err << "  debugRest:     "+generalSettings['debugRest']+"\n"
            System.err << "  batchMode:     "+generalSettings['batchMode']+"\n"
            if (helperClass) {
                System.err << "  helperClass:   "+helperClass.name+"\n"
            }
            System.err << "\n"
        }
        Integer rc = runner.run(copyArgs, generalSettings)
        sysExit(rc)
        
        
        // TODO: return error if something goes wrong
        return rc
    }
    
    def showGeneralHelp(String msg='') {
        if (msg) {
            println msg+"\n"
        }
        cli.usage()
        if (msg) {
            System.exit(RC_BAD_OPTARGS)
        } else {
            System.exit(RC_SUCCESS)
        }
    }
    
    def showVersion() {
        def version = this.getClass().getResource( '/cli.version' ).text.trim()
        println version
        System.exit(RC_SUCCESS)
    }

    @CompileDynamic
    def showApiVersion(options, ArrayList unparsedArgs) {
        def command = 'api-version'
        //print "command=$command class=${commands[(command)]['class']}"
        def runner = commands[(command)]['class'].newInstance()
        // improve: factor out common argument handling
        def cfg = new Config(filePath: cfgFile)
        generalSettings = cfg.getMappedConfig()
        if (options.u) {
            generalSettings['url'] = options.u
        } else if ('api_url' in cfg) {
            generalSettings['url'] = cfg.api_url
        }
        if (options."http-debug") {
            generalSettings['debugRest'] = Level.INFO
        }
        if (options.debug) {
            try {
                generalSettings['debug'] = options.debug as Integer
            } catch (NumberFormatException) {
                generalSettings['debug'] = 0
            }
        }
        if (options."trust-all-certs") {
            generalSettings['trustAllCerts'] = true
        }
        if (unparsedArgs[0] == 'help') {
            unparsedArgs[0] = '--help'
        }
        runner.command = "${mainCommand} [<general options>] ${command}"
        Integer rc = runner.run(unparsedArgs, generalSettings)
        sysExit(rc)
    }   
    
    void sysExit(Integer rc, String message="") {
        if (message) {
            System.err.println(message)
        }

        System.exit(rc)
    }

    @CompileDynamic
    def parseSubCommand(ArrayList unparsedArgs) {
        def command = unparsedArgs[0]
        if (!(command in commands)) {
            showGeneralHelp("unknown command: ${command}")
            System.exit(RC_BAD_OPTARGS)
        }
        if (unparsedArgs.size() < 2) {
            showDomainHelp(command, "subcommand missing")
            System.exit(RC_BAD_OPTARGS)
        } else {
            def subCommand = unparsedArgs[1]
            if (subCommand == 'help') {
                showDomainHelp(command)
                System.exit(RC_SUCCESS)
            } else {
                println "handle ${command} ${subCommand}. full list: ${unparsedArgs}"
                if (unparsedArgs.size() > 2 && unparsedArgs[2] =~ /^-*help$/) {
                    Class clazz = sub_commands[(command)][(subCommand)]
                    //print "${mainCommand} ${command} ${subCommand} "
                    def cliBuilder = clazz.newInstance().getCliBuilder('', false, false)
                    cliBuilder.formatter.setSyntaxPrefix("${mainCommand} ${command} ${subCommand} ")
                    cliBuilder.usage()
                } else {
                    Class clazz = sub_commands[(command)][(subCommand)]
                    def copyArgs = unparsedArgs.drop(2)
                    clazz.newInstance().run(copyArgs)
                    println "${command} ${subCommand} PLAIN"
                }
            }
        }
     }
}
