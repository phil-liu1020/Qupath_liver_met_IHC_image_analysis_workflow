import qupath.lib.objects.PathDetectionObject
import groovy.io.FileType
      
def annotations = getSelectedObjects()
def cal = getCurrentImageData().getServer().getPixelCalibration()

// Get pixel width in microns
def pixelWidth = cal.getPixelWidthMicrons()

// Store parameters for all annotations
def annotationParams = []

annotations.each { annotation ->
    def roi = annotation.getROI()  
    def points = roi.getAllPoints()  
    def centroidX = roi.getCentroidX()
    def centroidY = roi.getCentroidY()

    def distances = []  

    points.each { point -> 
        def pointX = point.x  
        def pointY = point.y
        def distance = pixelWidth * Math.sqrt((pointX - centroidX)**2 + (pointY - centroidY)**2)
        distances.add(distance)
    }

    def meanDistance = distances.sum() / distances.size()
    
    // Store expansion radius for each annotation
    annotationParams.add([annotation: annotation, radiusMicrons: meanDistance])
}

println "Expanding annotations..."

// **Run the dilation plugin once for all annotations**
def oldAnnotations = getAnnotationObjects()  // Store current annotations before expansion

runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', 
          "{\"radiusMicrons\": ${annotationParams[0].radiusMicrons}, \"lineCap\": \"ROUND\", \"removeInterior\": false, \"constrainToParent\": true}")

def newAnnotations = getAnnotationObjects() - oldAnnotations  // Get newly created annotations

// Remove classification from new annotations
newAnnotations.each { it.setPathClass(null) }

println "Expansion complete. New annotations have no classification."