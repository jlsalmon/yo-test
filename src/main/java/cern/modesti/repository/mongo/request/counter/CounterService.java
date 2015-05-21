/**
 *
 */
package cern.modesti.repository.mongo.request.counter;

/**
 * @author Justin Lewis Salmon
 *
 */
public interface CounterService {

  String REQUEST_ID_SEQUENCE = "requests";

  Long getNextSequence(String collectionName);
}
