/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package co.arago.hiro.cli.common

import groovy.transform.CompileStatic

import static co.arago.hiro.client.api.OGITConstants.Attributes.*
import groovy.json.JsonOutput

/**
 *
 * @author fotto
 */
@CompileStatic
class PrintHelper {
    
    static String json_indent = '  ' // 2 spaces

    static def suppportedListFormats = ['short', 'long', 'paragraph', 'csv', 'json']
    static String csvDelimiter = '"'
    static String csvSeparator = ':'
    
    static def filterListEntry(Map m, String listFormat, minmalAttrList, longAttrList=null) {
        def result = [:]
        if (listFormat == 'json') {
            return m
        }
        def current_attr_list = []
        switch (listFormat) {
            case 'short': 
                current_attr_list.add(OGIT_ID)
                break
            case 'long':
                current_attr_list.add(OGIT_ID)
                current_attr_list.addAll(minmalAttrList)
                break
            case 'paragraph':
            case 'csv':
                current_attr_list.add(OGIT_ID)
                current_attr_list.addAll(longAttrList ?: minmalAttrList)
                break
            default:
                throw new IllegalArgumentException("Unsupported list format: "+listFormat)
        }
        current_attr_list.each { key ->
            result[(key)] = m[(key)]
        }
        return result
    }
    
    
    static def formatListEntry(Map m, String listFormat, minmalAttrList, longAttrList=null) {
        switch (listFormat) {
            case 'short': 
                return m[(OGIT__ID)]
                break
            case 'long':
                def values = minmalAttrList.collect { attr ->
                    m[(attr)]
                }
                return (m[(OGIT__ID)] as String)+" "+values.join(" ")
                break
            case 'paragraph':
                String result = m[(OGIT__ID)]
                def attrList = longAttrList ?: minmalAttrList
                attrList.each { attr ->
                    // TODO: better formatting
                    result += "\n  "+attr+": "+m[(attr)]
                }
                result += "\n"
                return result
                break
            case 'csv':
                String result = csvDelimiter+m[(OGIT__ID)]+csvDelimiter
                longAttrList.each { attr ->
                    result += csvSeparator + csvDelimiter+m[(attr)]+csvDelimiter
                }
                return result
                break
            case 'json':
                def result = toJsonString(m)
                return result
                break
            default:
                throw new IllegalArgumentException("Unsupported list format: "+listFormat)
        }
    }

    static def printListEntry(Map m, String listFormat, minmalAttrList, longAttrList=null) {
        println formatListEntry(m, listFormat, minmalAttrList, longAttrList)
    }
    
    static def printListHeader(String listFormat, attrList) {
        switch (listFormat) {
            case 'short': 
                break
            case 'long': 
                break
            case 'json':
                break
            case 'paragraph':
                break
            case 'csv':
                def line = OGIT__ID
                attrList.each { attr ->
                    line += csvSeparator + attr
                }
                println line
                break
            default:
                throw new IllegalArgumentException("Unsupported list format: "+listFormat)
        }
    }

    static def toJsonString(object) {
        StringBuilder sb = new StringBuilder()
        _addToResult(object, sb, 0)
        return sb.toString()
    }

    private static def _addToResult(object, StringBuilder sb, Integer indentLevel) {
        //println "work on: $object"
        def newLevel = indentLevel + 1
        if (object instanceof Map) {
            sb.append("{\n")
            List keys = ((Map) object).keySet().sort()
            def lastIdx = keys.size() - 1
            keys.eachWithIndex { key, idx ->
                sb.append(json_indent * newLevel)
                sb.append('"')
                sb.append(key)
                sb.append('": ')
                def value = ((Map) object).get(key)
                _addToResult(value, sb, newLevel)
                if (idx == lastIdx) {
                    sb.append("\n")
                } else {
                    sb.append(",\n")
                }
            }
            sb.append(json_indent * indentLevel)
            sb.append("}")
        } else if (object instanceof List) {
            sb.append("[\n")
            List elements = (List) object
            def lastIdx = elements.size() - 1
            elements.eachWithIndex { it, idx ->
                sb.append(json_indent * newLevel)
                _addToResult(it, sb, newLevel)
                if (idx == lastIdx) {
                    sb.append("\n")
                } else {
                    sb.append(",\n")
                }
            }
            sb.append(json_indent * indentLevel)
            sb.append("]")
        } else {
            // for anything else we rely on conversion from Groovy SDK
            sb.append(JsonOutput.toJson(object));
        }
    }
}

