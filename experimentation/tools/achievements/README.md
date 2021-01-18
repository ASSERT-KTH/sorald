# Achievements script
This is a script that can be used to generate an "achievements" file for
Sorald, detailing the pull requests performed with it. It's essentially
a human-readable version of the PRs JSON file.

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
The script is only usable in a single way: given a `prs.json` file, it
generates a human-readable Markdown file with some of the interesting content.
Use like so:

```bash
$ python3 achievements.py --prs-json-file path/to/prs.json --output ACHIEVEMENTS.md
```
