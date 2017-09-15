/*
Copyright (c) 2007 The Regents of the University of California

Permission to use, copy, modify, and distribute this software and its documentation
for educational, research and non-profit purposes, without fee, and without a written
agreement is hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Permission to make commercial use of this software may be obtained
by contacting:
Technology Transfer Office
9500 Gilman Drive, Mail Code 0910
University of California
La Jolla, CA 92093-0910
(858) 534-5815
invent@ucsd.edu

THIS SOFTWARE IS PROVIDED BY THE REGENTS OF THE UNIVERSITY OF CALIFORNIA AND
CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.wildid.app;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.wildid.entity.CameraTrap;
import org.wildid.entity.CameraTrapArray;
import org.wildid.entity.Event;
import org.wildid.entity.HibernateUtil;
import org.wildid.entity.Project;

public class ExportImageMetadata {

    private final File exportFile;
    private final Project project;
    private final Event event;
    private final CameraTrapArray ctArray;
    private final CameraTrap cameraTrap;
    private Workbook workbook;
    static Logger log = Logger.getLogger(ExportImageMetadata.class.getName());

    public static final String[] columnNames = new String[]{
        "ID",
        "Project Name",
        "Camera Trap Name",
        "Latitude",
        "Longitude",
        "Sampling Event",
        "Photo Type",
        "Photo Date",
        "Photo time",
        "Raw Name",
        "Class",
        "Order",
        "Family",
        "Genus",
        "Species",
        "Number of Animals",
        "Person Identifying the Photo",
        "Camera Serial Number",
        "Camera Start Date",
        "Camera End Date",
        "Person setting up the Camera",
        "Person picking up the Camera",
        "Camera Manufacturer",
        "Camera Model",
        "Sequence Info",
        "Moon Phase",
        "Temperature",
        "Organization Name"
    };

    public ExportImageMetadata(
            File exportFile,
            Project project,
            Event event,
            CameraTrapArray ctArray,
            CameraTrap cameraTrap
    ) {
        this.exportFile = exportFile;
        this.project = project;
        this.event = event;
        this.ctArray = ctArray;
        this.cameraTrap = cameraTrap;
    }

    public void exportExcel() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.doWork(connection -> exportExcel(connection));
        s.close();
    }

    public void exportCSV() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.doWork(connection -> exportCSV(connection));
        s.close();
    }

    public void exportWCS(boolean withImages) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        s.doWork(connection -> exportWCS(connection, withImages));
        s.close();
    }

    public void exportWCS(Connection conn, boolean withImages) {
        try {
            boolean isXls = exportFile.getName().endsWith(".xls");
            InputStream is = WildID.class.getResourceAsStream("/resources/wcs_template.xls" + (isXls ? "" : "x"));

            if (isXls) {
                workbook = WorkbookFactory.create(is);
            } else {
                workbook = (XSSFWorkbook) WorkbookFactory.create(is);
            }
            is.close();

            Sheet sheet_project = workbook.getSheetAt(0);
            Sheet sheet_camera = workbook.getSheetAt(1);
            Sheet sheet_deployment = workbook.getSheetAt(2);
            Sheet sheet_image = workbook.getSheetAt(3);

            addData2Sheet(conn, sheet_project, getWcsProjectQuery(), 1);
            addData2Sheet(conn, sheet_camera, getWcsCameraQuery(), 1);
            addData2Sheet(conn, sheet_deployment, getWcsDeploymentQuery(), 1);
            addData2Sheet(conn, sheet_image, getWcsImageQuery(withImages), 1);

            FileOutputStream fos = new FileOutputStream(exportFile);
            workbook.write(fos);
            fos.close();

        } catch (FileNotFoundException ex) {
            log.info(ex.getMessage());
        } catch (IOException ex) {
            log.info(ex.getMessage());
        } catch (InvalidFormatException | EncryptedDocumentException ex) {
            log.info(ex.getMessage());
        }
    }

    public void addData2Sheet(Connection conn, Sheet sheet, String query, int startRowIndex) {
        try {
            Map<Integer, Object[]> data = new TreeMap<>();

            int ind = 1;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            while (rs.next()) {
                Object[] obj = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    obj[i] = rs.getString(i + 1);
                }
                data.put(++ind, obj);
            }

            rs.close();
            stmt.close();

            Set<Integer> keyset = data.keySet();

            for (int key : keyset) {
                Row row = sheet.createRow(startRowIndex++);

                Object[] objArr = data.get(key);
                int cellnum = 0;

                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellnum++);
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    }
                }
            }
        } catch (SQLException ex) {
            log.info(ex.getMessage());
        }
    }

    public void exportExcel(Connection conn) {

        try {
            boolean isXls = exportFile.getName().endsWith(".xls");

            String sql = getExportImageMetadataQuery();

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            if (isXls) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }
            Sheet sheet = workbook.createSheet("Export Image Metadata");
            sheet.createFreezePane(0, 1);

            Map<Integer, Object[]> data = new TreeMap<>();

            data.put(1, columnNames);

            int ind = 1;
            while (rs.next()) {
                data.put(++ind, new Object[]{
                    rs.getInt("ID"),
                    rs.getString("Project Name"),
                    rs.getString("Camera Trap Name"),
                    rs.getString("Latitude"),
                    rs.getString("Longitude"),
                    rs.getString("Sampling Event"),
                    rs.getString("Photo Type"),
                    rs.getString("Photo Date"),
                    rs.getString("Photo time"),
                    rs.getString("Raw Name"),
                    rs.getString("Class"),
                    rs.getString("Order"),
                    rs.getString("Family"),
                    rs.getString("Genus"),
                    rs.getString("Species"),
                    rs.getString("Number of Animals"),
                    rs.getString("Person Identifying the Photo"),
                    rs.getString("Camera Serial Number"),
                    rs.getString("Camera Start Date"),
                    rs.getString("Camera End Date"),
                    rs.getString("Person setting up the Camera"),
                    rs.getString("Person picking up the Camera"),
                    rs.getString("Camera Manufacturer"),
                    rs.getString("Camera Model"),
                    rs.getString("Sequence Info"),
                    rs.getString("Moon Phase"),
                    rs.getString("Temperature"),
                    rs.getString("Organization Name")
                });
            }

            Font boldFont = workbook.createFont();
            boldFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

            CellStyle style = workbook.createCellStyle();
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
            style.setAlignment(CellStyle.ALIGN_CENTER);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            style.setFont(boldFont);

            Set<Integer> keyset = data.keySet();
            int rownum = 0;

            for (int key : keyset) {
                Row row = sheet.createRow(rownum++);

                Object[] objArr = data.get(key);
                int cellnum = 0;

                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellnum++);
                    if (obj instanceof String) {
                        cell.setCellValue((String) obj);
                    } else if (obj instanceof Integer) {
                        cell.setCellValue((Integer) obj);
                    }

                    if (rownum == 1) {
                        cell.setCellStyle(style);
                        row.setHeightInPoints(30);
                    }
                }
            }

            FileOutputStream out;

            out = new FileOutputStream(exportFile);
            workbook.write(out);
            out.close();

        } catch (SQLException ex) {
            log.info(ex.getMessage());
        } catch (FileNotFoundException ex) {
            log.info(ex.getMessage());
        } catch (IOException ex) {
            log.info(ex.getMessage());
        }
    }

    public void exportCSV(Connection conn) {
        try {
            String sql = getExportImageMetadataQuery();

            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    FileWriter fw = new FileWriter(exportFile, false);

                    try (CSVWriter csvWriter = new CSVWriter(fw, ',')) {
                        csvWriter.writeAll(rs, true);
                        csvWriter.close();
                    }
                }
            }
        } catch (SQLException | IOException ex) {
            log.info(ex.getMessage());
        }

    }

    public String getExportImageMetadataQuery() {
        StringBuilder sql = new StringBuilder("");

        sql.append("SELECT ");
        sql.append("    IMAGE.IMAGE_ID AS \"ID\", ");
        sql.append("    PROJECT.NAME AS \"Project Name\", ");
        sql.append("    CAMERA_TRAP.NAME AS \"Camera Trap Name\", ");
        sql.append("    CAMERA_TRAP.LATITUDE AS \"Latitude\", ");
        sql.append("    CAMERA_TRAP.LONGITUDE AS \"Longitude\", ");
        sql.append("    EVENT.NAME AS \"Sampling Event\", ");
        sql.append("    IMAGE_TYPE.NAME AS \"Photo Type\", ");
        sql.append("    DATE_FORMAT(IMAGE.TIME_CAPTURED, '%Y-%m-%d') AS \"Photo Date\", ");
        sql.append("    TIME_FORMAT(IMAGE.TIME_CAPTURED, '%H:%i:%S') AS \"Photo time\", ");
        sql.append("    IMAGE.RAW_NAME AS \"Raw Name\", ");
        sql.append("    FAMILY_GENUS_SPECIES.CLASS AS \"Class\", ");
        sql.append("    FAMILY_GENUS_SPECIES.ORDER_TAXA AS \"Order\", ");
        sql.append("    FAMILY_GENUS_SPECIES.FAMILY AS \"Family\", ");
        sql.append("    FAMILY_GENUS_SPECIES.GENUS AS \"Genus\", ");
        sql.append("    FAMILY_GENUS_SPECIES.SPECIES AS \"Species\", ");
        sql.append("    IMAGE_SPECIES.SUBSPECIES AS \"Subspecies\", ");
        sql.append("    IMAGE_SPECIES.INDIVIDUAL_COUNT AS \"Number of Animals\", ");
        sql.append("    CONCAT(PERSON.FIRST_NAME, ' ', PERSON.LAST_NAME) AS \"Person Identifying the Photo\", ");
        sql.append("    CAMERA.SERIAL_NUMBER AS \"Camera Serial Number\", ");
        sql.append("    DATE_FORMAT(DEPLOYMENT.START_TIME, '%Y-%m-%d') AS \"Camera Start Date\", ");
        sql.append("    DATE_FORMAT(DEPLOYMENT.END_TIME, '%Y-%m-%d') AS \"Camera End Date\", ");
        sql.append("    CONCAT(PERSON_SETUP.FIRST_NAME, ' ', PERSON_SETUP.LAST_NAME) AS \"Person setting up the Camera\", ");
        sql.append("    CONCAT(PERSON_PICKUP.FIRST_NAME, ' ', PERSON_PICKUP.LAST_NAME) AS \"Person picking up the Camera\", ");
        sql.append("    CAMERA_MODEL.MAKER AS \"Camera Manufacturer\", ");
        sql.append("    CAMERA_MODEL.NAME AS \"Camera Model\", ");
        sql.append("    EXIF.SEQUENCE_INFO AS \"Sequence Info\", ");
        sql.append("    EXIF.MOON_PHASE AS \"Moon Phase\", ");
        sql.append("    EXIF.TEMPERATURE AS \"Temperature\", ");
        sql.append("    ORGS.NAMES AS \"Organization Name\" ");
        sql.append("FROM ");
        sql.append("    IMAGE ");
        sql.append("        LEFT JOIN IMAGE_TYPE ON (IMAGE.IMAGE_TYPE_ID = IMAGE_TYPE.IMAGE_TYPE_ID) ");
        sql.append("        LEFT JOIN IMAGE_SPECIES ON (IMAGE_SPECIES.IMAGE_ID = IMAGE.IMAGE_ID) ");
        sql.append("        LEFT JOIN PERSON ON (IMAGE_SPECIES.IDENTIFY_PERSON_ID = PERSON.PERSON_ID) ");
        sql.append("        LEFT JOIN FAMILY_GENUS_SPECIES ON (FAMILY_GENUS_SPECIES.FAMILY_GENUS_SPECIES_ID = IMAGE_SPECIES.FAMILY_GENUS_SPECIES_ID) ");
        sql.append("        LEFT JOIN ( ");
        sql.append("            SELECT ");
        sql.append("                IMAGE.IMAGE_ID, ");
        sql.append("                IMAGE_EXIF_1.EXIF_TAG_VALUE AS CAPTURE_TIME, ");
        sql.append("                IMAGE_EXIF_2.EXIF_TAG_VALUE AS MOON_PHASE, ");
        sql.append("                IMAGE_EXIF_3.EXIF_TAG_VALUE AS TEMPERATURE, ");
        sql.append("                IMAGE_EXIF_4.EXIF_TAG_VALUE AS SEQUENCE_INFO, ");
        sql.append("                IMAGE_EXIF_5.EXIF_TAG_VALUE AS SERIAL_NUMBER, ");
        sql.append("                IMAGE_EXIF_6.EXIF_TAG_VALUE AS CAMERA_MAKER, ");
        sql.append("                IMAGE_EXIF_7.EXIF_TAG_VALUE AS CAMERA_MODEL_NAME, ");
        sql.append("                IMAGE_EXIF_8.EXIF_TAG_VALUE AS FIRMWARE_VERSION ");
        sql.append("            FROM ");
        sql.append("                IMAGE ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_1 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_1.IMAGE_ID AND IMAGE_EXIF_1.IMAGE_FEATURE_ID = 1) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_2 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_2.IMAGE_ID AND IMAGE_EXIF_2.IMAGE_FEATURE_ID = 2) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_3 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_3.IMAGE_ID AND IMAGE_EXIF_3.IMAGE_FEATURE_ID = 3) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_4 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_4.IMAGE_ID AND IMAGE_EXIF_4.IMAGE_FEATURE_ID = 4) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_5 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_5.IMAGE_ID AND IMAGE_EXIF_5.IMAGE_FEATURE_ID = 5) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_6 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_6.IMAGE_ID AND IMAGE_EXIF_6.IMAGE_FEATURE_ID = 6) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_7 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_7.IMAGE_ID AND IMAGE_EXIF_7.IMAGE_FEATURE_ID = 7) ");
        sql.append("                    LEFT JOIN IMAGE_EXIF AS IMAGE_EXIF_8 ON (IMAGE.IMAGE_ID = IMAGE_EXIF_8.IMAGE_ID AND IMAGE_EXIF_8.IMAGE_FEATURE_ID = 8) ");
        sql.append("        ) AS EXIF ON (IMAGE.IMAGE_ID = EXIF.IMAGE_ID) ");
        sql.append("        , ");
        sql.append("    PROJECT ");
        sql.append("    LEFT JOIN ( ");
        sql.append("      SELECT PROJECT.PROJECT_ID AS PROJECT_ID, GROUP_CONCAT(ORGANIZATION.NAME SEPARATOR '; ') AS NAMES ");
        sql.append("      FROM PROJECT, ORGANIZATION, PROJECT_ORGANIZATION ");
        sql.append("      WHERE ");
        sql.append("          PROJECT_ORGANIZATION.PROJECT_ID=PROJECT.PROJECT_ID ");
        sql.append("          AND PROJECT_ORGANIZATION.ORGANIZATION_ID=ORGANIZATION.ORGANIZATION_ID ");
        sql.append("      GROUP BY PROJECT.PROJECT_ID ");
        sql.append("    ) ORGS ON PROJECT.PROJECT_ID = ORGS.PROJECT_ID, ");
        sql.append("    DEPLOYMENT ");
        sql.append("        LEFT JOIN PERSON AS PERSON_SETUP ON (DEPLOYMENT.SET_PERSON_ID = PERSON_SETUP.PERSON_ID) ");
        sql.append("        LEFT JOIN PERSON AS PERSON_PICKUP ON (DEPLOYMENT.SET_PERSON_ID = PERSON_PICKUP.PERSON_ID), ");
        sql.append("    IMAGE_SEQUENCE, ");
        sql.append("    EVENT, ");
        sql.append("    CAMERA_TRAP, ");
        sql.append("    CAMERA, ");
        sql.append("    CAMERA_MODEL ");
        sql.append("WHERE ");
        sql.append("    EVENT.PROJECT_ID = PROJECT.PROJECT_ID ");
        sql.append("    AND EVENT.EVENT_ID = DEPLOYMENT.EVENT_ID ");
        sql.append("    AND DEPLOYMENT.DEPLOYMENT_ID = IMAGE_SEQUENCE.DEPLOYMENT_ID ");
        sql.append("    AND IMAGE.IMAGE_SEQUENCE_ID = IMAGE_SEQUENCE.IMAGE_SEQUENCE_ID ");
        sql.append("    AND DEPLOYMENT.CAMERA_TRAP_ID = CAMERA_TRAP.CAMERA_TRAP_ID ");
        sql.append("    AND CAMERA.CAMERA_ID = DEPLOYMENT.CAMERA_ID ");
        sql.append("    AND CAMERA.CAMERA_MODEL_ID = CAMERA_MODEL.CAMERA_MODEL_ID ");
        sql.append("    AND CAMERA.CAMERA_MODEL_ID = CAMERA_MODEL.CAMERA_MODEL_ID ");

        return sql.toString() + getConditionQuery();
    }

    public String getTotalCountQuery() {
        StringBuilder sql = new StringBuilder("");

        sql.append("select count(*) as COUNT ");
        sql.append("from image, project, deployment, event, camera_trap, image_sequence ");
        sql.append("where ");
        sql.append("    event.project_id = project.project_id ");
        sql.append("    and event.event_id = deployment.event_id ");
        sql.append("    and deployment.deployment_id = image_sequence.deployment_id ");
        sql.append("    and image.image_sequence_id = image_sequence.image_sequence_id ");
        sql.append("    and deployment.camera_trap_id = camera_trap.camera_trap_id ");

        return sql.toString() + getConditionQuery();
    }

    public String getWcsProjectQuery() {
        StringBuilder sql = new StringBuilder("");
        sql.append(" SELECT");
        sql.append("     PROJECT.ABBREV_NAME AS \"Project Id\",");
        sql.append("     CURRENT_DATE AS \"Publish Date\",");
        sql.append("     PROJECT.NAME AS \"Project Name\",");
        sql.append("     PROJECT.OBJECTIVE AS \"Project Objectives\",");
        sql.append("     ORGS.NAMES AS \"Project Owner (organization or individual)\",");
        sql.append("     '' AS \"Project Owner Email (if applicable)\",");
        sql.append("     PI_PERSON.FULL_NAME AS \"Principal Investigator\",");
        sql.append("     PI_PERSON.EMAIL AS \"Principal Investigator Email\",");
        sql.append("     CONTACT_PERSON.FULL_NAME AS \"Project Contact\",");
        sql.append("     CONTACT_PERSON.EMAIL AS \"Project Contact Email\",");
        sql.append("     COUNTRY.CODE AS \"Country Code\",");
        sql.append("     PROJECT.USE_AND_CONSTRAINTS AS \"Project Data Use and Constraints\" ");
        sql.append(" FROM ");
        sql.append("     PROJECT ");
        sql.append("     LEFT JOIN ( ");
        sql.append("         SELECT PROJECT.PROJECT_ID AS PROJECT_ID, GROUP_CONCAT(ORGANIZATION.NAME SEPARATOR '; ') AS NAMES ");
        sql.append("         FROM PROJECT, ORGANIZATION, PROJECT_ORGANIZATION");
        sql.append("         WHERE");
        sql.append("             PROJECT_ORGANIZATION.PROJECT_ID=PROJECT.PROJECT_ID ");
        sql.append("             AND PROJECT_ORGANIZATION.ORGANIZATION_ID=ORGANIZATION.ORGANIZATION_ID ");
        sql.append("         GROUP BY PROJECT.PROJECT_ID ");
        sql.append("     ) ORGS ON PROJECT.PROJECT_ID = ORGS.PROJECT_ID");
        sql.append("     LEFT JOIN COUNTRY ON (COUNTRY.COUNTRY_ID = PROJECT.COUNTRY_ID)");
        sql.append("     LEFT JOIN (");
        sql.append("         SELECT");
        sql.append("             CONCAT(PERSON.FIRST_NAME, ' ', PERSON.LAST_NAME) AS FULL_NAME,");
        sql.append("             PERSON.EMAIL AS EMAIL,");
        sql.append("             PROJECT.PROJECT_ID AS PROJECT_ID");
        sql.append("         FROM PERSON, PROJECT_PERSON_ROLE, PROJECT, ROLE");
        sql.append("         WHERE");
        sql.append("             PROJECT_PERSON_ROLE.PROJECT_ID = PROJECT.PROJECT_ID");
        sql.append("             AND PROJECT_PERSON_ROLE.PERSON_ID = PERSON.PERSON_ID");
        sql.append("             AND PROJECT_PERSON_ROLE.ROLE_ID = ROLE.ROLE_ID");
        sql.append("             AND ROLE.NAME = 'Principal Investigator'");
        sql.append("     ) PI_PERSON ON PROJECT.PROJECT_ID = PI_PERSON.PROJECT_ID");
        sql.append("     LEFT JOIN (");
        sql.append("         SELECT");
        sql.append("             CONCAT(PERSON.FIRST_NAME, ' ', PERSON.LAST_NAME) AS FULL_NAME,");
        sql.append("             PERSON.EMAIL AS EMAIL,");
        sql.append("             PROJECT.PROJECT_ID AS PROJECT_ID");
        sql.append("         FROM PERSON, PROJECT_PERSON_ROLE, PROJECT, ROLE");
        sql.append("         WHERE");
        sql.append("             PROJECT_PERSON_ROLE.PROJECT_ID = PROJECT.PROJECT_ID");
        sql.append("             AND PROJECT_PERSON_ROLE.PERSON_ID = PERSON.PERSON_ID");
        sql.append("             AND PROJECT_PERSON_ROLE.ROLE_ID = ROLE.ROLE_ID");
        sql.append("             AND ROLE.NAME = 'Contact Person'");
        sql.append("     ) CONTACT_PERSON ON PROJECT.PROJECT_ID = CONTACT_PERSON.PROJECT_ID ");
        sql.append("     WHERE PROJECT.PROJECT_ID = ").append(project.getProjectId());

        return sql.toString();
    }

    public String getWcsCameraQuery() {
        StringBuilder sql = new StringBuilder("");

        sql.append("SELECT DISTINCT ");
        sql.append("    PROJECT.ABBREV_NAME AS \"Project ID\", ");
        sql.append("    CAMERA.SERIAL_NUMBER AS \"Camera id\", ");
        sql.append("    CAMERA_MODEL.MAKER AS \"Make\", ");
        sql.append("    CAMERA_MODEL.NAME AS \"Model\", ");
        sql.append("    CAMERA.SERIAL_NUMBER AS \"Serial Number\", ");
        sql.append("    CAMERA.YEAR_PURCHASED AS \"Year Purchased\" ");
        sql.append("FROM ");
        sql.append("    PROJECT, ");
        sql.append("    DEPLOYMENT, ");
        sql.append("    EVENT, ");
        sql.append("    CAMERA_TRAP, ");
        sql.append("    CAMERA, ");
        sql.append("    CAMERA_MODEL ");
        sql.append("WHERE ");
        sql.append("    PROJECT.PROJECT_ID = EVENT.PROJECT_ID ");
        sql.append("    AND EVENT.EVENT_ID = DEPLOYMENT.EVENT_ID ");
        sql.append("    AND DEPLOYMENT.CAMERA_TRAP_ID = CAMERA_TRAP.CAMERA_TRAP_ID ");
        sql.append("    AND CAMERA.CAMERA_ID = DEPLOYMENT.CAMERA_ID ");
        sql.append("    AND CAMERA.CAMERA_MODEL_ID = CAMERA_MODEL.CAMERA_MODEL_ID ");
        sql.append("    AND CAMERA.CAMERA_MODEL_ID = CAMERA_MODEL.CAMERA_MODEL_ID ");

        return sql.toString() + getConditionQuery();
    }

    public String getWcsDeploymentQuery() {
        StringBuilder sql = new StringBuilder("");

        sql.append("SELECT ");
        sql.append("        CASE ");
        sql.append("            WHEN (DEPLOYMENT.NAME IS NULL OR DEPLOYMENT.NAME = '') ");
        sql.append("                THEN CONCAT(PROJECT.ABBREV_NAME, '_', REPLACE(EVENT.NAME, ' ', '_'), '_', REPLACE(CAMERA_TRAP.NAME, ' ', '_'))");
        sql.append("            ELSE ");
        sql.append("                REPLACE(DEPLOYMENT.NAME, ' ', '_') ");
        sql.append("        END AS \"Deployment ID\", ");
        sql.append("    EVENT.NAME AS \"Event Name\", ");
        sql.append("    CAMERA_TRAP_ARRAY.NAME AS \"Array Name\", ");
        sql.append("    CAMERA_TRAP.NAME AS \"Deployment Location ID\", ");
        sql.append("    CAMERA_TRAP.LONGITUDE AS \"Longitude Resolution\", ");
        sql.append("    CAMERA_TRAP.LATITUDE AS \"Latitude Resolution\", ");
        sql.append("    DATE_FORMAT(DEPLOYMENT.START_TIME, '%Y-%M-%D') AS \"Camera Deployment Begin Date\", ");
        sql.append("    DATE_FORMAT(DEPLOYMENT.END_TIME, '%Y-%M-%D') AS \"Camera Deployment End Date\", ");
        sql.append("    BAIT_TYPE.NAME AS \"Bait Type\", ");
        sql.append("    BAIT_TYPE.description AS \"Bait Description\", ");
        sql.append("    FEATURE_TYPE.NAME AS \"Feature Type\", ");
        sql.append("    FEATURE_TYPE.METHODLOGY AS \"Feature Type Methodology\", ");
        sql.append("    CAMERA.SERIAL_NUMBER AS \"Camera ID\", ");
        sql.append("    '' AS \"Quiet Period Setting\", ");
        sql.append("    '' AS \"Restriction on Access\", ");
        sql.append("    DEPLOYMENT.FAILURE_DETAIL AS \"Camera Failure Details\", ");
        sql.append("    FAILURE_TYPE.NAME AS \"Camera Hardware Failure\" ");
        sql.append("FROM ");
        sql.append("    PROJECT ");
        sql.append("        LEFT JOIN CAMERA_TRAP_ARRAY ON (CAMERA_TRAP_ARRAY.PROJECT_ID = PROJECT.PROJECT_ID), ");
        sql.append("    DEPLOYMENT ");
        sql.append("        LEFT JOIN BAIT_TYPE ON (DEPLOYMENT.BAIT_TYPE_ID = BAIT_TYPE.BAIT_TYPE_ID) ");
        sql.append("        LEFT JOIN FEATURE_TYPE ON (DEPLOYMENT.FEATURE_TYPE_ID = FEATURE_TYPE.FEATURE_TYPE_ID) ");
        sql.append("        LEFT JOIN FAILURE_TYPE ON (DEPLOYMENT.FAILURE_TYPE_ID = FAILURE_TYPE.FAILURE_TYPE_ID), ");
        sql.append("    EVENT, ");
        sql.append("    CAMERA_TRAP, ");
        sql.append("    CAMERA ");
        sql.append("WHERE ");
        sql.append("    PROJECT.PROJECT_ID = EVENT.PROJECT_ID ");
        sql.append("    AND EVENT.EVENT_ID = DEPLOYMENT.EVENT_ID ");
        sql.append("    AND DEPLOYMENT.CAMERA_TRAP_ID = CAMERA_TRAP.CAMERA_TRAP_ID ");
        sql.append("    AND CAMERA.CAMERA_ID = DEPLOYMENT.CAMERA_ID ");
        sql.append("    AND CAMERA_TRAP_ARRAY.CAMERA_TRAP_ARRAY_ID = CAMERA_TRAP.CAMERA_TRAP_ARRAY_ID ");

        return sql.toString() + getConditionQuery();
    }

    public String getWcsImageQuery(boolean withImages) {

        String separator = String.valueOf(File.separatorChar).replaceAll("\\\\", "\\\\\\\\");

        StringBuilder sql = new StringBuilder("");
        sql.append("SELECT ");
        sql.append("    PROJECT.ABBREV_NAME AS \"Project ID\", ");
        sql.append("    CASE ");
        sql.append("        WHEN (DEPLOYMENT.NAME IS NULL OR DEPLOYMENT.NAME = '') ");
        sql.append("            THEN CONCAT(PROJECT.ABBREV_NAME, '_', REPLACE(EVENT.NAME, ' ', '_'), '_', REPLACE(CAMERA_TRAP.NAME, ' ', '_'))");
        sql.append("        ELSE ");
        sql.append("            REPLACE(DEPLOYMENT.NAME, ' ', '_') ");
        sql.append("    END AS \"Deployment ID\", ");

        sql.append("    CASE ");
        sql.append("        WHEN (DEPLOYMENT.NAME IS NULL OR DEPLOYMENT.NAME = '') ");
        sql.append("            THEN CONCAT(PROJECT.ABBREV_NAME, '_', REPLACE(EVENT.NAME, ' ', '_'), '_', REPLACE(CAMERA_TRAP.NAME, ' ', '_'), '_', IMAGE.RAW_NAME)");
        sql.append("        ELSE ");
        sql.append("            CONCAT(REPLACE(DEPLOYMENT.NAME, ' ', '_'), '_', IMAGE.RAW_NAME) ");
        sql.append("    END AS \"Image Id\", ");

        if (withImages) {
            sql.append("    CONCAT(PROJECT.ABBREV_NAME, '_', REPLACE(EVENT.NAME, ' ', '_'), '_', REPLACE(CAMERA_TRAP.NAME, ' ', '_'), '").append(separator).append("', IMAGE.RAW_NAME) AS \"LOCATION\",");
        } else {
            sql.append("    '' AS \"LOCATION\", ");
        }

        sql.append("    IMAGE_TYPE.NAME AS \"PHOTO TYPE\", ");
        sql.append("    CONCAT(PERSON.FIRST_NAME, ' ', PERSON.LAST_NAME) AS \"Photo Type Identified by\", ");
        sql.append("    CASE WHEN IMAGE_SPECIES.HOMO_SAPIENS_TYPE_ID IS NULL THEN CONCAT(FAMILY_GENUS_SPECIES.GENUS, ' ', FAMILY_GENUS_SPECIES.SPECIES) ELSE CONCAT(FAMILY_GENUS_SPECIES.GENUS, ' ', FAMILY_GENUS_SPECIES.SPECIES, ' - ', HOMO_SAPIENS_TYPE.NAME ) END AS \"Genus Species\", ");
        sql.append("    IMAGE_UNCERTAINTY_TYPE.NAME AS \"Uncertainty\", ");
        sql.append("    FAMILY_GENUS_SPECIES.IUCN_SPECIES_ID AS \"IUCN Identification Number\", ");
        sql.append("    DATE_FORMAT(IMAGE.TIME_CAPTURED, '%Y-%m-%d %H:%i:%S') as \"Date_Time Captured\", ");
        sql.append("    AGE.NAME AS \"Age\", ");
        sql.append("    IMAGE_INDIVIDUAL.SEX AS \"Sex\", ");
        sql.append("    IMAGE_INDIVIDUAL.NAME AS \"Individual ID\", ");
        sql.append("    IMAGE_SPECIES.INDIVIDUAL_COUNT AS \"Count\", ");
        sql.append("    '' AS \"Animal recognizable (Y/N)\", ");
        sql.append("    IMAGE_INDIVIDUAL.NOTE AS \"individual Animal notes\" ");
        sql.append("FROM ");
        sql.append("    IMAGE ");
        sql.append("        LEFT JOIN IMAGE_TYPE ON (IMAGE.IMAGE_TYPE_ID = IMAGE_TYPE.IMAGE_TYPE_ID) ");
        sql.append("        LEFT JOIN IMAGE_SPECIES ON (IMAGE_SPECIES.IMAGE_ID = IMAGE.IMAGE_ID) ");
        sql.append("        LEFT JOIN IMAGE_INDIVIDUAL ON (IMAGE_INDIVIDUAL.IMAGE_SPECIES_ID = IMAGE_SPECIES.IMAGE_SPECIES_ID) ");
        sql.append("        LEFT JOIN AGE ON (AGE.AGE_ID = IMAGE_INDIVIDUAL.AGE_ID) ");
        sql.append("        LEFT JOIN IMAGE_UNCERTAINTY_TYPE ON (IMAGE_UNCERTAINTY_TYPE.IMAGE_UNCERTAINTY_TYPE_ID = IMAGE_SPECIES.UNCERTAINTY_TYPE_ID) ");
        sql.append("        LEFT JOIN HOMO_SAPIENS_TYPE ON HOMO_SAPIENS_TYPE.HOMO_SAPIENS_TYPE_ID = IMAGE_SPECIES.HOMO_SAPIENS_TYPE_ID ");
        sql.append("        LEFT JOIN PERSON ON (IMAGE_SPECIES.IDENTIFY_PERSON_ID = PERSON.PERSON_ID) ");
        sql.append("        LEFT JOIN FAMILY_GENUS_SPECIES ON (FAMILY_GENUS_SPECIES.FAMILY_GENUS_SPECIES_ID = IMAGE_SPECIES.FAMILY_GENUS_SPECIES_ID), ");
        sql.append("    PROJECT, ");
        sql.append("    DEPLOYMENT, ");
        sql.append("    IMAGE_SEQUENCE, ");
        sql.append("    EVENT, ");
        sql.append("    CAMERA_TRAP_ARRAY, ");
        sql.append("    CAMERA_TRAP, ");
        sql.append("    CAMERA, ");
        sql.append("    CAMERA_MODEL ");
        sql.append("WHERE ");
        sql.append("    EVENT.PROJECT_ID = PROJECT.PROJECT_ID ");
        sql.append("    AND EVENT.EVENT_ID = DEPLOYMENT.EVENT_ID ");
        sql.append("    AND CAMERA_TRAP_ARRAY.CAMERA_TRAP_ARRAY_ID = CAMERA_TRAP.CAMERA_TRAP_ARRAY_ID ");
        sql.append("    AND DEPLOYMENT.DEPLOYMENT_ID = IMAGE_SEQUENCE.DEPLOYMENT_ID ");
        sql.append("    AND IMAGE.IMAGE_SEQUENCE_ID = IMAGE_SEQUENCE.IMAGE_SEQUENCE_ID ");
        sql.append("    AND DEPLOYMENT.CAMERA_TRAP_ID = CAMERA_TRAP.CAMERA_TRAP_ID ");
        sql.append("    AND CAMERA.CAMERA_ID = DEPLOYMENT.CAMERA_ID ");
        sql.append("    AND CAMERA.CAMERA_MODEL_ID = CAMERA_MODEL.CAMERA_MODEL_ID ");
        //System.out.println(sql.toString() + getConditionQuery());
        return sql.toString() + getConditionQuery();
    }

    public String getConditionQuery() {
        StringBuilder sql = new StringBuilder("");

        if (project != null) {
            sql.append("    AND PROJECT.PROJECT_ID = ").append(project.getProjectId());
        }

        if (event != null) {
            sql.append("    AND EVENT.EVENT_ID = ").append(event.getEventId());
        }

        if (ctArray != null) {
            sql.append("    AND CAMERA_TRAP.CAMERA_TRAP_ARRAY_ID = ").append(ctArray.getCameraTrapArrayId());
        }

        if (cameraTrap != null) {
            sql.append("    AND CAMERA_TRAP.CAMERA_TRAP_ID = ").append(cameraTrap.getCameraTrapId());
        }

        return sql.toString();
    }
}
