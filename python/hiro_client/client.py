#!/usr/bin/env python3
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

import json
import time
from abc import abstractmethod
from typing import Optional, Any, Iterator
from urllib.parse import quote_plus, quote, urlencode

import backoff
import requests

# from requests.packages.urllib3.exceptions import InsecureRequestWarning

# requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

BACKOFF_ARGS = [
    backoff.expo,
    requests.exceptions.RequestException
]
BACKOFF_KWARGS = {
    'max_tries': 2,
    'jitter': backoff.random_jitter,
    'giveup': lambda e: e.response is not None and e.response.status_code < 500
}


class TokenInfo:
    """
    This class stores token information
    """

    token: str = None
    """ The token string """
    expires_at = -1
    """ Token expiration in ms since epoch"""
    refresh_token: str = None
    """ The refresh token to use - if any."""

    def __init__(self, token: str = None, refresh_token: str = None, expires_at: int = -1):
        """
        Constructor

        :param token: The token string
        :param refresh_token: A refresh token
        :param expires_at: Token expiration in ms since epoch
        """
        self.token = token
        self.expires_at = expires_at
        self.refresh_token = refresh_token

    @staticmethod
    def get_epoch_millis() -> int:
        """
        Get timestamp
        :return: Current epoch in milliseconds
        """
        return int(round(time.time() * 1000))

    def parse_token_result(self, res: dict, what: str) -> None:
        """
        Parse the result payload and extract token information.

        :param res: The result payload from the backend.
        :param what: What token command has been issued (for error messages).
        :raises AuthenticationTokenError: When the token request returned an error.
        """
        if 'error' in res:
            raise AuthenticationTokenError(
                '{}: {} ({})'.format(
                    what,
                    res['error'].get('message'),
                    res['error'].get('code')
                )
            )

        self.token = res.get('_TOKEN')

        expires_at = res.get('expires-at')
        if expires_at:
            self.expires_at = int(expires_at)
        else:
            expires_in = res.get('expires_in')
            if expires_in:
                self.expires_at = self.get_epoch_millis() + int(expires_in) * 1000

        refresh_token = res.get('refresh_token')
        if refresh_token:
            self.refresh_token = refresh_token

    def expired(self) -> bool:
        """
        Check token expiration

        :return: True when the token has been expired (expires_at <= get_epoch_mills())
        """
        return self.expires_at <= self.get_epoch_millis()


