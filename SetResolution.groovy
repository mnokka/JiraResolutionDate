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

def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()

MutableIssue myIssue = issueManager.getIssueObject("TESDATERES-1")
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
	it.toString == "Done"
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
mylogger.debug( "doneStamp: $doneStamp   ")

//Timestamp time = new Timestamp(119,7,5,13,41,0,0)
//myIssue.setResolutionDate(PastDate)
myIssue.setResolutionDate(doneStamp)


//def issueInputParams = issueService.newIssueInputParameters()
//issueInputParams.setResolutionDate(PastDate.toString())

//issueManager.updateIssue(currentUser, myIssue, EventDispatchOption.ISSUE_UPDATED, false)
myIssue.store() //works but usage not recommended anymore, the modern approach commented out does not work yet

//def updateValidationResult = issueService.validateUpdate(currentUser, myIssue.id, issueInputParams)
//assert updateValidationResult.valid : updateValidationResult.errorCollection

//def updateResult = issueService.update(currentUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
//assert updateResult.valid : updateResult.errorCollection



mylogger.info("----------------ENDED--------------------------------------------------------------------")