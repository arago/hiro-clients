/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package co.arago.hiro.cli.common

import co.arago.hiro.cli.util.QueryHelper
import co.arago.hiro.admin.client.api.HiroIamClient
import co.arago.hiro.client.api.HiroClient
import groovy.transform.CompileStatic

import static co.arago.hiro.client.api.OGITConstants.Attributes.*
import static co.arago.hiro.client.api.OGITConstants.Entities.*

/**
 *
 * @author fotto
 */
@CompileStatic
class IamHelper {
    static String ORG_NAME_SEPARATOR = "/"
    
    static String rolename2id(HiroClient client, String name) {
        def orgId
        def query = [
            (OGIT__TYPE): AUTH_ROLE,
            (OGIT_NAME): name]
        def params = ['limit': '1',
                    'offset': '0'
        ]
        client.vertexQueryObject(QueryHelper.wildCardEsQuery(query), params).each { it ->
            orgId = it[(OGIT__ID)]
        }
        orgId
    }
    
    static String orgname2id(HiroClient client, String name) {
        def orgId
        def query = [
            (OGIT__TYPE): AUTH_ORGANIZATION,
            (OGIT_NAME): name]
        def params = ['limit': '1',
                    'offset': '0'
        ]
        client.vertexQueryObject(QueryHelper.wildCardEsQuery(query), params).each { it ->
            orgId = it[(OGIT__ID)]
        }
        orgId
    }
    
    static String name2id(HiroClient client, String type, String name, String orgId=null) {
        String queryOrgId  = orgId
        String queryName = name
        if (name.indexOf(ORG_NAME_SEPARATOR) > 0) {
            // expect <orgname>$sep<name of item>
            def tmp = name.split(ORG_NAME_SEPARATOR, 2)
            if (!queryOrgId) { // overrule by caller
                queryOrgId = orgname2id(client, tmp[0])
            }
        } else {
            if (!queryOrgId) {
                queryOrgId = client.meAccount([:])[(OGIT__ORGANIZATION)]
            }
        }
        def itemId
        if (!queryOrgId) {
            return itemId
        }
        def query = [
            (OGIT__TYPE): type,
            (OGIT_NAME): queryName,
            (OGIT__ORGANIZATION): queryOrgId]
        def params = ['limit': '1',
                    'offset': '0'
        ]
        client.vertexQueryObject(QueryHelper.wildCardEsQuery(query), params).each { it ->
            itemId = it[(OGIT__ID)]
        }
        itemId
    }
	
}