class AbstractAPI:
    __raise_exceptions: bool = False
    """Use res._check_status_error() to generate exceptions on http errors"""

    def __init__(self,
                 username: str,
                 password: str,
                 client_id: str,
                 client_secret: str,
                 graph_endpoint: str,
                 auth_endpoint: str,
                 iam_endpoint: str = None,
                 raise_exceptions: bool = False):
        """
        Constructor

        :param username: Username for authentication
        :param password: Password for authentication
        :param client_id: OAuth client_id for authentication
        :param client_secret: OAuth client_secret for authentication
        :param graph_endpoint: Full url for graph
        :param auth_endpoint: Full url for auth
        :param iam_endpoint: Full url for IAM access (optional)
        :param raise_exceptions: Raise exceptions on HTTP status codes that denote an error. Default is False
        """
        self._headers = {'Content-type': 'application/json',
                         'Accept': 'text/plain, application/json'
                         }

        self._username = username
        self._password = password
        self._client_id = client_id
        self._client_secret = client_secret
        self._graph_endpoint = graph_endpoint
        self._auth_endpoint = auth_endpoint
        self._iam_endpoint = iam_endpoint
        self.raise_exceptions = raise_exceptions

    @property
    def raise_exceptions(self) -> bool:
        """
        Use res._check_status_error() to generate exceptions on http errors

        :return: self.__raise_exceptions
        """
        return self.__raise_exceptions

    @raise_exceptions.setter
    def raise_exceptions(self, enable: bool) -> None:
        """
        Use res._check_status_error() to generate exceptions on http errors

        :param enable: The value to set.
        """
        self.__raise_exceptions = enable

    ###############################################################################################################
    # Basic requests
    ###############################################################################################################

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def get_binary(self, url: str, token: str = None) -> Iterator[bytes]:
        """
        Implementation of GET for binary data.

        :param url: Url to use
        :param token: External token to use. Default is False to handle token internally.
        :return: Yields an iterator over raw chunks of the response payload.
        """
        with requests.get(url,
                          headers=self._get_headers(token, content=False),
                          verify=False,
                          stream=True) as res:
            self._check_response(res, token)

            yield from res.iter_content(chunk_size=65536)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def post_binary(self, url: str, data: Any, content_type: str = None, token: str = None) -> dict:
        """
        Implementation of POST for binary data.

        :param url: Url to use
        :param data: The payload to POST. This can be anything 'requests.post(data=...)' supports.
        :param content_type: The content type of the data. Defaults to "application/octet-stream" internally if unset.
        :param token: External token to use. Default is False to handle token internally.
        :return: The payload of the response
        """
        headers = self._get_headers(token)
        headers['Content-Type'] = content_type or "application/octet-stream"
        res = requests.post(url,
                            data=data,
                            headers=headers,
                            verify=False)
        return self._parse_json_response(res, token)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def get(self, url: str, token: str = None) -> dict:
        """
        Implementation of GET

        :param url: Url to use
        :param token: External token to use. Default is False to handle token internally.
        :return: The payload of the response
        """
        res = requests.get(url,
                           headers=self._get_headers(token, content=False),
                           verify=False)
        return self._parse_json_response(res, token)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def post(self, url: str, data: Any, token: str = None) -> dict:
        """
        Implementation of POST

        :param url: Url to use
        :param data: The payload to POST
        :param token: External token to use. Default is False to handle token internally.
        :return: The payload of the response
        """
        res = requests.post(url,
                            json=data,
                            headers=self._get_headers(token, content=True),
                            verify=False)
        return self._parse_json_response(res, token)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def delete(self, url: str, token: str = None) -> dict:
        """
        Implementation of DELETE

        :param url: Url to use
        :param token: External token to use. Default is False to handle token internally.
        :return: The payload of the response
        """
        res = requests.delete(url,
                              headers=self._get_headers(token, content=False),
                              verify=False)
        return self._parse_json_response(res, token)

    ###############################################################################################################
    # Tool methods for requests
    ###############################################################################################################

    def _get_headers(self, token: str, content: bool = True) -> dict:
        """
        Create a header dict for requests. Uses abstract method *self._handle_token()*.

        :param token: An external token that gets passed through if it is not None.
        :param content: Remove 'Content-Type' from headers when set to False.
        :return: A dict containing header values for requests.
        """
        token = self._handle_token(token)
        headers = self._headers.copy()

        if not content and 'Content-Type' in headers:
            del headers['Content-Type']

        if token:
            headers['Authorization'] = "Bearer " + token

        return headers

    @staticmethod
    def _get_query_part(params: dict) -> str:
        """
        Create the query part of an url. Keys in *params* that are set to None are removed.

        :param params: A dict of params to use for the query.
        :return: The query part of an url with a leading '?', or an empty string when query is empty.
        """
        params_cleaned = {k: v for k, v in params.items() if v is not None}
        return '?' + urlencode(params_cleaned, quote_via=quote, safe="/,") if params_cleaned else ""

    def _parse_json_response(self, res: requests.Response, token: str = None) -> dict:
        """
        Parse the response of the backend.

        :param res: The result payload
        :param token: The external token if provided
        :return: The result payload
        :raises RequestException: On HTTP errors.
        """
        try:
            self._check_response(res, token)
            self._check_status_error(res)
            return res.json()
        except (json.JSONDecodeError, ValueError):
            return {"error": {"message": res.text, "code": 999}}

    def _check_status_error(self, res: requests.Response) -> None:
        """
        Catch exceptions and rethrow them with additional information returned by the error response body.

        :param res: The response
        :raises requests.exceptions.HTTPError: When an HTTPError occurred.
        """
        try:
            if self.raise_exceptions:
                res.raise_for_status()
        except requests.exceptions.HTTPError as err:
            http_error_msg = str(err.args[0])

            if res.content:
                try:
                    json_result: dict = res.json()
                    message = json_result['error']['message']
                    http_error_msg += ": " + message
                except (json.JSONDecodeError, KeyError):
                    if '_TOKEN' not in res.text:
                        http_error_msg += ": " + str(res.text)

            raise requests.exceptions.HTTPError(http_error_msg, response=err.response) from err

    ###############################################################################################################
    # Response and token handling
    # Abstract methods
    ###############################################################################################################

    @abstractmethod
    def _check_response(self, res: requests.Response, token: str) -> None:
        """
        Abstract base function for response checking. Might check for authentication errors depending on the context.

        :param res: The result payload
        :param token: The external token if provided
        """
        raise RuntimeError('Cannot use _check_response of this abstract class.')

    @abstractmethod
    def _handle_token(self, token: str) -> Optional[str]:
        """
        Abstract base function for token handling.

        :param token: An external token.
        :return: A valid token that might have been fetched automatically depending on the context.
        """
        raise RuntimeError('Cannot use _handle_token of this abstract class.')


