# Web service to display JSON file contents

## Purpose
Basic service to display JSON file contents. There are 2 endpoints one displays whole JSON file & other returns record one bye one (stream records).
Provide path to the combined JSON file generated using [arm-merge-json-files](https://github.com/shimpiashutosh/arm-merge-json-files.git).

---

## Prerequisites

Before you can build and run this project, ensure that the following tools/softwares are installed on your system:

### 1. **Java Development Kit (JDK)**
- **Version**: Java 19 or later (Tested on version 19).
- **Installation**:
    - [Download JDK from Oracle](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://openjdk.java.net/).
    - Set `JAVA_HOME` environment variable to java installation path. You can check whether it has already been set.
      ```
      # Unix system
      echo $JAVA_HOME
      
      # Windows system
      echo %JAVA_HOME%
      ```
      if it is empty, then follow below steps according to your system. This is shell/commandline local variable.
      ```
      # Unix system
      export JAVA_HOME=/path/to/jdk/installation
      
      # Windows system
      set JAVA_HOME=/path/to/jdk/installation
      ```
      In case you want more details, you can follow [Set JAVA_HOME Variable](https://www.baeldung.com/java-home-on-windows-mac-os-x-linux).
- **Verify installation**:
  ```bash
  java -version
  ```

### 2. **Apache Maven** [optional]
(This is an optional step, you can use maven wrapper included in this project, refer to [Build the project](#how-to-build) section)
- **Version**: Maven 3.9.1 or later (Tested on version 3.9.1, you can try lower version if it works).
- **Installation**:
    - [Download Apache Maven](https://maven.apache.org/download.cgi).
    - Follow the installation steps and set the `M2_HOME` and `JAVA_HOME` environment variables if not set.
- **Verify installation**:
  ```bash
  mvn -v
  ```

### 3. **Git**
- **Version**: git 2.39 or later (Tested on version 2.39.5, you can try lower version if it works).
- **Installation**:
    - [Download Git](https://git-scm.com/downloads).
    - Follow the installation steps.
- **Verify installation**:
  ```bash
  git -v
  ```
---

## How to build?

Follow these steps to build the project:

1. **Clone the repository**:
   If you haven't already, clone the project repository to your local machine.
   ```bash
   # Clone git repo.
   git clone https://github.com/shimpiashutosh/arm-display-json-file.git
   
   # Get into cloned directory.
   cd arm-display-json-file
   ```

2. **Config changes**: [optional step]
   Make changes in `src/main/resources/application.properties`. One required change, update `display.file-path.json`
   property with absolute JSON file path to display. You can also change `server.port`. This is optional step, you can make changes in external config file `config/application.properties`

3. **Build the project**: [2 options, either can be used]
    1. Use Maven bundled in this project to compile and package the project (preferred).
       ```bash
       # Unix system
       ./mvnw clean install
       
       # Windows system
       mvnw.cmd clean install
       ``` 
       OR
    2. Use Maven installed on your system to compile and package the project.
       ```bash
       mvn clean install
       ```
   This command will download necessary dependencies, compile the source code, and generate a `.jar` file in the `target/` directory.

---

## How to run?

Before you run the application, change following in the `config/application.properties` file. You can skip this step if you have already made changes in `src/main/resources/application.properties`

   ```
   display.file-path.json={replace-this-with-absolute-path-to-json-file}
   ```
   Optionally you can change `server.port` to your desired port if required. By default, service starts at port `8080`.
   Once you make config changes, you are ready to start service.

   ```
   unix system absolute path looks like => /Users/ashutosh/projects/arm-merge-json-files/json-example-files/output-file/combined-json-file-small-data.json
   windows system absolute path looks like => C:/Users/ashutosh/Desktop/arm-merge-json-files/json-example-files/output-file/combined-json-file-small-data.json
   ```

### **How to run the service?**
   ```bash
   # Without external config, if changes made to file 'src/main/resources/application.properties'
   java -jar target/arm-display-json-file-<version>.jar
   ```
   OR
   ```bash
   # With external config, if changes not made to file 'src/main/resources/application.properties'
   java -jar target/arm-display-json-file-<version>.jar --spring.config.additional-location=file:/path/to/config/file
   ```
   Replace `<version>` with latest built jar version e.g. `1.0.0`

#### JVM settings [Just for a reference, add if you really need it]
   Program runs on low memory for large files.
    
   Explicit Heap memory – Xms and Xmx options
    
   One of the most common performance-related practices is to initialize the heap memory as per the application requirements.
    
   That’s why we should specify minimal and maximal heap size. We can use the below parameters to achieve this:

   ```
   # We can mark units as ‘g’ for GB, ‘m’ for MB, and ‘k’ for KB
   -Xms<heap size>[unit] 
   -Xmx<heap size>[unit]
   ```

   Example:
   ```
   java -Xms128m -jar target/arm-display-json-file-<version>.jar --spring.config.additional-location=file:/path/to/config/file
   java -Xmx256m -jar target/arm-display-json-file-<version>.jar --spring.config.additional-location=file:/path/to/config/file
   java -Xms128m -Xmx256m -jar target/arm-display-json-file-<version>.jar --spring.config.additional-location=file:/path/to/config/file
   ```

### **Examples to run the service**
   One example file has been included for testing purpose `example-json-file/combined-json-file.json`.
   ```bash
   # In case relative path doesn't work then use absolute path according to system e.g. windows, linux etc.
   java -jar target/arm-display-json-file-1.0.0.jar --spring.config.additional-location=file:config/application.properties
   ```
   ```bash
   java -jar target/arm-display-json-file-1.0.0.jar
   ```

## **How to use service?**
  After running a service as per given instructions, access the service to check whether JSON file is being displayed.
  There are 3 ways to access the service `OpenAPI/Swagger UI` Or `Command line` Or `Browser`. There are 2 endpoints designed, `/boards` displays whole file.
  `/boards/ndjson` streams record one by one.

### **OpenAPI/Swagger UI**
  Access below link via any suitable Browser [if you have changed the port then replace the same]
  ```
  http://localhost:8080/api/v1/webjars/swagger-ui/index.html
  ```
### **Command line**
  ```bash
   # Whole JSON file
   curl -H 'accept: application/json' -X 'GET' 'http://localhost:8080/api/v1/boards'
   ```
   ```bash
   # Stream record from JSON file one by one
   curl -H 'accept: application/x-ndjson' -X 'GET' 'http://localhost:8080/api/v1/boards/ndjson'
   ```
### **Browser**
You can access APIs directly over the browser, in case of `/api/v1/boards/ndjson` API you would need `application/x-ndjson` support, if not provided then file will be downloaded.
```bash
http://localhost:8080/api/v1/boards
```
```bash
http://localhost:8080/api/v1/boards/ndjson
```
