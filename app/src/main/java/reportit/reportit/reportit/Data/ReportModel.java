package reportit.reportit.reportit.Data;

public class ReportModel {
    String id, reporterID, imageurl, description, dateAndTome;
    double longitude, latitude;

    public ReportModel() {
    }

    public ReportModel(String id, String reporterID, String imageurl, String description, String dateAndTome, double longitude, double latitude) {
        this.id = id;
        this.reporterID = reporterID;
        this.imageurl = imageurl;
        this.description = description;
        this.dateAndTome = dateAndTome;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getDateAndTome() {
        return dateAndTome;
    }

    public void setDateAndTome(String dateAndTome) {
        this.dateAndTome = dateAndTome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterID() {
        return reporterID;
    }

    public void setReporterID(String reporterID) {
        this.reporterID = reporterID;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