class TokenHandler(AbstractAPI):
    """
    API Tokens will be fetched using this class. It does not handle any automatic token fetching, refresh or token
    expiry. This has to be checked and triggered by the *caller*.

    It is built this way to avoid endless calling loops when resolving tokens.
    """

    def __init__(self,
                 caller: AbstractAPI):
        """
        Constructor

        :param caller: The AbstractAPI parent using this class.
        """
        super().__init__(caller._username,
                         caller._password,
                         caller._client_id,
                         caller._client_secret,
                         caller._graph_endpoint,
                         caller._auth_endpoint,
                         caller._iam_endpoint,
                         caller.raise_exceptions)

    def get_token(self, token_info: TokenInfo) -> None:
        """
        Construct a request to obtain a new token. API self._auth_endpoint + '/app'

        :param token_info: The token info to update
        :raises AuthenticationTokenError: When no auth_endpoint is set.
        """
        if not self._auth_endpoint:
            raise AuthenticationTokenError(
                'Token is invalid and auth_endpoint for obtaining is not set.')

        url = self._auth_endpoint + '/app'
        data = {
            "client_id": self._client_id,
            "client_secret": self._client_secret,
            "username": self._username,
            "password": self._password
        }

        res = self.post(url, data)
        token_info.parse_token_result(res, 'Get token')

    def refresh_token(self, token_info: TokenInfo) -> None:
        """
        Construct a request to refresh an existing token. API self._auth_endpoint + '/refresh'

        :param token_info: The token info to update
        :raises AuthenticationTokenError: When no auth_endpoint is set.
        """
        if not self._auth_endpoint:
            raise AuthenticationTokenError(
                'Token is invalid and auth_endpoint for refresh is not set.')

        if not token_info.refresh_token:
            self.get_token(token_info)
            return

        url = self._auth_endpoint + '/refresh'
        data = {
            "client_id": self._client_id,
            "client_secret": self._client_secret,
            "refresh_token": token_info.refresh_token
        }

        res = self.post(url, data)
        token_info.parse_token_result(res, 'Refresh token')

    ###############################################################################################################
    # Response and token handling
    ###############################################################################################################

    def _check_response(self, res: requests.Response, token: str) -> None:
        """
        This is a dummy method. No response checking here.

        :param res: The result payload
        :param token: The external token if provided
        """
        return

    def _handle_token(self, token: str) -> Optional[str]:
        """
        Just return *token*. This *token* is usually None in this context, therefore a header without Authorization
        will be created in *self._get_headers()*.

        Does *not* try to obtain or refresh a token.

        :param token: An external token that gets passed through - usually None in this context here.
        :return: *token* given.
        """
        return token


