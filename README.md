# uniprot-keyword
REST API for UniProtKB supporting data keywords see https://www.uniprot.org/keywords/

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
