import requests;
import json;
from collections import Counter
pageSize = 500;
pageIndex = 1;
violations_remaining = True;
violated_rules = [];
while(violations_remaining):
    request = requests.get('http://127.0.0.1:9000/api/issues/search?resolved=false&ps=' + str(pageSize) + '&pageIndex=' + str(pageIndex));
    if(request.status_code == 200):
        request_json = request.json();
        issues = request_json['issues'];
        if(len(issues) == 0):
            violations_remaining = False;
        for issue in issues:
            # print(issue['rule']);
            violated_rules.append(issue['rule']);
    pageIndex += 1;

print(Counter(violated_rules).keys());
print(Counter(violated_rules).values());
