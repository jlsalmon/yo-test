/**
 *
 */
package cern.modesti.legacy.parser;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import cern.modesti.model.SubSystem;
import cern.modesti.request.point.Point;

/**
 * @author Justin Lewis Salmon
 *
 */
public class PVSSRequestParser extends RequestParser {

  /**
   * @param sheet
   */
  public PVSSRequestParser(Sheet sheet) {
    super(sheet);
  }

  @Override
  protected Point parseDataPoint(Row row) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected String parseColumnTitle(String title, int column) {
    return null;
  }

  @Override
  protected List<String> parseCategories(List<Point> points) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  protected Double getMinimumSupportedVersion() {
    return null;
  }

  @Override
  protected int getFirstDataColumn() {
    return 0;
  }

  @Override
  protected int getLastDataColumn() {
    return 0;
  }

  @Override
  protected int getPointIdColumn() {
    return 0;
  }

}
