"""
Package which contains the classes to communicate with HIRO / Graphit.
"""
import site
from os.path import dirname

from hiro_client.batchclient import GraphitBatch, SessionData
from hiro_client.client import Graphit, AuthenticationTokenError

__version__ = "2.2.5"

__all__ = [
    'Graphit', 'GraphitBatch', 'SessionData', 'AuthenticationTokenError'
]

site.addsitedir(dirname(__file__))
