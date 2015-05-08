/**
 *
 */
package cern.modesti;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cern.modesti.request.RequestType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.modesti.request.point.Point;
import cern.modesti.request.Request;
import cern.modesti.request.Request.RequestStatus;
import cern.modesti.repository.mongo.request.counter.CounterServiceImpl;
import cern.modesti.schema.Schema;
import cern.modesti.repository.mongo.schema.SchemaRepository;
import cern.modesti.request.RequestEventHandler;

/**
 * @author Justin Lewis Salmon
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestEventHandlerTest {

  /**
   * The class to be tested
   */
  @InjectMocks
  RequestEventHandler requestEventHandler;

  @Mock
  CounterServiceImpl counterService;

  @Mock
  SchemaRepository schemaRepository;

  @Test
  public void requestIsCreatedInProgress() throws Exception {
    when(schemaRepository.findOneByNameIgnoreCase(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getStatus() == RequestStatus.IN_PROGRESS);
  }

  @Test
  public void requestIdIsGenerated() {
    when(counterService.getNextSequence(anyString())).thenReturn(1L);
    when(schemaRepository.findOneByNameIgnoreCase(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getRequestId().equals("1"));
  }

  @Test
  public void requestSaveGeneratesPointIds() {
    when(counterService.getNextSequence(anyString())).thenReturn(1L).thenReturn(2L);

    Request request = getTestRequest();
    requestEventHandler.handleRequestSave(request);
    assertTrue(request.getPoints().get(0).getId() == 1L);
    assertTrue(request.getPoints().get(1).getId() == 2L);
  }

  @Test
  public void requestSchemaIsLinked() {
    when(schemaRepository.findOneByNameIgnoreCase(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getCategories() != null);
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setType(RequestType.CREATE);
    request.setDescription("cool description");
    request.setDomain("TIM");
    request.setCategories(new ArrayList<>(Arrays.asList("PLC")));
    request.setPoints(getTestPoints());
    return request;
  }

  private List<Point> getTestPoints() {
    List<Point> points = new ArrayList<>();
    points.add(new Point());
    points.add(new Point());
    return points;
  }
}
