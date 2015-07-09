/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.legacy.exception.RequestParseException;
import cern.modesti.request.point.Point;

/**
 * @author Justin Lewis Salmon
 *
 */
public class TIMRequestParser extends RequestParser {

  /**
   * Minimum supported version of the legacy Excel MODESTI request sheet
   */
  private static final Double MINIMUM_SUPPORTED_VERSION = 4.0;

  public static final int FIRST_DATA_COLUMN = 2;
  public static final int POINT_ID_COLUMN = 1;

  private Map<String, String> columnTitleMappings = new HashMap<>();

  /**
   * @param sheet
   */
  public TIMRequestParser(Sheet sheet) {
    super(sheet);

    // General mappings
    columnTitleMappings.put("pointDataType", "pointDatatype");
    columnTitleMappings.put("id", "responsiblePersonId");
    columnTitleMappings.put("pointComplementaryInfo", "pointCompInfo");

    // Alarm mappings
    columnTitleMappings.put("value", "alarmValue");
    columnTitleMappings.put("autoCallNumber", "autocallNumber");

    // Alarm Help mappings
    columnTitleMappings.put("alarmConsequences", "alarmConseq");
    columnTitleMappings.put("taskDuringWorkingHoursActionHo", "workHoursTask");
    columnTitleMappings.put("taskOutsideWorkingHoursActionHho", "outsideHoursTask");

    // Location mappings
    columnTitleMappings.put("site", "functionalityCode");
    columnTitleMappings.put("number", "buildingNumber");
    columnTitleMappings.put("floor", "buildingFloor");
    columnTitleMappings.put("room", "buildingRoom");
    columnTitleMappings.put("zone", "safetyZone");

    // OPC mappings
    columnTitleMappings.put("tagname", "opcTagname");
    columnTitleMappings.put("tagtype", "opcTagtype");
    columnTitleMappings.put("redundantAddress", "opcRedundantAddress");
    columnTitleMappings.put("addressType", "opcAddressType");
    columnTitleMappings.put("nameSpace", "opcNamespace");
    columnTitleMappings.put("cmdType", "opcCommandType");
    columnTitleMappings.put("sampleRate", "opcSampleRate");

    // DIP mappings
    columnTitleMappings.put("item", "dipItem");
    columnTitleMappings.put("field", "dipField");
    columnTitleMappings.put("index", "dipIndex");

    // PLC mappings
    columnTitleMappings.put("blockType", "plcBlockType");
    columnTitleMappings.put("wordId", "plcWordId");
    columnTitleMappings.put("bitId", "plcBitId");
    columnTitleMappings.put("nativePrefix", "plcNativePrefix");
    columnTitleMappings.put("slaveAddress", "plcSlaveAddress");
    columnTitleMappings.put("connectId", "plcConnectId");

    // LASER Source mappings
    columnTitleMappings.put("family", "laserFaultFamily");
    columnTitleMappings.put("member", "laserFaultMember");
    columnTitleMappings.put("code", "laserFaultCode");

    // JAPC mappings
    columnTitleMappings.put("protocol", "japcProtocol");
    columnTitleMappings.put("service", "japcService");
    columnTitleMappings.put("deviceName", "japcDeviceName");
    columnTitleMappings.put("propertyName", "japcPropertyName");
    columnTitleMappings.put("indexFieldName", "japcIndexFieldName");
    columnTitleMappings.put("indexName", "japcIndexName");
    columnTitleMappings.put("dataFieldName", "japcDataFieldName");
    columnTitleMappings.put("columnIndex", "japcColumnIndex");
    columnTitleMappings.put("rowIndex", "japcRowIndex");

    // Analogue mappings
    columnTitleMappings.put("conversionFactor", "plcAdConversion");
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    String mapping = columnTitleMappings.get(title);
    if (mapping != null) {
      title = mapping;
    }

    // Handle some special cases to avoid duplicate property names
    else if (title.equals("name") && column == 9) {
      title = "responsiblePersonName";
    }
    else if (title.equals("name") && column == 20) {
      title = "buildingName";
    }
    // v6.1 has alarmCategory in column 26
    else if (title.equals("category") && (column == 25 || column == 26)) {
      title = "alarmCategory";
    }
    // v6.1 has laserCategory in column 54
    else if (title.equals("category") && (column == 49 || column == 54)) {
      title = "laserCategory";
    }
    else if (title.equals("application") && column == 14) {
      title = "dipClientApp";
    }
    else if (title.equals("application") && column == 16) {
      title = "japcClientApp";
    }
    else if (title.equals("requestorId") && column == 15) {
      title = "dipRequestorId";
    }
    else if (title.equals("requestorId") && column == 17) {
      title = "japcRequestorId";
    }
    else if (title.equals("valueDeadBand") && (column == 65 || column == 66 || column == 71)) {
      title = "valueDeadband";
    }
    else if (title.equals("valueDeadBand") && (column == 69 || column == 70 || column == 75)) {
      title = "logValueDeadband";
    }
    else if (title.equals("deadBandType") && (column == 66 || column == 67 || column == 72)) {
      title = "deadbandType";
    }
    else if (title.equals("deadBandType") && (column == 70 || column == 71 || column == 76)) {
      title = "logDeadbandType";
    }
    else if (title.equals("timeDeadBand") && column == 11) {
      title = "timeDeadband";
    }
    else if (title.equals("timeDeadBand") && (column == 71 || column == 72 || column == 77)) {
      title = "logTimeDeadband";
    }

    return title;
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    List<String> categories = new ArrayList<>();

    // Naive implementation: look at the first point and assume the rest are the same.
    Point point = points.get(0);

    if (point.getProperties().containsKey("opcTagname")) {
      categories.add("OPC");
    } else if (point.getProperties().containsKey("dipItem")) {
      categories.add("DIP");
    } else if (point.getProperties().containsKey("plcBlockType")) {
      categories.add("PLC");
    } else if (point.getProperties().containsKey("japcProtocol")) {
      categories.add("JAPC");
    } else if (point.getProperties().containsKey("hostName")) {
      categories.add("DIAMON");
    } else if (point.getProperties().containsKey("dbTagname")) {
      categories.add("DB");
    }

    if (categories.isEmpty()) {
      throw new RequestParseException("Could not determine request categories");
    }

    return categories;
  }

  @Override
  protected Double getMinimumSupportedVersion() {
    return MINIMUM_SUPPORTED_VERSION;
  }

  @Override
  protected int getFirstDataColumn() {
    return FIRST_DATA_COLUMN;
  }

  @Override
  protected int getLastDataColumn(Double version) {
    // v4.1 ends at 76, v4.2/v4.3 at 77, and v6.1 at 82
    if (version == 6.1) {
      return 82;
    } else if (version >= 4.2) {
      return 77;
    } else {
      return 76;
    }
  }

  @Override
  protected int getPointIdColumn() {
    return POINT_ID_COLUMN;
  }
}
