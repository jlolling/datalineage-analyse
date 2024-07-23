# Purpose
This library contains analyse features for
* Talend Jobs
* SQL Scripts
* Tableau workbooks and datasources

# Analysing Talend Jobs
## Initialize Project
The first thing to do is to initialize a Project object by the Talend project root folder (where the file talend.project is located).
```
// initialize the project and if this was already done in e.g. parent jobs return the already initialized Project.
de.jlo.analyse.talend.Project project = de.jlo.analyse.talend.Project.getProject(context.talend_project_root_folder); // typically use a context var to refer to the folder
project.setDefaultContext("production,Default"); // set the default context names necessary later to fetch the context values
if (project.getCountJobs() == 0) { // check if the
  throw new Exception("Project does not have no jobs! Check the project root path: " + context.talend_project_root_folder);	
}
```

## Read embedded jobs for job identified by name
You need to habe the job name as String (e.g. in a context variable). The following code runs for one job name
```
// get the project (if it is not already done it will be done)
de.jlo.analyse.talend.Project project = de.jlo.analyse.talend.Project.getProject(context.talend_project_root_folder);
if (project.getCountJobs() == 0) {
	throw new Exception("Project does not have no jobs! Check the project root path: " + context.talend_project_root_folder);	
}
de.jlo.analyse.talend.Job job = project.getLatestJob(context.job_name);
if (job == null) {
	throw new Exception("Job with name: " + context.job_name + " does not exist");
}
log.info("Start analysing job: " + job.getJobName());
// Create a parser for analyse embedded jobs
de.jlo.analyse.talend.AnalyseEmbeddedJobs parser = new de.jlo.analyse.talend.AnalyseEmbeddedJobs(job);
globalMap.put("parser", parser); // put the parser to the globalMap because we need more the one flow out of it
try {
	parser.analyse();
} catch (Exception e) {
	globalMap.put("errorLog", e.getMessage());
}
```
Now in a new flow get the parser and get the job chain
Do it in a tJavaFlex in the Start part
```
de.jlo.analyse.talend.AnalyseEmbeddedJobs parser = (de.jlo.analyse.talend.AnalyseEmbeddedJobs) globalMap.get("parser");
List<String> jobs = parser.getEmbeddedJobNames();
for (String job : jobs) {
```
In the Main part set the value for the schema column "child_job_name" in the flow named here "read" 
```
read.child_job_name = job;
```
In the End part close the loop
```
}
```

## Analyse the read/written/created tables of a job
This analysis works for one job. You need to iterate through the jobs in a parent job and run this analysis in a child job running for one job.
```
de.jlo.analyse.talend.Project project = de.jlo.analyse.talend.Project.getProject(context.talend_project_root_folder);
project.setDefaultContext("production,Default");
if (project.getCountJobs() == 0) {
	throw new Exception("Project does not have no jobs! Check the project root path: " + context.talend_project_root_folder);	
}
de.jlo.analyse.talend.Job job = project.getLatestJob(context.job_name);
if (job == null) {
	throw new Exception("Job with name: " + context.job_name + " does not exist");
}
globalMap.put("job", job);
```
Because table names could be potentially build by context variables and and also one job could work for different tables in different task it is important to run this analysis in context of a task and provide the whole task-specific context variables to the job. The next code assumes you read in a flow the context variables (parameter name and value) and let the flow end in a tJavaRow.
One good choice is to read them from the TMC via TMC API (get tasks)
```
de.jlo.analyse.talend.Job job = (de.jlo.analyse.talend.Job) globalMap.get("job");
job.addReplaceContextVariable(input_row.parameter_name, input_row.parameter_value);
```
Now analyse the job for table usage. We use here the Job object previously put into the globalMap.
We keep the new parser in the globalMap to use it in various flows for the results.
```
de.jlo.analyse.talend.Job job = (de.jlo.analyse.talend.Job) globalMap.get("job");
log.info("Start analysing job: " + job.getJobName());
de.jlo.analyse.talend.AnalyseTables parser = new de.jlo.analyse.talend.AnalyseTables(job);
globalMap.put("parser", parser); // put the parser to the globalMap to have it later
globalMap.remove("errorLog");
try {
	parser.analyseTables();
} catch (Exception e) {
	globalMap.put("errorLog", e.getMessage());
}
```
Now we are ready to get the results.
Get the tables read first
In a tJavaFlex add this to the Start part:
```
de.jlo.analyse.talend.AnalyseTables parser = (de.jlo.analyse.talend.AnalyseTables) globalMap.get("parser");
List<de.jlo.analyse.DatabaseTable> tables = parser.getListInputTables(); // get the read tables
for (de.jlo.analyse.DatabaseTable table : tables) {
```
In the same tJavaFlex add this to the Main part. It assumes the flow is named "read"
The flow needs a variable table_name for the table and database_host for the database (a job can use tables from many differnt database and we differentiate between them by the host.
The database and also the db schema name is part of the table name.
```
read.table_name = table.getTableName();
read.database_host = table.getDatabaseHost();
```
Get the tables written
In a tJavaFlex add this to the Start part:
```
de.jlo.analyse.talend.AnalyseTables parser = (de.jlo.analyse.talend.AnalyseTables) globalMap.get("parser");
List<de.jlo.analyse.DatabaseTable> tables = parser.getListOutputTables(); // get the written tables
for (de.jlo.analyse.DatabaseTable table : tables) {
```
In the same tJavaFlex add this to the Main part. It assumes the flow is named "write"
The flow needs a variable table_name for the table and database_host for the database (a job can use tables from many differnt database and we differentiate between them by the host.
The database and also the db schema name is part of the table name.
```
write.table_name = table.getTableName();
write.database_host = table.getDatabaseHost();
```
Get the tables created/dropped
In a tJavaFlex add this to the Start part:
```
de.jlo.analyse.talend.AnalyseTables parser = (de.jlo.analyse.talend.AnalyseTables) globalMap.get("parser");
List<de.jlo.analyse.DatabaseTable> tables = parser.getListCreateTables(); // get the created tables
for (de.jlo.analyse.DatabaseTable table : tables) {
```
In the same tJavaFlex add this to the Main part. It assumes the flow is named "create"
The flow needs a variable table_name for the table and database_host for the database (a job can use tables from many differnt database and we differentiate between them by the host.
The database and also the db schema name is part of the table name.
```
create.table_name = table.getTableName();
create.database_host = table.getDatabaseHost();
```
