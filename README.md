

## mvn mission:rows-count

````
<build>
    <plugins>
        <plugin>
            <groupId>com.revengemission.plugins</groupId>
            <artifactId>mission-maven-plugin</artifactId>
            <version>0.1-SNAPSHOT</version>
            <configuration>
                <includes>
                    <include>java</include>
                    <include>sql</include>
                    <include>properties</include>
                </includes>
            </configuration>
            <executions>
                <execution>
                    <phase>compile</phase>
                    <goals>
                        <goal>rows-count</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
````

