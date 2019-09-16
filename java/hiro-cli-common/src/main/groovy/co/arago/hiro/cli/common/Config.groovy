
package co.arago.hiro.cli.common

import groovy.transform.CompileStatic

import java.util.Properties
import java.io.FileOutputStream

/**
 *
 * @author fotto
 */
@CompileStatic
class Config {
    static String defaultPropFile = System.getenv()['HOME']+File.separator+'.hiro.cli.properties'
    static def defaults = ['auth_url': 'http://localhost:8888',
                    'auth_client_id': '', 
                    'auth_client_secret': '', 
                    'username': '',
                    'api_url': 'http://localhost:8888',
                    'trust_all_certs': false,
                    'timeout': 60,
                    'default_knowledge_pool': '']
    
    static Map toRunnerSettings = [ 'api_url': 'url',
    'trust_all_certs': 'trustAllCerts',
    'timeout': 'timeout',
    'default_knowledge_pool': 'defaultKnowledgePool']
    static Map moreRunnerDefaults = [
    'debug': 0,
    'debugRest': null,
    'batchMode': false] 
    
    static def supportedProperties = defaults.collect { k, v -> k }
    static def booleanProperties = ['trust_all_certs']
    static def integerProperties = ['timeout']
    
    private Properties p
    String filePath = defaultPropFile
    Boolean loaded = false
    
    def _make_boolean(value) {
        switch  (value) {
            case 0: return false
            case 1: return true
            case ~/^(?i)t$/: return true
            case ~/^(?i)y$/: return true
            case ~/^(?i)true$/: return true
            case ~/^(?i)yes$/: return true
            default: return false
        }
    }
    
    def read() {
        p = new Properties()
        File f = new File(filePath)
        if (f.exists()) {
            FileInputStream is = new FileInputStream(f)
            p.load(is);
            is.close()
        }
        loaded = true
    }
    
    // for CommandWrapper and BaseCommand
    Map getMappedConfig() {
        if (!loaded) {
            read()
        }
        Map result = [:]
        moreRunnerDefaults.each { k, v ->
            result[k] = v
        }
        defaults.each { String k, v ->
            if (! k in toRunnerSettings) {
                return
            }
            def newKey = toRunnerSettings[(k)]
            if (k in this) {
                result[newKey] = get(k)
            } else {
                result[newKey] = v
            }
        }
        result
    }
    
    // returns null for missing
    def get(String key) {
        if (!loaded) {
            read()
        }
        // get rid of spaces
        def value = p.getProperty(key)?.trim()
        if (key in booleanProperties) {
            value = _make_boolean(value)
        }
        if (key in integerProperties) {
            value = value as Integer
        }
        value
    }
    
    def set(String key, String value) {
        if (!loaded) {
            read()
        }
        p.setProperty(key, value)
        this
    }
    
    // allow 'in' operator to check for containment
    def isCase(String key) {
        if (!loaded) {
            read()
        }
        if (p.getProperty(key) != null) {
            return true
        }
        return false
    }
    
    // allows access to config parameters as Groovy properties
    def getProperty(String propertyName) {
        if (propertyName in supportedProperties) {
            return get(propertyName)
        } else {
            return metaClass.getProperty(this, propertyName)
        }
    }
    
    def save(String comment) {
        if (!loaded) {
            throw new IllegalStateException("Properties not loaded")
        }
        new File(filePath).withWriter('utf-8') { writer ->
            p.store(writer, comment)
        }
    }
    
}

