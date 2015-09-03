/**
 *
 */
package cern.modesti;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import cern.modesti.request.RequestType;
import cern.modesti.request.counter.CounterServiceImpl;
import cern.modesti.schema.SchemaRepository;
import cern.modesti.workflow.WorkflowService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.modesti.point.Point;
import cern.modesti.request.Request;
import cern.modesti.request.RequestStatus;
import cern.modesti.schema.Schema;
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
  WorkflowService workflowService;

  @Mock
  SchemaRepository schemaRepository;

  @Test
  @Ignore
  public void requestIsCreatedInProgress() throws Exception {
    when(schemaRepository.findOne(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getStatus() == RequestStatus.IN_PROGRESS);
  }

  @Test
  public void requestIdIsGenerated() {
    when(counterService.getNextSequence(anyString())).thenReturn(1L);
    when(schemaRepository.findOne(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getRequestId().equals("1"));
  }

  @Test
  public void requestSaveGeneratesPointIds() {
    when(counterService.getNextSequence(anyString())).thenReturn(1L).thenReturn(2L);

    Request request = getTestRequest();
    requestEventHandler.handleRequestSave(request);
    assertTrue(request.getPoints().get(0).getLineNo() == 1L);
    assertTrue(request.getPoints().get(1).getLineNo() == 2L);
  }

  @Test
  public void requestSchemaIsLinked() {
    when(schemaRepository.findOne(anyString())).thenReturn(new Schema());

    Request request = getTestRequest();
    requestEventHandler.handleRequestCreate(request);
    assertTrue(request.getCategories() != null);
  }

  private Request getTestRequest() {
    Request request = new Request();
    request.setType(RequestType.CREATE);
    request.setDescription("description");
    request.setDomain("TIM");
    request.setCategories(new HashSet<>(Arrays.asList("PLC")));
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
