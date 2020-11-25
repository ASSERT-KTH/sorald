# GitHub issue search script
This is a small wrapper script around GitHub's [issue search
API](https://docs.github.com/en/free-pro-team@latest/rest/reference/search#search-issues-and-pull-requests).
 
## Requirements
Requires Python 3.8+

## Install
Install the packages listed in `requirements.txt`.

```bash
$ python3 -m pip install -r requirements.txt
```

> **Note:** You should do this in a virtual environment to avoid polluting
> system-wide or user packages.

## Usage
Run like so.

```bash
$ python3 main.py --token <YOUR_GITHUB_TOKEN> --query <YOUR_QUERY>
```

There are a bunch of other options you can set as well, run with the `--help`
option for complete usage. Here's an example invocation searching for
the keyword "sonar".

```bash
$ python3 main.py --token xxxxxxxx --query sonar
https://github.com/pmd/pmd/issues/2942
https://github.com/SpoonLabs/sorald/issues/226
https://github.com/javaparser/javaparser/issues/2937
https://github.com/springdoc/springdoc-openapi-maven-plugin/issues/19
https://github.com/pmd/pmd/issues/2928
https://github.com/cnescatlab/sonar-cnes-report/issues/170
https://github.com/DPigeon/Money-Tree/issues/147
https://github.com/Riverside-Software/sonar-openedge/issues/856
https://github.com/pau101/Fairy-Lights/issues/94
https://github.com/Tencent/QMUI_Android/issues/1032
https://github.com/insideapp-oss/sonar-flutter/issues/9
https://github.com/SonarSource/sonar-css/issues/297
https://github.com/SonarSource/SonarJS/issues/1918
https://github.com/sonar-intellij-plugin/sonar-intellij-plugin/issues/83
https://github.com/find-sec-bugs/find-sec-bugs/issues/617
https://github.com/Nesreenmoh/sfg-pet-clinic/issues/82
https://github.com/Nesreenmoh/sfg-pet-clinic/issues/81
[...]
```

For more stuff you can do with the query string, see [the GitHub
docs](https://docs.github.com/en/free-pro-team@latest/github/searching-for-information-on-github/searching-issues-and-pull-requests).

> **Warning:** This script uses up API requests quite rapidly, and you'll hit
> the rate limit before you know it.
