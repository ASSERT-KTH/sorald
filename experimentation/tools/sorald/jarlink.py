"""Script for fetching a Sorald jar from Maven Central."""
import argparse
import datetime
import re
import sys

import requests
from bs4 import BeautifulSoup


def main():
    parser = argparse.ArgumentParser(
        prog="jarlink",
        description="Script for finding a link to the JAR of a given version "
        "of Sorald on Maven Central",
    )
    parser.add_argument(
        "-s", "--sorald-version", help="version to fetch JAR link for", required=True
    )
    version = parser.parse_args(sys.argv[1:]).sorald_version

    html_text = requests.get(
        f"https://oss.sonatype.org/content/repositories/snapshots/se/kth/castor/sorald/{version}/"
    ).text
    soup = BeautifulSoup(html_text, "html.parser")
    jar_links = [
        a.get("href")
        for a in soup.find_all(
            "a", attrs=dict(href=re.compile("jar-with-dependencies.jar$"))
        )
    ]

    if not jar_links:
        print(f"No JAR found for sorald-{version}", file=sys.stderr)
        sys.exit(1)

    selected_link = (
        jar_links
        if not version.endswith("-SNAPSHOT")
        else sorted(jar_links, key=extract_datetime, reverse=True)
    )[0]
    print(selected_link)


def to_datetime(date_str, time_str):
    year = int(date_str[:4])
    month = int(date_str[4:6])
    day = int(date_str[6:])

    hour = int(time_str[:2])
    minute = int(time_str[2:4])
    second = int(time_str[4:])
    return datetime.datetime(year, month, day, hour, minute, second)


def extract_datetime(s):
    date, time = re.search(r"(\d{8})\.(\d{6})", s).groups()
    return to_datetime(date, time)


if __name__ == "__main__":
    main()
