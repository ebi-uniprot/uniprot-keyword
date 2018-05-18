# uniprot-keyword
REST API for UniProtKB supporting data keywords see https://www.uniprot.org/keywords/

UniProtKB (Universal Protein Knowledge Base) is a collection of functional information on proteins. UniProtKB entries are tagged with keywords (controlled vocabulary) that can be used to retrieve particular subsets of entries. These keyword belongs to categories and have complex structure in it self. They can have hierarchies, synonyms, gene ontology and definition. User (website/machine) can use this REST API to search or get all information about keywords used in UniProtKB.

Standalone application, you need java8 and maven to startup.

## Technologies
* Java 8
* Spring boot 2.0.1
* Embedded neo4j 3
* Maven 3.5.2
* Junit 5.01
* Mockito 2.18.3
* jackson 2.9.5
* assertj 3.9.1
* docker 17.12

## Getting started
1. Download keyword data file from ftp://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/keywlist.txt on local file system
1. Download/clone the code from github `git clone https://github.com/ebi-uniprot/uniprot-keyword.git`
1. Open file *uniprot-keyword/src/main/resources/application.properties* and change the value **spring.data.neo4j.uri=file://** with the path where you want to create your neo4j database 
1. Go to uniprot-keyword directory from terminal/command prompt
1. run command `mvn package`
1. For First time only (import data into database from txt file) run command `java -jar target/uniprot-keyword-0.0.1-SNAPSHOT.jar keywlist.txt`
  1. It will delete existing database first and then start to import data
  1. **Note:** I have downloaded *keywlist.txt* file in same directory. You have give the complete path of file if it is not in same directory
  1. Server will remain started and entertain requests
  1. If you want to stop server and just want to import data use `java -jar target/uniprot-keyword-0.0.1-SNAPSHOT.jar keywlist.txt --stopserver`
1. To start server second time (without import) use `java -jar target/uniprot-keyword-0.0.1-SNAPSHOT.jar`

## Endpoints
Endpoint | Description
-------- | -----------
http://localhost:8080/accession/KW-0001 | Return the single keyword entry with all depth relationships exact match on accession=KW-0001
http://localhost:8080/identifier/3D-structure | Return single keyword entry with all depth relationships exact match on identifier=3D-structure with case-sensitive
http://localhost:8080/identifier/all/2s | Returns the collection of all the matching keywords which contains the "2s" after ignoring case in identifiers. Return elements in list will resolve relationships at depth 1
http://localhost:8080/search/2s 4s | Returns the unique collection of all the matching keywords which contains the "2s" or "4s" after ignoring case in identifier or accession or synonyms or definition. Return elements in collection will contain relationships at depth level 1

## Getting started with Docker
You can build image [locally](docker) as well as use docker hub to pull image.

to pull from docker hub and start container in backgroud
```
docker run -d -p8080:8080 --name keyword impo/keyword_api:2018_04
```
Need any help regarding git commands see [git](https://github.com/rizwan-ishtiaq/wiki/blob/master/commands/docker.txt) for quick reference.

## Code Explanation
1. Package name convention, using the plural for packages with homogeneous contents and the singular for packages with heterogeneous contents.
1. Main Class uk.ac.ebi.uniprot.uniprotkeyword.UniprotKeywordApplication
1. Single Controller for API uk.ac.ebi.uniprot.uniprotkeyword.controller.DefaultController
1. Controller interacting with service and service interacting with repository
1. Import/Parse file logic is in uk.ac.ebi.uniprot.uniprotkeyword.import_data.ParseKeywordLines
1. Dataset while never (too slow) grow, therefore making following to make application fast
   1. While importing loading all lines from file to memory
   1. Create / persist list of all (1200) object into database at once

## License
This software is licensed under the Apache 2 license, quoted below.

Copyright (c) 2018, ebi-uniprot

Licensed under the [Apache License, Version 2.0.](LICENSE) You may not
use this project except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
