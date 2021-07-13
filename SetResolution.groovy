// Set issue's Resolutiondate to fixed date stamp, based on history when issue originally was solved
// Used to force set some issues which Resolved Date is missing due some bulk changes to issue
// Detection logic needs changes if issue has been resolved several times, scoped out in this script
// JQL search part from Adaptavista library
//
// mika.nokka1@gmail.com 22.6.2021 



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
import com.atlassian.jira.issue.resolution.Resolution

// CONFIGURATIONS *********************

int MonthsValue = 20 as Integer 
final String targetState="Settled" //Done
final String testissue="NB1394NCM-334"

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
def currentUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser


// TEST LOGIC BEFORE BULK DRIVE
Issue  myIssue = issueManager.getIssueObject(testissue)

Date CurrentDate=new Date()
mylogger.debug( "CurrentDate: $CurrentDate   ")
// from import groovy.time.TimeCategory
def timepast
Timestamp PastDate
use(TimeCategory) {
	timepast=CurrentDate - MonthsValue.months // testing if time can really be changed
	PastDate=new Timestamp(timepast.getTime())
}
mylogger.debug( "Test New date: $timepast   ")
mylogger.debug( "Test New datestamp: $PastDate   ")

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
mylogger.debug( "Test doneTime: $doneTime   ")
mylogger.debug( "Test doneDate: $doneDate   ")
mylogger.debug( "Test orig settleddoneStamp: $doneStamp   ")
currentresdate=myIssue.getResolutionDate() 
mylogger.debug( "Test currentresdate: $currentresdate   ")

FindAndSetResolutionDate(testissue,currentUser,targetState,mylogger,issueManager,changeHistoryManager)
return 
// END OF TEST LOGIC




// Actual operations
// 1) Do JQL query for wished issues (with missing Resolved info)
// 2) Go through all issues, find when issue was origivally solved, force set Resolved Date

// from adaptavista library
final jqlSearch = "(Project = \"NB1395 Noteworthy Claim Management\" ) AND (status = Settled) AND (resolved is EMPTY) ORDER BY resolved ASC"
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
		mylogger.info("Count:$counter Issue: $it.key")
		counter=counter+1
		FindAndSetResolutionDate(it.key,currentUser,targetState,mylogger,issueManager,changeHistoryManager)
	}

	mylogger.info("Number of issues found: $counter")
	//issues*.key // ??
} catch (SearchException e) {
	e.printStackTrace()
	null
}




// check original revoslde date fron history and force set
def FindAndSetResolutionDate(String issuekey,currentUser,String targetState,mylogger,issueManager,changeHistoryManager) {
	

	Issue myIssue =issueManager.getIssueObject(issuekey)

		
	// check when issuw was acttually resolved, in this case when moved to for example "Done" state
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
	
	mylogger.debug( "Entered issus state ($targetState): $doneTime   ")
	mylogger.debug( "Entered issus state ($targetState) date: $doneDate   ")
	mylogger.debug( "Entered issus state ($targetState) stamp: $doneStamp   ")
	
	currentresdate=myIssue.getResolutionDate()
	mylogger.debug( "Current getResolutionDate: $currentresdate   ")
	
	// Activate for changes to be done
	myIssue.setResolutionDate(doneStamp)
	myIssue.setResolved(doneStamp)  // REMOBRE ME
	//
	myIssue.setResolution("Done")
	myIssue.store() // WARNING: works but usage not recommended anymore, the modern approach commented out does not work yet
	mylogger.debug( "Done ResolotionDate setting for: $issuekey  -> $doneStamp")
	mylogger.info("--------------------------------------------------------------------------------")
}



mylogger.info("----------------ENDED--------------------------------------------------------------------")