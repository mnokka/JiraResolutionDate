# JiraResolutionDate
Investigation how reset resolution date to certain fixed date (via Groovy scripting)
This fixed date is found from issue history (like when was state transit to ZZZ state)

Also adding JQL query to  Groovy script to find all "needed" issues for this fix operation
to be applied as a bulk operation

Check the code, you need to activate yourself yhe old issue.store() API usage

Doing full Jira reindex after script usage will calculate Resolution Date and Resolution again
