from setuptools import setup, find_packages
from hiro_client import __version__

setup(
    name="hiro_client",
    version=__version__,
    packages=find_packages(),

    author="Wolfgang Hübner (arago GmbH)",
    author_email="info@arago.co",
    description="Hiro Client for Graph REST API of HIRO 7",
    keywords="HIRO7 connector arago REST API",
    url="https://github.com/arago/hiro-clients",
    license="MIT",
    classifiers=[
        "Programming Language :: Python :: 3",
        "License :: OSI Approved :: MIT License",
        "Operating System :: OS Independent",
    ]
)
