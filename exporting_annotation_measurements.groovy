import qupath.lib.gui.tools.MeasurementExporter
import qupath.lib.objects.PathAnnotationObject
import qupath.lib.gui.scripting.QPEx
import qupath.lib.projects.Project
import qupath.lib.projects.Projects

// Get current project
def project = getProject()

// Choose the columns to include in the export
def columnsToInclude = ["Object ID", "Positive %", "Area µm^2", "Perimeter µm","Num Positive per mm^2"] as String[]

// Output directory
def baseOutputDir = buildFilePath(PROJECT_BASE_DIR,'Annotation_level_data')
mkdirs(baseOutputDir)

// Loop through all images in the project
for (entry in project.getImageList()) {
    def imageData = entry.readImageData()
    QPEx.setBatchProjectAndImage(project, imageData)

    def imageName = entry.getImageName()
    println "Processing image: ${imageName}"

    // Set output file name based on image name
    def safeImageName = imageName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_")
    def outputFile = new File(buildFilePath(baseOutputDir, "${safeImageName}_annotations.csv"))

    // Create exporter
    def exporter = new MeasurementExporter()
                      .imageList([entry])
                      .separator(",")
                      .includeOnlyColumns(columnsToInclude)
                      .exportType(PathAnnotationObject.class)
                      .exportMeasurements(outputFile)

    println "Exported to: ${outputFile}"
}
println "All annotations exported."