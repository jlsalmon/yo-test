package cern.modesti.request;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.rest.service.api.runtime.task.TaskResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import cern.modesti.schema.SchemaController;

@Component
public class RequestLinks {

  private static final Logger LOG = LoggerFactory.getLogger(RequestLinks.class);

  @Autowired
  TaskService taskService;

  /**
   *
   * @param request
   * @return
   */
  Link getSchemaLink(Request request) {
    if (request.getCategories() != null) {

      // Need to manually build a comma-separated list of categories
      StringBuilder categories = new StringBuilder();
      for (String category : request.getCategories()) {
        categories.append(category).append(",");
      }

      if (categories.length() > 0) {
        categories.deleteCharAt(categories.length() - 1);
      }

      return linkTo(methodOn(SchemaController.class).getSchema(request.getRequestId(), categories.toString())).withRel("schema");
    } else {
      LOG.warn("Request " + request.getRequestId() + " has no schema link!");
      return null;
    }
  }

  /**
   *
   * @param request
   * @return
   */
  Link getTaskLink(Request request) {
    List<Task> tasks = taskService.createTaskQuery().processVariableValueEquals("requestId", request.getRequestId()).orderByTaskCreateTime().desc().list();
    if (tasks.isEmpty()) {
      return null;
    }

    Task task = tasks.get(0);
    // The creator must submit the request
    taskService.claim(task.getId(), request.getCreator());

    // TODO is this right? Should we even allow multiple instances of the process for a particular request?
    // Link to the most recent task
    return linkTo(methodOn(TaskResource.class).getTask(task.getId(), null)).withRel("task");
  }
}
