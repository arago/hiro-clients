"""
Package which contains the classes to communicate with HIRO / Graphit.
"""
import site
from os.path import dirname

from batchclient import GraphitBatch, SessionData
from client import Graphit, AuthenticationTokenError

__version__ = "2.2.5"

__all__ = [
    'Graphit', 'GraphitBatch', 'SessionData', 'AuthenticationTokenError'
]

site.addsitedir(dirname(__file__))