class Graphit(AbstractAPI):
    """
    Python implementation for accessing the HIRO Graphit REST API
    """

    _token_info: TokenInfo = None
    """Contains all token information"""

    def __init__(self,
                 username: str,
                 password: str,
                 client_id: str,
                 client_secret: str,
                 graph_endpoint: str,
                 auth_endpoint: str,
                 iam_endpoint: str = None,
                 raise_exceptions: bool = False):
        """
        Constructor

        :param username: Username for authentication
        :param password: Password for authentication
        :param client_id: OAuth client_id for authentication
        :param client_secret: OAuth client_secret for authentication
        :param graph_endpoint: Full url for graph
        :param auth_endpoint: Full url for auth
        :param iam_endpoint: Full url for IAM access (optional)
        :param raise_exceptions: Raise exceptions on HTTP status codes that denote an error. Default is False
        """
        super().__init__(username,
                         password,
                         client_id,
                         client_secret,
                         graph_endpoint,
                         auth_endpoint,
                         iam_endpoint,
                         raise_exceptions)

        self._token_info = TokenInfo()

    ###############################################################################################################
    # Response and token handling
    ###############################################################################################################

    def _check_response(self, res: requests.Response, token: str) -> None:
        """
        Response checking. Tries to refresh the token on status_code 401, then raises RequestException to try
        again using backoff.

        :param res: The result payload
        :param token: The external token if provided
        :raises requests.exceptions.RequestException: When an error 401 occurred and the token has been refreshed.
        :raises AuthenticationTokenError: When an external token has been provided but code 401 has been returned
                                          from the backend.
        """
        if res.status_code == 401:
            if token:
                raise AuthenticationTokenError(
                    'Cannot refresh invalid token that was given externally.')

            TokenHandler(self).refresh_token(self._token_info)

            # Raise this exception to trigger retry with backoff
            raise requests.exceptions.RequestException

    def _handle_token(self, token: str) -> Optional[str]:
        """
        Try to return a valid token by passing it, or obtaining or refreshing it.

        :param token: An external token that gets passed through if it is not None.
        :return: A valid token.
        """
        if token:
            return token

        if not self._token_info.token:
            TokenHandler(self).get_token(self._token_info)
        elif self._token_info.expired():
            TokenHandler(self).refresh_token(self._token_info)

        return self._token_info.token

    ###############################################################################################################
    # REST API operations
    ###############################################################################################################

    def query(self,
              query: str,
              fields: str = None,
              token: str = None,
              limit=-1,
              offset=0,
              order: str = None,
              meta=False) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/query/vertices'`

        :param query: The actual query. e.g. outE().inV() for gremlin, ogit\\\\/_type: ogit\\\\/Question for vertices,
                      id1,id2,id3 for Multi-ID, id1 for XID (External ID)'
        :param fields: the comma separated list of fields to return
        :param token: Optional external token.
        :param limit: limit of entries to return
        :param offset: offset where to start returning entries
        :param order: order by a field asc|desc, e.g. ogit/name desc
        :param meta: List detailed metainformations in result payload
        :return: Result payload
        """
        url = self._graph_endpoint + '/query/vertices'
        data = {"query": str(query),
                "limit": limit,
                "fields": (quote_plus(fields.replace(" ", ""), safe="/,") if fields else ""),
                "count": False,
                "listMeta": meta,
                "offset": offset}
        if order is not None:
            data['order'] = order
        return self.post(url, data, token)

    def create_node(self, data: dict, obj_type: str, token: str = None, return_id=False) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/new/{id}'`

        :param data: Payload for the new node/vertex
        :param obj_type: ogit/_type of the new node/vertex
        :param token: Optional external token.
        :param return_id: Return only the ogit/_id. Default is False to return everything.
        :return: The result payload
        """
        url = self._graph_endpoint + '/new/' + quote_plus(obj_type)
        res = self.post(url, data, token)
        return res['ogit/_id'] if return_id and 'error' not in res else res

    def update_node(self, node_id: str, data: dict, token: str = None) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/{id}'`

        :param data: Payload for the node/vertex
        :param node_id: ogit/_id of the node/vertex
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/' + quote_plus(node_id)
        return self.post(url, data, token)

    def delete_node(self, node_id: str, token: str = None) -> dict:
        """
        Graphit REST query API: `DELETE self._graph_endpoint + '/{id}'`

        :param node_id: ogit/_id of the node/vertex
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/' + quote_plus(node_id)
        return self.delete(url, token)

    def connect_nodes(self, from_node_id: str, verb: str, to_node_id: str, token: str = None) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/connect/{verb}'`

        :param from_node_id: ogit/_id of the source node/vertex
        :param verb: verb for the connection
        :param to_node_id: ogit/_id of the target node/vertex
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/connect/' + quote_plus(verb)
        data = {"out": from_node_id, "in": to_node_id}
        return self.post(url, data, token)

    def disconnect_nodes(self, from_node_id: str, verb: str, to_node_id: str, token: str = None) -> dict:
        """
        Graphit REST query API: `DELETE self._graph_endpoint + '/{id}'`

        :param from_node_id: ogit/_id of the source node/vertex
        :param verb: verb for the connection
        :param to_node_id: ogit/_id of the target node/vertex
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/' + quote_plus(
            from_node_id
        ) + "$$" + quote_plus(
            verb
        ) + "$$" + quote_plus(
            to_node_id
        )
        return self.delete(url, token)

    def get_node(self, node_id: str, fields: str = None, meta: bool = None, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._graph_endpoint + '/{id}'`

        :param node_id: ogit/_id of the node/vertex or edge
        :param fields: Filter for fields
        :param meta: List detailed metainformations in result payload
        :param token: Optional external token.
        :return: The result payload
        """
        query = {
            "fields": fields.replace(" ", "") if fields else None,
            "listMeta": "true" if meta else None
        }

        url = self._graph_endpoint + '/' + quote_plus(node_id) + self._get_query_part(query)
        return self.get(url, token)

    def get_node_by_xid(self, node_id: str, fields: str = None, meta: bool = None, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._graph_endpoint + '/xid/{xid}'`

        :param node_id: ogit/_xid of the node/vertex or edge
        :param fields: Filter for fields
        :param meta: List detailed metainformations in result payload
        :param token: Optional external token.
        :return: The result payload
        """
        query = {
            "fields": fields.replace(" ", "") if fields else None,
            "listMeta": "true" if meta else None
        }

        url = self._graph_endpoint + '/xid/' + quote_plus(node_id) + self._get_query_part(query)
        return self.get(url, token)

    def get_account(self, node_id, fields: str = None, meta: bool = None, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._iam_endpoint + '/accounts/{xid}'`

        :param node_id: ogit/_xid of the node/vertex or edge
        :param fields: Filter for fields
        :param meta: List detailed metainformations in result payload
        :param token: Optional external token.
        :return: The result payload
        """
        query = {
            "fields": fields.replace(" ", "") if fields else None,
            "listMeta": "true" if meta else None
        }

        url = self._iam_endpoint + '/accounts/' + quote_plus(node_id) + self._get_query_part(query)
        return self.get(url, token)

    def update_account(self, node_id: str, data: dict, token: str = None) -> dict:
        """
        Graphit REST query API: `POST self._iam_endpoint + '/accounts/{xid}'`

        :param node_id: ogit/_xid of the node/vertex or edge
        :param data: Data for updating account
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._iam_endpoint + '/accounts/' + quote_plus(node_id)
        return self.post(url, data, token)

    def get_timeseries(self, node_id: str, starttime: str = None, endtime: str = None, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._graph_endpoint + '/{id}/values'`

        :param node_id: ogit/_id of the node containing timeseries
        :param starttime: ms since epoch.
        :param endtime: ms since epoch.
        :param token: Optional external token.
        :return: The result payload
        """
        query = {
            "from": starttime,
            "to": endtime
        }

        url = self._graph_endpoint + '/' + quote_plus(node_id) + '/values' + self._get_query_part(query)
        res = self.get(url, token)
        if 'error' in res:
            return res
        timeseries = res['items']
        timeseries.sort(key=lambda x: x['timestamp'])
        return timeseries

    def post_timeseries(self, node_id: str, items: list, token: str = None) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/{id}/values'`

        :param node_id: ogit/_id of the node containing timeseries
        :param items: list of timeseries values [{timestamp: (ms since epoch), value: ...},...]
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/' + quote_plus(node_id) + '/values?synchronous=true'
        data = {"items": items}
        return self.post(url, data, token)

    def get_attachment(self,
                       node_id: str,
                       content_id: str = None,
                       include_deleted: bool = False,
                       token: str = None) -> Iterator[bytes]:
        """
        Graphit REST query API: `GET self._graph_endpoint + '/{id}/content'`

        :param node_id: Id of the attachment node
        :param content_id: Id of the content within the attachment node. Default is None.
        :param include_deleted: Whether to be able to access deleted content: Default is False
        :param token: Optional external token.
        :return: An Iterator over byte chunks from the response body payload.
        """
        query = {
            "contentId": content_id,
            "includeDeleted": "true" if include_deleted else None
        }

        url = self._graph_endpoint + '/' + quote_plus(node_id) + '/content' + self._get_query_part(query)
        return self.get_binary(url, token)

    def post_attachment(self,
                        node_id: str,
                        data: Any,
                        content_type: str = None,
                        token: str = None) -> dict:
        """
        Graphit REST query API: `POST self._graph_endpoint + '/{id}/content'`

        :param node_id: Id of the attachment node
        :param data: Data to upload in binary form. Can also be an IO object for streaming.
        :param content_type: Content-Type for *data*. Defaults to 'application/octet-stream' if left unset.
        :param token: Optional external token.
        :return: The result payload
        """
        url = self._graph_endpoint + '/' + quote_plus(node_id) + '/content'
        return self.post_binary(url, data, content_type, token)

    def get_identity(self, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._auth_endpoint + '/me/account'`

        :param token: Optional external token.
        :return: The result payload
        """
        url = self._auth_endpoint + '/me/account'
        return self.get(url, token)

    def get_teams(self, token: str = None) -> dict:
        """
        Graphit REST query API: `GET self._auth_endpoint + '/me/teams'`

        :param token: Optional external token.
        :return: The result payload
        """
        url = self._auth_endpoint + '/me/teams'
        return self.get(url, token)


class AuthenticationTokenError(Exception):
    """
    Class for unrecoverable failures with access tokens
    """
    message: str

    def __init__(self, message: str):
        self.message = message

    def __str__(self):
        return "{}: {}".format(self.__class__.__name__, self.message)
