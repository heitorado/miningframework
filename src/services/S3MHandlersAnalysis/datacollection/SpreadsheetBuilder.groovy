package services.S3MHandlersAnalysis.datacollection

import main.project.MergeCommit
import main.project.Project
import services.S3MHandlersAnalysis.implementations.OutputProcessor
import services.S3MHandlersAnalysis.util.MergeCommitSummary
import services.S3MHandlersAnalysis.util.MergeScenarioSummary
import services.S3MHandlersAnalysis.util.Utils

import java.nio.file.Path

class SpreadsheetBuilder {
    private static final String GLOBAL_SPREADSHEET_HEADER = 'project,merge commit,number of modified files,number of TM conflicts,number of CT conflicts,number of SF conflicts,number of MM conflicts,number of KB conflicts,handlers have the same outputs,handlers have the same conflicts,notes,false positives,false negatives,travis builds,,,'
    private static final String COMMIT_SPREADSHEET_HEADER = 'project,merge commit,file,number of TM conflicts,number of CT conflicts,number of SF conflicts,number of MM conflicts,number of KB conflicts,CT text = SF text,CT text = MM text,CT text = KB text,SF text = MM text, SF text = KB text,MM text = KB text,CT conflicts = SF conflicts,CT conflicts = MM conflicts,CT conflicts = KB conflicts,SF conflicts = MM conflicts,SF conflicts = KB conflicts,MM conflicts = KB conflicts'
    private static final String SPREADSHEET_NAME = 'results.csv'

    static synchronized void buildSpreadsheets(Project project, MergeCommit mergeCommit, MergeCommitSummary summary) {
        buildGlobalSpreadsheet(project, mergeCommit, summary)
        buildCommitSpreadsheet(project, mergeCommit, summary.mergeScenarioSummaries)
    }

    private static void buildCommitSpreadsheet(Project project, MergeCommit mergeCommit, List<MergeScenarioSummary> summaries) {
        Path spreadsheetPath = Utils.commitFilesPath(project, mergeCommit).resolve(SPREADSHEET_NAME)
        File spreadsheet = spreadsheetPath.toFile()
        appendHeader(spreadsheet, COMMIT_SPREADSHEET_HEADER)

        summaries.each { summary ->
            appendLineToSpreadsheet(spreadsheet, appendAfterProjectAndMergeCommitLinks(project, mergeCommit, summary.toString()))
        }
    }

    private static void buildGlobalSpreadsheet(Project project, MergeCommit mergeCommit, MergeCommitSummary summary) {
        Path spreadsheetPath = Utils.getOutputPath().resolve(SPREADSHEET_NAME)
        File spreadsheet = spreadsheetPath.toFile()
        appendHeader(spreadsheet, GLOBAL_SPREADSHEET_HEADER)

        appendLineToSpreadsheet(spreadsheet, appendAfterProjectAndMergeCommitLinks(project, mergeCommit, summary.toString()))
    }

    private static void appendHeader(File spreadsheet, String header) {
        if (!spreadsheet.exists()) {
            appendLineToSpreadsheet(spreadsheet, header)
        }
    }

    private static String appendAfterProjectAndMergeCommitLinks(Project project, MergeCommit mergeCommit, String string) {
        String projectName = Utils.getHyperLink(OutputProcessor.ANALYSIS_REMOTE_URL + "/${project.getName()}", project.getName())
        String commitSHA = Utils.getHyperLink(OutputProcessor.ANALYSIS_REMOTE_URL + "/${project.getName()}/${mergeCommit.getSHA()}", mergeCommit.getSHA())
        return "${projectName},${commitSHA},${string}"
    }

    private static void appendLineToSpreadsheet(File spreadsheet, String line) {
        spreadsheet << "${line.replaceAll('\\\\', '/')}\n"
    }

}
