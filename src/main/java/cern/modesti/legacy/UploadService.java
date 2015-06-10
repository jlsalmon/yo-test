/**
 *
 */
package cern.modesti.legacy;

import cern.modesti.legacy.parser.RequestParser;
import cern.modesti.legacy.parser.RequestParserFactory;
import cern.modesti.repository.jpa.person.PersonRepository;
import cern.modesti.repository.jpa.subsystem.SubSystemRepository;
import cern.modesti.repository.mongo.request.RequestRepository;
import cern.modesti.repository.mongo.request.counter.CounterService;
import cern.modesti.request.Request;
import cern.modesti.security.ldap.User;
import cern.modesti.workflow.WorkflowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.Principal;

/**
 * @author Justin Lewis Salmon
 *
 */
@Service
public class UploadService {

  private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);

  @Autowired
  private RequestRepository requestRepository;

  @Autowired
  private RequestParserFactory requestParserFactory;

  @Autowired
  private CounterService counterService;

  @Autowired
  private WorkflowService workflowService;

  /**
   *
   * @param filename
   * @param stream
   * @param principal
   * @return
   */
  public Request parseRequestFromExcelSheet(String filename, InputStream stream, Principal principal) {
    RequestParser parser = requestParserFactory.createRequestParser(stream);
    Request request = parser.parseRequest();

    request.setDescription(filename);
    request.setCreator((User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal());

    // Generate a request id
    request.setRequestId(counterService.getNextSequence("requests").toString());
    LOG.debug("generated request id: " + request.getRequestId());

    // Kick off the workflow process
    workflowService.startProcessInstance(request);

    // Store the request in the database
    requestRepository.insert(request);
    return request;
  }
}
