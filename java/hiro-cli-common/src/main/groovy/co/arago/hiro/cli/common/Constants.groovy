/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package co.arago.hiro.cli.common

import groovy.transform.CompileStatic

/**
 *
 * @author fotto
 */
@CompileStatic
class Constants {

    static Integer RC_BAD_OPTARGS = 2
    static Integer IO_ERROR = 3
    static Integer OTHER_ERROR = -1
    static Integer RC_SUCCESS = 0

    static String ERROR_KEY = "error"
    static def NEIGHBOUR_ATTRIBUTE = "_edgeType"
    
    static Integer FIELD_LENGTH = 15  // to format help messages
    
    static String TOKEN_ENV_VAR = 'HIRO_TOKEN'
    static int HTTP_CONFLICT = 409
    static int HTTP_NOTFOUND = 404
    
    static Map FILE_EXT_2_CONTENT_TYPE = [
        'png': 'image/png',
        'jpg': 'image/jpeg',
    ]
	
}

