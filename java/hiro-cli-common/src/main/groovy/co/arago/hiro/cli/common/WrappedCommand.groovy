/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package co.arago.hiro.cli.common

import groovy.transform.CompileStatic

import static co.arago.hiro.cli.common.Constants.*
import groovy.util.logging.Log


/**
 *
 * @author fotto
 */

@CompileStatic
@Log
class WrappedCommand {
    
    //def subCommands // must be defined in subclass
        
    String mainCommand
    String command
    def subCommands = []
    
    Class parseSubCommand(List optargs) {
        String sub_command = optargs[0]
        
        switch (sub_command) {
            case null:
                showHelp(command, "Missing sub_command.")
                break            
            case ['help', '-help', '--help']:
                showHelp(command)
                break
            case 'commands': // hidden in usage
                String result = listSubCommands()
                print result.join('\n')
                System.exit(RC_SUCCESS)
                break
            case subCommands:
                return subCommands[(sub_command)]['class']
                break
            default:
                showHelp(command, "Invalid sub_command: ${sub_command}")
                break
        }
    }
    
    def showHelp(String command, String msg='') {
        if (msg) {
            println msg+"\n"
        }
        println "usage: ${mainCommand} [<general options>] ${command} <subcommand> <specific options/arguments>\n"
        println "Subcommands:\n"
        println subCommands.collect { String k, Map v -> k+ ' '*(FIELD_LENGTH-k.length()) + v.desc }.join("\n")
        if (msg) {
            System.exit(RC_BAD_OPTARGS)
        } else {
            System.exit(RC_SUCCESS)
        }
    }
    
    Class getHelperClass(String command) {
        if ('helperClass' in subCommands[(command)]) {
            return subCommands[(command)]['helperClass']
        } else {
            return null
        }
    }
    
    // return subcommand except 'help' as list
    def listSubCommands() {
        return subCommands.collect { k,v -> k }.findAll { k -> k != 'help' }
    }
}

