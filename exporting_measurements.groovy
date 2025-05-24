import qupath.lib.objects.PathDetectionObject
import qupath.lib.projects.ProjectImageEntry
import qupath.lib.gui.scripting.QPEx

// Creates an output directory named "Detection_level_data"where exported measurements are saved.
def outputDir = buildFilePath(PROJECT_BASE_DIR, 'Detection_level_data')
mkdirs(outputDir)

// Define which measurements you would like to export.
def columnsToInclude = [
    "Detection Classification", "Grandparent Annotation ID", "Parent Annotation ID",
    "Parent Annotation Class", "Signed distance to annotation with Tumor µm","Centroid X µm","Centroid Y µm"
]

// Loop through all images in the project
for (entry in getProject().getImageList()) {

    // Open image
    def imageData = entry.readImageData()
    QPEx.setBatchProjectAndImage(getProject(), imageData)

    def imageName = entry.getImageName()
    println "Processing image: ${imageName}"

    def annotations = getAnnotationObjects()
    def measurementList = []

    for (annotation in annotations) {
        def detections = annotation.getChildObjects().findAll { it instanceof PathDetectionObject }

        for (detection in detections) {
            def parent = detection.getParent()
            def grandparent = parent?.getParent()
            def row = [
                detection.getPathClass()?.toString() ?: "Unclassified",
                grandparent?.getID() ?: "None",
                parent?.getID() ?: "None",
                parent?.getPathClass()?.toString() ?: "Unclassified",
                detection.getMeasurementList().getMeasurementValue("Signed distance to annotation with Tumor µm"),
                detection.getROI()?.getCentroidX() ?: "None",
                detection.getROI()?.getCentroidY() ?: "None"
            ]
            measurementList << row
        }
    }

    // Extract last 3 characters before ".svs" or equivalent extension
    def shortName = imageName.contains(".") ? imageName[0..imageName.lastIndexOf('.') - 1] : imageName
    def safeShortName = shortName.replaceAll("[^a-zA-Z0-9_\\-]", "_")
    def outputFile = buildFilePath(outputDir, "${safeShortName}_detections.csv")
    def writer = new FileWriter(outputFile)
    writer.append(columnsToInclude.join(",") + "\n")
    measurementList.each { row -> writer.append(row.join(",") + "\n") }
    writer.close()

    println "Saved: ${outputFile}"
}

print "All image exports completed!"
