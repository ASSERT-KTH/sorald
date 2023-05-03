# Do not run the script locally. This script is made for submitting pull
# requests for generated files only via GitHub actions.

set -o errexit
set -o nounset
set -o pipefail

# Parse command-line arguments
# The number of command-line arguments is twice the number of flags passed and
# that is why we `shift` twice to skip to the next `key` and its value.
while [[ $# -gt 0 ]]; do
  key="$1"
  case $key in
    --branch-prefix)
      BRANCH_PREFIX="$2"
      shift
      shift
    ;;
    --generated-file)
      GENERATED_FILE="$2"
      shift
      shift
    ;;
    --gh-sha)
      SHA="$2"
      shift
      shift
    ;;
    --gh-token)
      TOKEN="$2"
      shift
      shift
    ;;
    --gh-repository)
      REPOSITORY="$2"
      shift
      shift
    ;;
    --gh-workflow)
      WORKFLOW="$2"
      shift
      shift
    ;;
    *)
      echo "Incorrect arguments"
      exit 1
    ;;
  esac
done

git config --local user.email github-actions[bot]@users.noreply.github.com
git config --local user.name github-actions[bot]

branch_name="${BRANCH_PREFIX}-update-${SHA}"
git switch -c "$branch_name"

git add "${GENERATED_FILE}"
git commit -m "docs: Update ${GENERATED_FILE}" || {
  echo "Nothing to commit"
  exit 0
}

git push https://"${TOKEN}"@github.com/"${REPOSITORY}".git

# Submit pull-request
curl \
  -X POST \
  -H "Accept: application/vnd.github.v3+json" \
  -H "Authorization: token ${TOKEN}" \
  https://api.github.com/repos/"${REPOSITORY}"/pulls \
  -d "{
    \"head\":\"$branch_name\",
    \"base\":\"master\",
    \"title\":\"docs: Update ${GENERATED_FILE}\",
    \"body\":\"Automatic update of ${GENERATED_FILE} from ${WORKFLOW} workflow. **Do not forget to remove this branch after merge!**\"
  }"

# Switch back to previous branch so that subsequent branches are checked out
# from that branch and not the branch created in this script.
git switch --detach -
