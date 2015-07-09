package cern.modesti.repository.jpa.gmao;

import cern.modesti.repository.base.ReadOnlyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * TODO
 *
 * @author Justin Lewis Salmon
 */
public interface GmaoCodeRepository extends ReadOnlyRepository<GmaoCode, Long> {

  @RestResource(rel = "find", path = "find")
  @Cacheable("gmaoCodes")
  List<GmaoCode> findByValueStartsWithIgnoreCase(@Param("query") String query);
}
