# HIRO-clients

HIRO clients holds the official java clients for interacting with the APIs of HIRO (graph, connector, ...).

A client written in Python 3 is available at https://github.com/arago/python-hiro-clients

**Status**

- Technical Preview

**Usage**

Requires java8 and maven.

    cd java
    mvn clean install
    
and in your project's pom.xml add

    <dependency>
      <groupId>co.arago</groupId>
      <artifactId>hiro-client</artifactId>
      <version>${version}</version>
    </dependency>
    
A good starting point is java/hiro-client/src/main/java/co/arago/hiro/client/Hiro.java

    Hiro.newBuilder()/* options */.build();

**Contributing**

We value contribution, if you have any changes or suggestions, or if you have created a new action handler, please feel free to open a pull request add it here.

If you submit content to this repository, your contribution will be licensed under the MIT license (see below).

**Questions and Support**

For questions and support, please refer to our documentation https://docs.hiro.arago.co/, visit our community https://hiro.community/ for asking detailed questions or contact support@hiro.arago.co.

**License (MIT)**

Copyright (c) 2019 arago GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
