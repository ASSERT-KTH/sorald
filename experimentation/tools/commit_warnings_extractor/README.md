# Commit warnings extractor
This is a script for extracting warnings from individual commits in a given set
of repositories.

## Requirements
Requires Python 3.8+ and a Sorald jarfile, as well as a Java runtime capable of
executing the jarfile.

## Install
Install the packages listed in `requirements.txt`.

```bash
$ python3 -m pip install -r requirements.txt
```

> **Note:** You should do this in a virtual environment to avoid polluting
> system-wide or user packages.

## Usage
Before using the script, build the Sorald jar by going to the root of the
repository and executing `mvn package -DskipTests`. Then, you can execute the
script like so.

```bash
$ python main.py path/to/repos_list.txt
```

This will execute the commit extractor with a bunch of default values for
different options. The most important assumption is that there
is a **Sorald jar in the target directory at the root of this repository**.
If there isn't, you must specify the path manually (or build the Sorald jar).
Execute the script with the `--help` for more detailed information on the rest
of the options.

`repos_list.txt` is a text file listing one repository per line, like so.

```
spoonlabs/sorald
inria/spoon
kth/spork
```

> **Note:** Currently, the commit extractor is optimized for projects using
> Maven. It will not find sources for any other build tools.

### Limiting running time
If the experiment is taking too long, try limiting either a) the amount of
commits you extract (`--num-commits-per-repo`), or b) the step size between
commits (`--step-size`), or c) both of those.

### Passing options to the miner
It's sometimes useful to pass options to the miner itself. This can be done
with the `--miner-option|--mo` argument. For example, to specify the
`--ruleTypes vulnerability` option for the miner, simply pass it as a miner
option.

```bash
$ python main.py path/to/repos_list.txt --miner-option 'ruleTypes=vulnerability'
```

Note the absence of leading `--` in front of `ruleTypes`, and the connecting
`=` between the name of the option, and its value.
