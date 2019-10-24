import requests;
import json;
from collections import Counter # Counts and orders the list of violations
import sys;
from urllib.parse import quote_plus # Make sysarg url-safe
# List of Apache Commons libraries which I know can be analyzed (without crashing/failing their tests)
commonsList = ["bcel",
				"beanutils",
				"cli",
				"codec",
				"collections",
				"compress",
				"configuration",
				"crypto",
				"csv",
				"daemon",
				"dbcp",
				"dbutils",
				"exec",
				"fileupload",
				"geometry",
				"imaging",
				"io",
				"jexl",
				"lang",
				"logging",
				"math",
				"net",
				"ognl",
				"pool",
				"scxml",
				"statistics",
				"text",
				"validator",
				"vfs"];
# Number of issues per page (Max 500)
pageSize = 500;

def set_cmd_values():
    # Url to SQ instance (overwritten by cmd arguments).
    url = "http://127.0.0.1:9000/";
    # If a SQ instance with multiple projects is specified (such as OW2 containing Spoon-Core), the specific project can be chosen (overwritten by cmd args).
    project_key= "";
    if(len(sys.argv) > 1):
        url = sys.argv[1];
    if(not url.endswith("/")):
        url += "/";
    if(len(sys.argv) > 2):
        project_key = quote_plus(sys.argv[2]);
    return (url, project_key);

# Fill array with SQ violations. Keep making calls until all (up to 10000 since SQ doesn't support more) issues have been caught.
def get_violations(url, project_key):
	violated_rules = [];
	for lib in commonsList:
		violations_remaining = True;
		pageIndex = 1;
		project_key = "commons-" + lib;
		while(violations_remaining):
			request_string = url + 'api/issues/search?resolved=false';
			if (not project_key == ""):
				request_string += '&componentKeys=' + project_key;
			request_string += '&ps=' + str(pageSize) + '&pageIndex=' + str(pageIndex);
			request = requests.get(request_string);
			if(request.status_code == 200):
				request_json = request.json();
				issues = request_json['issues'];
				if(len(issues) == 0):
					violations_remaining = False;
				for issue in issues:
					if(issue['type'] == "BUG"):
						violated_rules.append(issue['rule']);
			pageIndex += 1;
	return violated_rules;

# Pretty prints a list, printing every object on its own line
def pretty_print(listVar):
    f = open("ordered_violations_list.txt", "w");
    for obj in listVar:
        print(obj);
        f.write(convertTuple(obj));

def convertTuple(tup):
    string = tup[0] + ", " + str(tup[1]) + "\n";
    return string;

def main():
    init_values = set_cmd_values();
    ordered_violations = (Counter(get_violations(init_values[0], init_values[1])).most_common());
    pretty_print(ordered_violations);

if __name__ == "__main__":
    main();
