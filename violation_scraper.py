import requests;
import json;
from collections import Counter
import sys;
from urllib.parse import quote_plus # Make sysarg url-safe
# Url to SQ instance
url = "http://127.0.0.1:9000/";
if(len(sys.argv) > 1):
    url = sys.argv[1];
if(not url.endswith("/")):
    url += "/";
# If a SQ instance with multiple projects is specified (such as OW2 containing Spoon-Core), the specific project can be chosen.
project_key= "";
if(len(sys.argv) > 2):
    project_key = quote_plus(sys.argv[2]);
# Number of issues per page (Max 500)
pageSize = 500;
pageIndex = 1;
violations_remaining = True;
violated_rules = [];
# Fill array with SQ violations. Keep making calls until all (up to 10000 since SQ doesn't support more) issues have been caught.
while(violations_remaining):
    request_string = url + 'api/issues/search?resolved=false';
    if (not project_key == ""):
        request_string += '&componentRoots=' + project_key;
    request_string += '&ps=' + str(pageSize) + '&pageIndex=' + str(pageIndex);
    request = requests.get(request_string);
    if(request.status_code == 200):
        request_json = request.json();
        issues = request_json['issues'];
        if(len(issues) == 0):
            violations_remaining = False;
        for issue in issues:
            violated_rules.append(issue['rule']);
    pageIndex += 1;

print(Counter(violated_rules).keys());
print(Counter(violated_rules).values());
