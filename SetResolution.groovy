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


// CONFIGURATIONS *********************

int MonthsValue = 10 as Integer 

// END OF CONFIGURATIONS ****************



// set logging to Jira log
def Logger mylogger
mylogger = Logger.getLogger("ResolutioDater")
mylogger.setLevel(Level.DEBUG ) // or INFO (prod) or DEBUG (development)

mylogger.info("----------------STARTED--------------------------------------------------------------------")



def issueManager = ComponentAccessor.getIssueManager()
//MutableIssue myIssue = issueManager.getIssueObject("JIRA-1")


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



//Timestamp time = new Timestamp(119,7,5,13,41,0,0)
//myIssue.setResolutionDate(time)
//myIssue.store()


mylogger.info("----------------ENDED--------------------------------------------------------------------")