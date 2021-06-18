// Set fixed issue's Resolutiondate to fixed date stamp



import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.IssueManager
import java.sql.Timestamp

import org.apache.log4j.Logger
import org.apache.log4j.Level
import java.text.SimpleDateFormat
import java.sql.Timestamp
import java.text.DateFormat
import java.util.Date
import groovy.time.TimeCategory
import java.sql.Timestamp
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.IssueImpl
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.util.ImportUtils
import com.atlassian.jira.issue.security.IssueSecurityLevelManager
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.issue.search.SearchException

// CONFIGURATIONS *********************

int MonthsValue = 20 as Integer 

// END OF CONFIGURATIONS ****************



// set logging to Jira log
def Logger mylogger
mylogger = Logger.getLogger("ResolutioDater")
mylogger.setLevel(Level.DEBUG ) // or INFO (prod) or DEBUG (development)

mylogger.info("----------------STARTED--------------------------------------------------------------------")


def issueService = ComponentAccessor.issueService
def issueManager = ComponentAccessor.getIssueManager()
//IssueIndexingService issueIndexingService = ComponentAccessor.getComponent(IssueIndexingService)
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()

//MutableIssue myIssue = issueManager.getIssueObject("TESDATERES-1")
//MutableIssue myIssue = issueManager.getIssueObject("NB1394NCM-1")
Issue  myIssue = issueManager.getIssueObject("NB1394NCM-283")
//Issue  myIssue = issueManager.getIssueObject("TESDATERES-1")
MutableIssue mutantIssue=issueManager.getIssueObject(myIssue.id)

def currentUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser

Date CurrentDate=new Date()
mylogger.debug( "CurrentDate: $CurrentDate   ")
// from import groovy.time.TimeCategory
def timepast
Timestamp PastDate
use(TimeCategory) {
	timepast=CurrentDate - MonthsValue.months
	PastDate=new Timestamp(timepast.getTime())
}
mylogger.debug( "New date: $timepast   ")
mylogger.debug( "New datestamp: $PastDate   ")


// check when issuw was acttually resolved, in this case when moved to "Done" state
def done = changeHistoryManager.getChangeItemsForField(myIssue, "status").reverse().find {
	it.toString == "Settled"  // Settled Done
	}?.getCreated()
def doneTime = done?.getTime()
def doneDate
doneDate = new Date((long)doneTime)

def doneStamp
def timedone
use(TimeCategory) {
	timedone=doneDate
	doneStamp=new Timestamp(timedone.getTime())
}

mylogger.debug( "doneTime: $doneTime   ")
mylogger.debug( "doneDate: $doneDate   ")
mylogger.debug( "orig settleddoneStamp: $doneStamp   ")

currentresdate=myIssue.getResolutionDate() 
mylogger.debug( "currentresdate: $currentresdate   ")



myIssue.setResolutionDate(doneStamp)


//def issueInputParams = issueService.newIssueInputParameters()
//issueInputParams.setResolutionDate(PastDate.toString())
//issueManager.updateIssue(currentUser, myIssue, EventDispatchOption.ISSUE_UPDATED, false)
// Update the search index for the newly created child issue.
//def wasIndexing = ImportUtils.isIndexIssues()
//ImportUtils.setIndexIssues(true)
//issueIndexingService.reIndex(mutantIssue)
//ImportUtils.setIndexIssues(wasIndexing)


myIssue.store() //works but usage not recommended anymore, the modern approach commented out does not work yet

//def updateValidationResult = issueService.validateUpdate(currentUser, myIssue.id, issueInputParams)
//assert updateValidationResult.valid : updateValidationResult.errorCollection
//def updateResult = issueService.update(currentUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
//assert updateResult.valid : updateResult.errorCollection




// from adaptavista library
final jqlSearch = "status = Settled AND resolved is EMPTY ORDER BY resolved ASC"
def searchService = ComponentAccessor.getComponentOfType(SearchService)

// Parse the query
def parseResult = searchService.parseQuery(currentUser, jqlSearch)
if (!parseResult.valid) {
	mylogger.error("JQL Query error")
	mylogger.info("----------------ENDED FOR ERROR--------------------------------------------------------------------")
	return null
}


try {
	// Perform the query to get the issues
	def counter=0
	def results = searchService.search(currentUser, parseResult.query, PagerFilter.unlimitedFilter)
	def issues = results.results
	issues.each {
		mylogger.info("Issue: $it.key")
		counter=counter+1
	}

	mylogger.info("Number of issues found: $counter")
	//issues*.key
} catch (SearchException e) {
	e.printStackTrace()
	null
}






mylogger.info("----------------ENDED--------------------------------------------------------------------")