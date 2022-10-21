# SecretHub Client for Java

__SecretHub has been discontinued, so this project has been archived.__

[![Build Status](https://github.com/jasonshobe/secrethub-java/workflows/build/badge.svg)]()
[![Apache 2.0](https://img.shields.io/github/license/jasonshobe/secrethub-java)]()
![Maven Central](https://img.shields.io/maven-central/v/com.jshobe.secrethub/secrethub-java)

A library that provides a Java client for the SecretHub Secrets Management API.

## Installation

Add the dependency to your project:

#### Using Maven

```xml
<dependency>
  <groupId>com.jshobe.secrethub</groupId>
  <artifactId>secrethub-java</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### Using Gradle

```groovy
implementation 'com.jshobe.secrethub:secrethub-java:1.0.0'
```

## Usage

Put the credentials for your SecretHub service account in the
`$HOME/.secrethub/credential` file or in the `SECRETHUB_CREDENTIAL`
environment variable.

> NOTE: It is not necessary to install the SecretHub binaries to use this
> library.

In your Java code, create an instance of `Client` and start using it.

```java
import com.jshobe.secrethub.Client;
import com.jshobe.secrethub.SecretHubException;

public class MyClass {
  public static void main(String[] args) {
    try (Client client = new Client()) {
      String secret = client.readString("com/example/test");
      System.out.println("MY SECRET: " + secret);
    } catch (SecretHubException e) {
      e.printStackTrace();
    }
  }
}
```

The client API closely follows what is provided by the
[SecretHub SDK](https://pkg.go.dev/github.com/secrethub/secrethub-go). See
the [API Documentation](https://jasonshobe.github.io/secrethub-java/) for
details.

## License

The SecretHub Client for Java is releases under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0.txt).
