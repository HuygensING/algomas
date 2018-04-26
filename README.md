# Algo más

This is a Java library of general-purpose algorithms, data structures and
utilities. It supplements Apache Commons and Guava. Highlights include:

* Connected components (union-find)
* Levenshtein distance (edit distance) in several flavors
* Math utilities
* N-gram extraction
* Partial sorting
* Random sampling
* VP-trees (spatial index structure)

To use algomas, put the following in your pom.xml:

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
