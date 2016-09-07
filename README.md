# Algo más

This is a library of general-purpose utilities for Java.

    <repository>
      <id>huygens</id>
      <url>http://maven.huygens.knaw.nl/repository/</url>
      <releases>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
        <checksumPolicy>warn</checksumPolicy>
      </releases>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
        <checksumPolicy>fail</checksumPolicy>
      </snapshots>
    </repository>
    
    <dependency>
      <groupId>nl.knaw.huygens.algomas</groupId>
      <artifactId>algomas-core</artifactId>
      <version>${algomas.version}</version>
    </dependency>
