import re
from setuptools import setup, find_packages

test_requirements = ["pytest>=6.0.0"]
required = [
    "jinja2>=2.11.2",
    "gitpython",
    "pygithub",
    "requests",
    "tqdm>=4.51.0",
    "pandas>=1.1.3",
    "beautifulsoup4>=4.9.3",
]

setup(
    name="soraldscripts",
    description="Helper scripts for the Sorald automatic repair tool",
    packages=find_packages(exclude=("tests", "docs")),
    tests_require=test_requirements,
    install_requires=required,
    extras_require=dict(TEST=test_requirements),
    include_package_data=True,
    zip_safe=False,
)
