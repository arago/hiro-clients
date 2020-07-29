#!/usr/bin/env python3
# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.


import json
import time
from urllib.parse import quote_plus

import requests
import backoff

BACKOFF_ARGS = [
    backoff.expo,
    requests.exceptions.RequestException
]
BACKOFF_KWARGS = {
    'max_tries': 10,
    'jitter':    backoff.random_jitter,
    'giveup':    lambda e: e.response is not None and e.response.status_code < 500
}

class Graphit():

    def __init__(self, username, password,
                 client_id, client_secret,
                 graph_endpoint, auth_endpoint,
                 iam_endpoint=None):
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
        self._token = None
        self._token_expired = None

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def get(self, url, token=None):
        url = url.replace('"','') # temporary workaround.

        if token is None:
            token, _token_expired = self.get_token()
        headers = self._headers
        if token is not None:
            headers['Authorization'] = "Bearer " + token
        res = requests.get(url, headers=headers, verify=False)
        return self._parse_response(res)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def post(self, url, data, token=None):
        url = url.replace('"', '') # temporary workaround.
        if token is None:
            token, _token_expired = self.get_token()
        headers = self._headers
        if token is not None:
            headers['Authorization'] = "Bearer " + token
        res = requests.post(url, data=json.dumps(data),
                            headers=headers,
                            verify=False)
        return self._parse_response(res)

    @backoff.on_exception(*BACKOFF_ARGS, **BACKOFF_KWARGS)
    def delete(self, url, token=None):
        if token is None:
            token, _token_expired = self.get_token()
        headers = self._headers
        if token is not None:
            headers['Authorization'] = "Bearer " + token
        res = requests.delete(url, headers=headers, verify=False)
        return self._parse_response(res)

    def _timestamp(self):
        return "[" + time.asctime(time.gmtime()) + " UTC]"


    def _parse_response(self, res):
        if res.status_code == 401:
            self.refresh_token()
            raise requests.exceptions.RequestException
        try:
            return json.loads(res.text)
        except json.decoder.JSONDecodeError:
            return res.text

    def get_identity(self, jwt_thetoken):
        url = self._auth_endpoint + '/me/account'
        headers = self._headers
        headers['Authorization'] = "Bearer " + jwt_thetoken
        res = requests.get(url, headers=headers, verify=False)
        return self._parse_response(res)

    def refresh_token(self):
        url = self._auth_endpoint + '/app'
        data = {
            "client_id": self._client_id,
            "client_secret": self._client_secret,
            "username": self._username,
            "password": self._password
        }
        res = self.post(url, data, token="getToken")
        if '_TOKEN' in res.keys():
            self._token = res['_TOKEN']
        else:
            self._token = None
        if 'expires-at' in res.keys():
            self._token_expired = res['expires-at']

    def get_token(self):
        if self._token is None:
            self.refresh_token()
        return self._token, self._token_expired

    def query(self, query, fields=None, token=None, limit=-1, offset=0, order=None):
        url = self._graph_endpoint + '/query/vertices'
        data = {"query": str(query),
                "limit": limit,
                "fields": (quote_plus(fields.replace(" ", ""), safe="/,") if fields else ""),
                "count": False,
                "offset": offset}
        if order is not None:
            data['order'] = order
        return self.post(url, data, token)

    def create_node(self, data, obj_type, token=None, return_id=False):
        url = self._graph_endpoint + '/new/' + quote_plus(obj_type)
        res = self.post(url, data, token)
        return res['ogit/_id'] if return_id and 'error' not in res else res

    def update_node(self, node_id, data, token=None):
        url = self._graph_endpoint + '/' + quote_plus(node_id)
        return self.post(url, data, token)

    def delete_node(self, node_id, token=None):
        url = self._graph_endpoint + '/' + quote_plus(node_id)
        return self.delete(url, token)

    def get_node(self, node_id, fields=None, meta=None, token=None):
        url = self._graph_endpoint + '/' + \
              quote_plus(node_id) + ('?fields=' +
                                     quote_plus(fields.replace(" ", ""), safe="/,") if fields else "") + \
              ('?listMeta=True' if meta else "")
        return self.get(url, token)

    def get_node_by_xid(self, node_id, fields=None, meta=None, token=None):
        url = self._graph_endpoint + '/xid/' + \
              quote_plus(node_id) + ('?fields=' +
                                     quote_plus(fields.replace(" ", ""), safe="/,") if fields else "") + \
              ('?listMeta=True' if meta else "")
        return self.get(url, token)

    def get_account(self, node_id, fields=None, meta=None, token=None):
        token, _token_expired = self.get_token()
        url = self._iam_endpoint + '/accounts/' + \
              quote_plus(node_id) + ('?fields=' +
                                     quote_plus(fields.replace(" ", ""), safe="/,") if fields else "") + \
              ('?listMeta=True' if meta else "")
        return self.get(url, token)

    def update_account(self, node_id, data, token=None):
        url = self._iam_endpoint + '/accounts/' + quote_plus(node_id)
        return self.post(url, data, token)

    def get_timeseries(self, timeseries_node, starttime=None, endtime=None, token=None):
        url = self._graph_endpoint + '/' + quote_plus(timeseries_node) + '/values' + (
            '?from=' + starttime if starttime else "") + ('&to=' + endtime if endtime else "")
        res = self.get(url, token)
        if 'error' in res:
            return res
        timeseries = res['items']
        timeseries.sort(key=lambda x: x['timestamp'])
        return timeseries

    def post_timeseries(self, node_id, items, token=None):
        url = self._graph_endpoint + '/' + \
              quote_plus(node_id) + '/values?synchronous=true'
        data = {"items": items}
        return self.post(url, data, token)

    def get_teams(self, token=None):
        url = self._auth_endpoint + '/me/teams'
        return self.get(url, token)
